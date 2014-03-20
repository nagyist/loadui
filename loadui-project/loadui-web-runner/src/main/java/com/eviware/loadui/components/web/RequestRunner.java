package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.webdata.results.HttpWebResult;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RequestRunner implements Runnable
{
	public static final int MAX_REQUEST_THREADS = 5;

	private Executor requestExecutor = Executors.newFixedThreadPool( MAX_REQUEST_THREADS );

	private final CloseableHttpClient httpClient;
	private final Clock clock;
	private final Iterable<Request> requests;

	public RequestRunner( Clock clock, CloseableHttpClient httpClient, Iterable<URI> pageUris )
	{
		this.clock = clock;
		this.httpClient = httpClient;
		this.requests = asRequests( pageUris );
	}

	private Iterable<Request> asRequests( Iterable<URI> pageUris )
	{
		List<Request> requests = new ArrayList<>();
		for( URI uri : pageUris )
		{
			requests.add( new Request( uri ) );
		}
		return requests;
	}

	@Override
	public void run()
	{
		for( Request request : requests )
		{
			request.call();
		}
	}

	private class Request implements Callable<HttpWebResult>
	{
		private final URI uri;

		public Request( URI uri )
		{
			this.uri = uri;
		}

		@Override
		public HttpWebResult call()
		{
			HttpGet get = new HttpGet( uri );

			try(CloseableHttpResponse response = httpClient.execute( get ))
			{
				return HttpWebResult.of( response, clock.millis() );
			}
			catch( IOException e )
			{
				return HttpWebResult.of( e );
			}
		}

	}


}
