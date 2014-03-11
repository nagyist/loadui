package com.eviware.loadui.components.rest;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.components.rest.statistics.LatencyCalculator;
import com.eviware.loadui.impl.component.categories.RunnerBase;
import com.eviware.loadui.util.RealClock;
import com.eviware.loadui.util.component.ComponentTestUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.eviware.loadui.api.model.CanvasItem.REQUEST_FAILURE_COUNTER;
import static com.eviware.loadui.components.rest.RestRunner.BODY;
import static com.eviware.loadui.components.rest.RestRunner.METHOD;
import static com.eviware.loadui.components.rest.RestRunner.URL;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

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

	@Before
	public void setup() throws ComponentCreationException
	{
		ctu = new ComponentTestUtils();
		ctu.getDefaultBeanInjectorMocker();

		component = ctu.createComponentItem();
		ComponentContext contextSpy = spy( component.getContext() );
		ctu.mockStatisticsFor( component, contextSpy );

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
		HttpUriRequest request = httpClient.lastRequest();
		assertThat( request.getMethod(), is( "GET" ) );
		assertThat( request.getURI(), is( URI.create( TEST_URL )) );
		CounterAsserter.forHolder( component.getContext() )
				.sent( 1 )
				.completed( 1 )
				.failures( 0 );
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
		CustomHttpRequest request = ( CustomHttpRequest )httpClient.lastRequest();
		assertThat( request.getMethod(), is( "POST" ) );
		assertThat( request.getURI(), is( URI.create( TEST_URL )) );
		assertThat( EntityUtils.toString( request.getEntity() ), is( "Foo" ) );
		CounterAsserter.forHolder( component.getContext() )
				.sent( 1 )
				.completed( 1 )
				.failures( 0 );
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
		CustomHttpRequest request = ( CustomHttpRequest )httpClient.lastRequest();
		assertThat( request.getURI(), is( URI.create( TEST_URL )) );
		assertThat( EntityUtils.toString( request.getEntity() ), is( "Foo" ) );
		CounterAsserter.forHolder( component.getContext() )
				.sent( 1 )
				.completed( 1 )
				.failures( 0 );
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
	}

	private TerminalMessage getNextOutputMessage() throws InterruptedException
	{
		return results.poll( 1, SECONDS );
	}
}
