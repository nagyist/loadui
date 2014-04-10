package com.eviware.loadui.components.web;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.eviware.loadui.util.html.HtmlAssetScraper;
import com.eviware.loadui.util.property.UrlProperty;
import com.eviware.loadui.util.test.CounterAsserter;
import com.eviware.loadui.util.test.FakeHttpClient;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

public class WebRunnerTest
{
	public static final String TEST_URL = "http://www.example.org";
	public static final String GET_MAIN_URL = "GET http://www.example.org";
	public static final String GET_ASSET_1 = "GET http://www.example.org/image1.png";
	public static final String GET_ASSET_2 = "GET http://www.example.org/style.css";
	private WebRunner runner;
	private ComponentItem component;
	private ComponentTestUtils ctu;
	private InputTerminal triggerTerminal;
	private OutputTerminal resultsTerminal;
	private FakeHttpClient httpClient;
	private BlockingQueue<TerminalMessage> results;

	@Before
	public void setup() throws ComponentCreationException, IOException
	{
		ctu = new ComponentTestUtils();
		ctu.getDefaultBeanInjectorMocker();

		component = createComponent();
	}

	private ComponentItem createComponent()
	{
		HtmlAssetScraper assetScraper = FakeAssetScraper.returningAssets(
				"http://www.example.org/image1.png",
				"http://www.example.org/style.css"
		);
		return createComponent( assetScraper );
	}

	private ComponentItem createComponent( HtmlAssetScraper assetScraper )
	{
		component = ctu.createComponentItem();

		ComponentContext contextSpy = spy( component.getContext() );
		ctu.mockStatisticsFor( component, contextSpy );

		httpClient = new FakeHttpClient();

//		runner = new WebRunner( contextSpy, assetScraper, FakeRequestRunnerProvider.usingHttpClient( httpClient ) );
//		ctu.setComponentBehavior( component, runner );
//
//		triggerTerminal = runner.getTriggerTerminal();
//		resultsTerminal = runner.getResultTerminal();
//
//		results = ctu.getMessagesFrom( resultsTerminal );
//
//		// GIVEN
//		setProperty( UrlProperty.URL, TEST_URL );
//		runner.setLoadTestRunning( true );

		return component;
	}

	@Test
	public void shouldRequestAssets() throws Exception
	{
		Multiset<String> expectedRequests = ImmutableMultiset.of(
				GET_MAIN_URL,
				GET_ASSET_1,
				GET_ASSET_2
		);

		// WHEN
		triggerAndWait();

		// THEN
		Multiset<String> actualRequests = httpClient.popAllRequests();
		assertEquals( expectedRequests, actualRequests );
		CounterAsserter.oneSuccessfulRequest( component.getContext() );
	}

	@Test
	public void shouldHandleConcurrentRequests() throws Exception
	{
		Multiset<String> expectedRequests =
				new ImmutableMultiset.Builder<String>()
						.addCopies( GET_MAIN_URL, 3 )
						.addCopies( GET_ASSET_1, 3 )
						.addCopies( GET_ASSET_2, 3 )
						.build();

		// WHEN
		ctu.sendSimpleTrigger( triggerTerminal );
		ctu.sendSimpleTrigger( triggerTerminal );
		ctu.sendSimpleTrigger( triggerTerminal );
		getNextOutputMessage();
		getNextOutputMessage();
		getNextOutputMessage();

		// THEN
		Multiset<String> actualRequests = httpClient.popAllRequests();
		assertEquals( expectedRequests, actualRequests );
		CounterAsserter.forHolder( component.getContext() )
				.sent( 3 )
				.completed( 3 )
				.failures( 0 );
	}

	@Test
	public void shouldHandleNoAssets() throws Exception
	{
		// GIVEN
		createComponent( FakeAssetScraper.returningNoAssets() );

		Multiset<String> expectedRequests = ImmutableMultiset.of( GET_MAIN_URL );

		// WHEN
		triggerAndWait();

		// THEN
		Multiset<String> actualRequests = httpClient.popAllRequests();
		assertEquals( expectedRequests, actualRequests );
		CounterAsserter.oneSuccessfulRequest( component.getContext() );
	}

	@Test
	public void shouldFailOnInvalidUrl() throws Exception
	{
		// GIVEN
		setProperty( UrlProperty.URL, "hxxp ://wat?.com" );

		// WHEN
		triggerAndWait();

		// THEN
		assertFalse( httpClient.hasReceivedRequests() );
		CounterAsserter.oneFailedRequest( component.getContext() );
	}

	@Test
	public void shouldFailWhenMainRequestReturns404() throws Exception
	{
		// GIVEN
		setProperty( UrlProperty.URL, "http://404" );

		// WHEN
		triggerAndWait();

		// THEN
		assertTrue( httpClient.hasReceivedRequests() );
		CounterAsserter.oneFailedRequest( component.getContext() );
	}

	private void triggerAndWait() throws InterruptedException
	{
		ctu.sendSimpleTrigger( triggerTerminal );
		getNextOutputMessage();
	}

	private void setProperty( String name, Object value )
	{
		component.getContext().getProperty( name ).setValue( value );
		try
		{
			TestUtils.awaitEvents( component );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}



	private TerminalMessage getNextOutputMessage() throws InterruptedException
	{
		return results.poll( 1, SECONDS );
	}
}
