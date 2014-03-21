package com.eviware.loadui.components.web;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;

import com.eviware.loadui.util.component.ComponentTestUtils;
import static com.eviware.loadui.components.web.WebRunner.WEB_PAGE_URL_PROP;
import com.eviware.loadui.util.html.HtmlAssetScraper;
import com.eviware.loadui.util.test.CounterAsserter;
import com.eviware.loadui.util.test.FakeHttpClient;
import com.eviware.loadui.util.test.TestUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.BlockingQueue;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;

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
	public void setup() throws ComponentCreationException
	{
		ctu = new ComponentTestUtils();
		ctu.getDefaultBeanInjectorMocker();

		component = ctu.createComponentItem();
		ComponentContext contextSpy = spy( component.getContext() );
		ctu.mockStatisticsFor( component, contextSpy );

		httpClient = new FakeHttpClient();
		runner = new WebRunner( contextSpy, HtmlAssetScraper.create(), new FakeRequestRunnerProvider() );
		ctu.setComponentBehavior( component, runner );

		triggerTerminal = runner.getTriggerTerminal();
		resultsTerminal = runner.getResultTerminal();

		results = ctu.getMessagesFrom( resultsTerminal );

		// GIVEN
		setProperty( WEB_PAGE_URL_PROP, TEST_URL );
	}

	@Test
	@Ignore
	public void shouldSendRequests() throws Exception
	{
		// WHEN
		triggerAndWait();

		// THEN
		HttpUriRequest request = httpClient.lastRequest();
		assertThat( request.getMethod(), is( "GET" ) );
		assertThat( request.getURI(), is( URI.create( TEST_URL )) );
		CounterAsserter.oneSuccessfulRequest( component.getContext() );
	}

	@Test
	@Ignore
	public void shouldNotRequireHttpPrefix() throws Exception
	{
		// GIVEN
		setProperty( WEB_PAGE_URL_PROP, TEST_URL.replace( "http://", "" ) );

		// WHEN
		triggerAndWait();

		// THEN
		HttpUriRequest request = httpClient.lastRequest();
		assertThat( request.getMethod(), is( "GET" ) );
		assertThat( request.getURI(), is( URI.create( TEST_URL )) );
		CounterAsserter.oneSuccessfulRequest( component.getContext() );
	}

	@Test
	@Ignore
	public void shouldFailOnBadUrl() throws Exception
	{
		// GIVEN
		setProperty( WEB_PAGE_URL_PROP, "hxxp:/mal formed.url" );

		// WHEN
		triggerAndWait();

		// THEN
		assertFalse( httpClient.hasReceivedRequests() );
		CounterAsserter.forHolder( component.getContext() )
				.sent( 1 )
				.completed( 1 )
				.failures( 1 );
	}

	private void triggerAndWait() throws InterruptedException
	{
		ctu.sendSimpleTrigger( triggerTerminal );
		getNextOutputMessage();
	}

	private void setProperty(String name, Object value )
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
