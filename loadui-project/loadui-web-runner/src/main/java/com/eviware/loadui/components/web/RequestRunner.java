package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.components.web.internal.RequestRunnerExecutor;
import com.eviware.loadui.webdata.HttpWebResponse;
import com.eviware.loadui.webdata.StreamConsumer;
import com.eviware.loadui.webdata.results.HttpWebResult;
import com.google.common.base.Function;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RequestRunner implements Callable<Boolean>
{
	static final Logger log = LoggerFactory.getLogger( RequestRunner.class );

	private final RequestRunnerExecutor requestRunnerExecutor = new RequestRunnerExecutor();
	private final CloseableHttpClient httpClient;
	private final Clock clock;
	private final WebRunnerStatsSender statsSender;
	private final URI pageUri;
	private final Iterable<URI> assets;
	private PageUriRequest pageRequest;
	private Collection<Request> assetRequests;
	private StreamConsumer consumer = new StreamConsumer();
	RequestConverter requestConverter = new RequestConverter();

	private static final Function<Future<Boolean>, Boolean> futureToBoolean = new Function<Future<Boolean>, Boolean>()
	{
		@Nullable
		@Override
		public Boolean apply( @Nullable Future<Boolean> input )
		{
			try
			{
				return input != null && input.get();
			}
			catch( InterruptedException | ExecutionException e )
			{
				log.debug( "Problem while waiting for request future: {}", e );
				return false;
			}
		}
	};

	public RequestRunner( Clock clock, CloseableHttpClient httpClient,
								 URI pageUri,
								 Iterable<URI> assets,
								 WebRunnerStatsSender statsSender )
	{
		this.clock = clock;
		this.httpClient = httpClient;
		this.statsSender = statsSender;
		this.pageUri = pageUri;
		this.assets = assets;
	}

	public void setConsumer( StreamConsumer consumer )
	{
		this.consumer = consumer;
	}

	@Override
	public Boolean call()
	{
		if( assetRequests == null )
		{
			this.pageRequest = requestConverter.convertPageUri( pageUri );
			this.assetRequests = requestConverter.convertAssets( assets );
		}
		log.debug( "Running all requests, number of assets: {}", assetRequests.size() );
		try
		{
			boolean runAssets = requestRunnerExecutor.runPageRequest( pageRequest ).get();
			if( runAssets )
				requestRunnerExecutor.runAll( assetRequests ).get();
			return runAssets;
		}
		catch( Exception e )
		{
			// if an Exception is thrown here, whatever failed already notified the statsSender so nothing needs to be done
			e.printStackTrace();
			return false;
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

		public PageUriRequest convertPageUri( URI uri )
		{
			addResource( uri );
			return new PageUriRequest( uri );
		}

		public List<Request> convertAssets( Iterable<URI> uris )
		{
			List<Request> reqs = new ArrayList<>();
			for( URI uri : uris )
			{
				addResource( uri );
				reqs.add( new Request( uri ) );
			}
			return reqs;
		}

		private void addResource( URI uri )
		{
			log.debug( "Creating request for URI {}", uri.toASCIIString() );
			statsSender.addResource( uri.toASCIIString() );
		}

	}

	@Immutable
	public class Request implements Callable<Boolean>
	{
		private final URI uri;
		private final String resource;

		public Request( URI uri )
		{
			this.uri = uri;
			this.resource = uri.toASCIIString();
		}

		@Override
		public Boolean call()
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
					if( isFailure( webResponse ) )
					{
						handleError( new RuntimeException( "Request to " + resource + " failed" ) );
						return false;
					}
					else
					{
						statsSender.updateLatency( resource, clock.millis() - startTime );
						long contentLength = webResponse.getContentLength();
						statsSender.updateResponse( resource, clock.millis() - startTime, contentLength );
					}
				}
			}
			catch( IOException e )
			{
				handleError( e );
			}
			return true;
		}

		protected boolean isFailure( HttpWebResponse response )
		{
			return false; // assets should never fail requests
		}

		private void handleError( Exception e )
		{
			log.warn( "Request Error", e );
			statsSender.updateRequestFailed( resource );
		}

	}

	@Immutable
	public class PageUriRequest extends Request
	{

		public PageUriRequest( URI uri )
		{
			super( uri );
		}

		@Override
		protected boolean isFailure( HttpWebResponse response )
		{
			return response.getResponseCode() != 200;
		}

	}


}
