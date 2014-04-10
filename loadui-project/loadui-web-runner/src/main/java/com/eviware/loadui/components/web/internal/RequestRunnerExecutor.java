package com.eviware.loadui.components.web.internal;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.components.web.RequestRunner;
import com.eviware.loadui.components.web.WebRunnerStatsSender;
import com.eviware.loadui.webdata.HttpWebResponse;
import com.eviware.loadui.webdata.StreamConsumer;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RequestRunnerExecutor
{
	static final Logger log = LoggerFactory.getLogger( RequestRunnerExecutor.class );

	private final WebRunnerStatsSender statsSender;
	private final CloseableHttpAsyncClient httpClient;
	private final Clock clock;

	private StreamConsumer consumer = new StreamConsumer();
	private List<Future<?>> runningRequests = new ArrayList<>();

	private ExecutorService downloadService = Executors.newCachedThreadPool();

	public RequestRunnerExecutor( CloseableHttpAsyncClient httpClient,
											WebRunnerStatsSender statsSender,
											Clock clock )
	{
		this.httpClient = httpClient;
		this.statsSender = statsSender;
		this.clock = clock;
		httpClient.start();
	}

	public Future<Boolean> runPageRequest( final RequestRunner.PageUriRequest request )
	{
		return runRequest( request );
	}

	public ListenableFuture<List<Boolean>> runAll( Collection<RequestRunner.Request> requests ) throws Exception
	{
		List<ListenableFuture<Boolean>> futures = new ArrayList<>( requests.size() );
		for( RequestRunner.Request request : requests )
		{
			futures.add( runRequest( request ) );
		}
		return Futures.allAsList( futures );
	}

	private ListenableFuture<Boolean> runRequest( final RequestRunner.Request request )
	{
		final SettableFuture<Boolean> result = SettableFuture.create();
		final URI uri = request.getUri();
		final String resource = request.getResource();

		log.debug( "Running request: {}", resource );
		HttpGet get = new HttpGet( uri );
		statsSender.updateRequestSent( request.getResource() );

		final long startTime = clock.millis();

		Future<HttpResponse> futureResponse = httpClient.execute( get, new FutureCallback<HttpResponse>()
		{
			@Override
			public void completed( HttpResponse httpResponse )
			{
				long st = System.currentTimeMillis();
				HttpWebResponse webResponse = HttpWebResponse.of( httpResponse );
				boolean failed = request.isFailure( webResponse );
				log.debug( "It took {}ms to just know if it failed or not", System.currentTimeMillis() - st );
				if( failed )
				{
					failed( new RuntimeException( "Request reported webResponse constitutes a failure" ) );
				}
				else
				{
					webResponse.setConsumer( consumer );
					statsSender.updateLatency( resource, clock.millis() - startTime );
					consumeResponseAsync( webResponse, resource, startTime );
					result.set( true );
					log.debug( "It took {}ms to finish reacting", System.currentTimeMillis() - st );
				}
			}

			@Override
			public void failed( Exception error )
			{
				request.handleError( new RuntimeException( "Request to " + resource + " failed" ) );
				result.set( false );
			}

			@Override
			public void cancelled()
			{
				result.set( false );
			}

		} );

		result.addListener( new Runnable()
		{
			@Override
			public void run()
			{
				runningRequests.remove( result );
			}
		}, MoreExecutors.sameThreadExecutor() );

		runningRequests.add( futureResponse );
		return result;
	}

	private void consumeResponseAsync( final HttpWebResponse webResponse, final String resource, final long startTime )
	{
		downloadService.execute( new Runnable()
		{
			@Override
			public void run()
			{
				log.debug( "Completed request {} on thread {}", resource, Thread.currentThread().getName() );
				long contentLength = webResponse.getContentLength();
				statsSender.updateResponse( resource, clock.millis() - startTime, contentLength );
			}
		} );
	}

	public int cancelAll()
	{
		int runningCount = runningRequests.size();
		for( Future<?> future : runningRequests )
		{
			future.cancel( true );
		}
		return runningCount;
	}

	public void setConsumer( StreamConsumer consumer )
	{
		this.consumer = consumer;
	}
}
