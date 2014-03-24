package com.eviware.loadui.components.web;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.eviware.loadui.util.html.HtmlAssetScraper;
import com.eviware.loadui.util.test.CounterAsserter;
import com.eviware.loadui.util.test.FakeHttpClient;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import static com.eviware.loadui.components.web.WebRunner.WEB_PAGE_URL_PROP;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class WebRunnerTest
{
	public static final String TEST_URL = "http://www.example.org";
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

		component = ctu.createComponentItem();
		ComponentContext contextSpy = spy( component.getContext() );
		ctu.mockStatisticsFor( component, contextSpy );

		HtmlAssetScraper assetScraper = mock( HtmlAssetScraper.class );
		Set<URI> assets = ImmutableSet.of(
				URI.create( "http://www.example.org/image1.png" ),
				URI.create( "http://www.example.org/style.css" )
		);
		when( assetScraper.scrapeUrl( anyString() ) ).thenReturn( assets );

		httpClient = new FakeHttpClient();
		runner = new WebRunner( contextSpy, assetScraper, FakeRequestRunnerProvider.usingHttpClient( httpClient ) );
		ctu.setComponentBehavior( component, runner );

		triggerTerminal = runner.getTriggerTerminal();
		resultsTerminal = runner.getResultTerminal();

		results = ctu.getMessagesFrom( resultsTerminal );

		// GIVEN
		setProperty( WEB_PAGE_URL_PROP, TEST_URL );
	}

	@Test
	public void shouldRequestAssets() throws Exception
	{
		Multiset<String> expectedRequests = ImmutableMultiset.of(
				"GET http://www.example.org/image1.png",
				"GET http://www.example.org/style.css"
		);

		// WHEN
		triggerAndWait();

		// THEN
		Multiset<String> actualRequests = httpClient.popAllRequests();
		assertEquals( expectedRequests, actualRequests );
		CounterAsserter.oneSuccessfulRequest( component.getContext() );
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
