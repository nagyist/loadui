package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.webdata.StreamConsumer;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class RequestRunnerTest
{

	Clock clock;
	CloseableHttpClient httpClient;
	List<URI> uris = new ArrayList<>();
	WebRunnerStatsSender mockStatsSender;
	StreamConsumer mockConsumer;
	static final String RESPONSE_CONTENT = "hello";

	@Before
	public void setup() throws Exception
	{
		clock = mock( Clock.class );
		when( clock.millis() ).thenReturn( 0L, 10L, 25L );

		httpClient = mock( CloseableHttpClient.class );

		CloseableHttpResponse mockResponse = mock( CloseableHttpResponse.class );
		StatusLine mockStatusLine = mock( StatusLine.class );
		when( mockStatusLine.getStatusCode() ).thenReturn( 200 );
		when( mockResponse.getStatusLine() ).thenReturn( mockStatusLine );

		HttpEntity mockEntity = mock( HttpEntity.class );
		when( mockResponse.getEntity() ).thenReturn( mockEntity );
		Header mockContentType = mock( Header.class );
		Header mockContentEncoding = mock( Header.class );
		when( mockEntity.getContentType() ).thenReturn( mockContentType );
		when( mockEntity.getContentEncoding() ).thenReturn( mockContentEncoding );

		when( httpClient.execute( any( HttpGet.class ) ) ).thenReturn( mockResponse );

		mockConsumer = mock( StreamConsumer.class );
		when( mockConsumer.consume( any( InputStream.class ) ) ).thenReturn( RESPONSE_CONTENT.getBytes() );

		mockStatsSender = mock( WebRunnerStatsSender.class );
	}

	@Test
	public void allResourcesAreAdded()
	{
		uris.addAll( asList( URI.create( "url1" ), URI.create( "url2" ) ) );
		RequestRunner runner = createRunner();

		runner.run();

		verify( mockStatsSender ).addResource( "url1" );
		verify( mockStatsSender ).addResource( "url2" );
	}

	@Test
	public void allRequestsArePerformed() throws IOException
	{
		uris.addAll( asList( URI.create( "url1" ), URI.create( "url2" ) ) );
		RequestRunner runner = createRunner();

		runner.run();

		verify( httpClient, times( 2 ) ).execute( any( HttpGet.class ) );
	}

	@Test
	public void resultsAreUpdatedOnRequest()
	{
		uris.addAll( asList( URI.create( "url1" ) ) );

		RequestRunner runner = createRunner();

		runner.run();

		verify( mockStatsSender ).updateRequestSent( "url1" );
		verify( mockStatsSender ).updateLatency( "url1", 10L );
		verify( mockStatsSender ).updateResponse( "url1", 25L, RESPONSE_CONTENT.length() );
	}

	@Test
	public void runningRequestsCanBeCancelled() throws Exception
	{
		final long EACH_REQUEST_TIME = 250L;
		final AtomicInteger interruptedThreads = new AtomicInteger();
		final CountDownLatch latch = new CountDownLatch( 2 );

		RequestRunner runner = createRunner();
		runner.requestConverter = mock( RequestRunner.RequestConverter.class );

		RequestRunner.Request mockReq = mock( RequestRunner.Request.class );
		when( mockReq.call() ).thenAnswer( new Answer<Void>()
		{
			@Override
			public Void answer( InvocationOnMock invocation ) throws Throwable
			{
				try
				{
					latch.countDown();
					Thread.sleep( EACH_REQUEST_TIME );
				}
				catch( InterruptedException e )
				{
					interruptedThreads.incrementAndGet();
					throw e;
				}
				return null;
			}
		} );

		when( runner.requestConverter.convert( uris ) ).thenReturn( Arrays.asList( mockReq, mockReq ) );

		new Thread( runner ).start();

		latch.await( 1, TimeUnit.SECONDS );

		int cancelledRequests = runner.cancelAllRequests();

		assertThat( cancelledRequests, is( 2 ) );

		Thread.sleep( EACH_REQUEST_TIME );
		assertThat( interruptedThreads.get(), is( 2 ) );
	}

	private RequestRunner createRunner()
	{
		RequestRunner runner = new RequestRunner( clock, httpClient, uris, mockStatsSender );
		runner.setConsumer( mockConsumer );
		return runner;
	}

}
