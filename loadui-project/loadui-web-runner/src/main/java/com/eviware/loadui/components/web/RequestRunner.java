package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.components.web.internal.RequestRunnerExecutor;
import com.eviware.loadui.webdata.HttpWebResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.Immutable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class RequestRunner implements Callable<Boolean>
{
	static final Logger log = LoggerFactory.getLogger( RequestRunner.class );

	private final RequestRunnerExecutor requestRunnerExecutor;
	private final WebRunnerStatsSender statsSender;
	private final URI pageUri;
	private final Iterable<URI> assets;
	private PageUriRequest pageRequest;
	private Collection<Request> assetRequests;
	RequestConverter requestConverter = new RequestConverter();

	public RequestRunner( Clock clock,
								 CloseableHttpAsyncClient httpClient,
								 URI pageUri,
								 Iterable<URI> assets,
								 WebRunnerStatsSender statsSender )
	{
		this.statsSender = statsSender;
		this.pageUri = pageUri;
		this.assets = assets;
		requestRunnerExecutor = new RequestRunnerExecutor( httpClient, statsSender, clock );
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
	public class Request
	{
		private final URI uri;
		private final String resource;

		public Request( URI uri )
		{
			this.uri = uri;
			this.resource = uri.toASCIIString();
		}

		public boolean isFailure( HttpWebResponse response )
		{
			return false; // assets should never fail requests
		}

		public void handleError( Exception e )
		{
			log.warn( "Request Error", e );
			statsSender.updateRequestFailed( resource );
		}

		public URI getUri()
		{
			return uri;
		}

		public String getResource()
		{
			return resource;
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
		public boolean isFailure( HttpWebResponse response )
		{
			return response.getResponseCode() >= 300;
		}

	}


}
