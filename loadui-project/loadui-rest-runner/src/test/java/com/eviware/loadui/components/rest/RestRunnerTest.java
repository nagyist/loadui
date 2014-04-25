package com.eviware.loadui.components.rest;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.RealClock;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.eviware.loadui.util.component.StatisticUpdateRecorder;
import com.eviware.loadui.util.test.CounterAsserter;
import com.eviware.loadui.util.test.FakeHttpClient;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.collect.ImmutableList;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.BlockingQueue;

import static com.eviware.loadui.components.rest.HeaderManager.HEADERS;
import static com.eviware.loadui.components.rest.RestRunner.BODY;
import static com.eviware.loadui.components.rest.RestRunner.METHOD;
import static com.eviware.loadui.util.component.StatisticUpdateRecorder.newInstance;
import static com.eviware.loadui.util.property.UrlProperty.URL;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

public class RestRunnerTest
{
	public static final String TEST_URL = "http://www.example.org";
	private RestRunner runner;
	private ComponentItem component;
	private ComponentTestUtils ctu;
	private InputTerminal triggerTerminal;
	private OutputTerminal resultsTerminal;
	private FakeHttpClient httpClient;
	private BlockingQueue<TerminalMessage> results;
	private StatisticUpdateRecorder statisticRecorder;

	@Before
	public void setup() throws ComponentCreationException
	{
		ctu = new ComponentTestUtils();
		ctu.getDefaultBeanInjectorMocker();

		component = ctu.createComponentItem();
		ComponentContext contextSpy = spy( component.getContext() );
		statisticRecorder = newInstance( component, contextSpy );

		httpClient = new FakeHttpClient();
		runner = new RestRunner( contextSpy, httpClient, new RealClock() );
		ctu.setComponentBehavior( component, runner );

		triggerTerminal = runner.getTriggerTerminal();
		resultsTerminal = runner.getResultTerminal();

		results = ctu.getMessagesFrom( resultsTerminal );

		// GIVEN
		setProperty( URL, TEST_URL );
	}

	@Test
	public void shouldSendRequests() throws Exception
	{
		// WHEN
		triggerAndWait();

		// THEN
		HttpUriRequest request = httpClient.popRequest();
		assertThat( request.getMethod(), is( "GET" ) );
		assertThat( request.getURI(), is( URI.create( TEST_URL )) );
		CounterAsserter.oneSuccessfulRequest( component.getContext() );
	}

	@Test
	public void shouldUpdateStatistics() throws Exception
	{
		shouldSendRequests();
		assertThat( statisticRecorder.getUpdatesToVariable( "Latency" ).size(), is( 1 ) );
		assertThat( statisticRecorder.getUpdatesToVariable( "Time Taken" ).size(), is( 1 ) );
		assertEquals( ImmutableList.of( ( Number ) Long.valueOf( FakeHttpClient.RESPONSE_BYTES.length ) ),
				statisticRecorder.getUpdatesToVariable( "Response Size" ) );
	}

	@Test
	public void shouldNotRequireHttpPrefix() throws Exception
	{
		// GIVEN
		setProperty( URL, TEST_URL.replace( "http://", "" ) );

		// WHEN
		triggerAndWait();

		// THEN
		HttpUriRequest request = httpClient.popRequest();
		assertThat( request.getMethod(), is( "GET" ) );
		assertThat( request.getURI(), is( URI.create( TEST_URL )) );
		CounterAsserter.oneSuccessfulRequest( component.getContext() );
	}

	@Test
	public void shouldIncludeHeaders() throws Exception
	{
		String headers = "Content-Type: text/xml; charset=utf-8" + System.lineSeparator()
				+	"Multiple-value: a" + System.lineSeparator() + "Multiple-value: b";

		// GIVEN
		setProperty( HEADERS, headers );

		// WHEN
		triggerAndWait();

		// THEN
		HttpUriRequest request = httpClient.popRequest();
		assertThat( request.getFirstHeader( "Content-Type" ).getValue(), is( "text/xml; charset=utf-8" ) );
		assertThat( request.getFirstHeader( "Multiple-value" ).getValue(), is( "a" ) );
		assertThat( request.getLastHeader( "Multiple-value" ).getValue(), is( "b" ) );
		CounterAsserter.oneSuccessfulRequest( component.getContext() );
	}

	@Test
	public void shouldSendPostRequests() throws Exception
	{
		// GIVEN
		setProperty( METHOD, "POST" );
		setProperty( BODY, "Foo");

		// WHEN
		triggerAndWait();

		// THEN
		CustomHttpRequest request = ( CustomHttpRequest )httpClient.popRequest();
		assertThat( request.getMethod(), is( "POST" ) );
		assertThat( request.getURI(), is( URI.create( TEST_URL )) );
		assertThat( EntityUtils.toString( request.getEntity() ), is( "Foo" ) );
		CounterAsserter.oneSuccessfulRequest( component.getContext() );
	}

	@Test
	public void shouldSendPutRequests() throws Exception
	{
		// GIVEN
		setProperty( METHOD, "PUT" );
		setProperty( BODY, "Foo" );

		// WHEN
		triggerAndWait();

		// THEN
		CustomHttpRequest request = ( CustomHttpRequest )httpClient.popRequest();
		assertThat( request.getURI(), is( URI.create( TEST_URL )) );
		assertThat( EntityUtils.toString( request.getEntity() ), is( "Foo" ) );
		CounterAsserter.oneSuccessfulRequest( component.getContext() );
	}

	@Test
	public void shouldFailOnBadUrl() throws Exception
	{
		// GIVEN
		setProperty( URL, "hxxp:/mal formed.url" );

		// WHEN
		triggerAndWait();

		// THEN
		assertFalse( httpClient.hasReceivedRequests() );
		CounterAsserter.oneFailedRequest( component.getContext() );
	}

	@Test
	public void shouldFailOn404() throws Exception
	{
		// GIVEN
		setProperty( URL, "http://404" );

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
