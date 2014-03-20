package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.webdata.HttpWebResponse;
import com.eviware.loadui.webdata.StreamConsumer;
import com.eviware.loadui.webdata.results.HttpWebResult;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class RequestRunner implements Runnable
{
	static final Logger log = LoggerFactory.getLogger( RequestRunner.class );

	public static final int MAX_REQUEST_THREADS = 5;

	private ListeningExecutorService requestExecutor =
			MoreExecutors.listeningDecorator( Executors.newFixedThreadPool( MAX_REQUEST_THREADS ) );

	private final CloseableHttpClient httpClient;
	private final Clock clock;
	private final Collection<Request> requests;
	private final WebRunnerStatsSender statsSender;
	private StreamConsumer consumer = new StreamConsumer();

	public RequestRunner( Clock clock, CloseableHttpClient httpClient,
								 Iterable<URI> pageUris, WebRunnerStatsSender statsSender )
	{
		this.clock = clock;
		this.httpClient = httpClient;
		this.statsSender = statsSender;
		this.requests = asRequests( pageUris );
	}

	private Collection<Request> asRequests( Iterable<URI> pageUris )
	{
		List<Request> reqs = new ArrayList<>();
		for( URI uri : pageUris )
		{
			log.debug( "Creating request for URI {}", uri.toASCIIString() );
			reqs.add( new Request( uri ) );
			statsSender.addResource( uri.toASCIIString() );
		}
		return reqs;
	}

	public void setConsumer( StreamConsumer consumer )
	{
		this.consumer = consumer;
	}

	@Override
	public void run()
	{
		log.debug( "Running all requests, size: {}", requests.size() );
		try
		{
			List<Future<Void>> futures = requestExecutor.invokeAll( requests );
			waitOnAll( futures );
		}
		catch( Exception e )
		{
			// if an Exception is thrown here, whatever failed already notified the statsSender so nothing needs to be done
			e.printStackTrace();
		}
	}

	private void waitOnAll( List<Future<Void>> futures )
			throws InterruptedException, ExecutionException, TimeoutException
	{
		for( Future<Void> future : futures )
		{
			future.get( 5, TimeUnit.MINUTES );
		}
	}

	private class Request implements Callable<Void>
	{
		private final URI uri;
		private final String resource;

		public Request( URI uri )
		{
			this.uri = uri;
			this.resource = uri.toASCIIString();
		}

		@Override
		public Void call()
		{
			log.debug( "Running request: {}", uri.toASCIIString() );
			HttpGet get = new HttpGet( uri );
			statsSender.updateRequestSent( resource );

			long startTime = clock.millis();
			try(CloseableHttpResponse response = httpClient.execute( get ))
			{
				HttpWebResult result = HttpWebResult.of( response );
				if( result.isFailure() )
				{
					handleError( result.getException() );
				}
				else
				{
					HttpWebResponse webResponse = result.getResponse();
					webResponse.setConsumer( consumer );
					int statusCode = webResponse.getResponseCode();
					if( statusCode == 200 )
					{
						statsSender.updateLatency( resource, clock.millis() - startTime );
						long contentLength = webResponse.getContentLength();
						statsSender.updateResponse( resource, clock.millis() - startTime, contentLength );
					}
					else
					{
						handleError( new RuntimeException( "Status code was " + statusCode ) );
					}
				}
			}
			catch( IOException e )
			{
				handleError( e );
			}
			return null;
		}

		private void handleError( Exception e )
		{
			log.warn( "Request Error", e );
			statsSender.updateRequestFailed( resource );
		}

	}


}
