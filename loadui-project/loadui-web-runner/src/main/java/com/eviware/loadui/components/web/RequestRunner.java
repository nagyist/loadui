package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.components.web.internal.RequestRunnerExecutor;
import com.eviware.loadui.webdata.HttpWebResponse;
import com.eviware.loadui.webdata.StreamConsumer;
import com.eviware.loadui.webdata.results.HttpWebResult;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class RequestRunner implements Runnable
{
	static final Logger log = LoggerFactory.getLogger( RequestRunner.class );

	private final RequestRunnerExecutor requestRunnerExecutor = new RequestRunnerExecutor();
	private final CloseableHttpClient httpClient;
	private final Clock clock;
	private final WebRunnerStatsSender statsSender;
	private final Iterable<URI> pageUris;
	private Collection<Request> requests;
	private StreamConsumer consumer = new StreamConsumer();
	RequestConverter requestConverter = new RequestConverter();

	public RequestRunner( Clock clock, CloseableHttpClient httpClient,
								 Iterable<URI> pageUris,
								 WebRunnerStatsSender statsSender )
	{
		this.clock = clock;
		this.httpClient = httpClient;
		this.statsSender = statsSender;
		this.pageUris = pageUris;
	}

	public void setConsumer( StreamConsumer consumer )
	{
		this.consumer = consumer;
	}

	@Override
	public void run()
	{
		if( requests == null )
		{
			this.requests = requestConverter.convert( pageUris );
		}
		log.debug( "Running all requests, size: {}", requests.size() );
		try
		{
			requestRunnerExecutor.runAll( requests ).get();
		}
		catch( Exception e )
		{
			// if an Exception is thrown here, whatever failed already notified the statsSender so nothing needs to be done
			e.printStackTrace();
		}
	}

	public void resetCounters()
	{
		statsSender.reset();
	}

	public int cancelAllRequests()
	{
		return requestRunnerExecutor.cancelAll();
	}

	class RequestConverter
	{

		public List<Request> convert( Iterable<URI> pageUris )
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

	}

	@Immutable
	public class Request implements Callable<Void>
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
