package com.eviware.loadui.components.web.internal;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.components.web.RequestRunner;
import com.eviware.loadui.components.web.WebRunnerStatsSender;
import com.eviware.loadui.webdata.HttpWebResponse;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncByteConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class RequestRunnerExecutor
{
	static final Logger log = LoggerFactory.getLogger( RequestRunnerExecutor.class );

	private final WebRunnerStatsSender statsSender;
	private final CloseableHttpAsyncClient httpClient;
	private final Clock clock;

	private List<Future<?>> runningRequests = new ArrayList<>();

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

	private ListenableFuture<Boolean> runRequest( final RequestRunner.Request request ) //TODO: Refactor!
	{
		final SettableFuture<Boolean> result = SettableFuture.create();
		final URI uri = request.getUri();
		final String resource = request.getResource();

		log.debug( "Running request: {}", resource );
		statsSender.updateRequestSent( request.getResource() );

		final long startTime = clock.millis();

		Future<HttpWebResponse> futureResponse = httpClient.execute(
				HttpAsyncMethods.createGet( uri ),
				new AsyncByteConsumer<HttpWebResponse>()
				{
					private volatile HttpWebResponse response;
					private boolean hasFirstByte = false;
					private AtomicLong length = new AtomicLong( 0 );

					@Override
					protected void onByteReceived( ByteBuffer buf, IOControl ioctrl )
							throws IOException
					{
						if( !hasFirstByte )
						{
							long latency = clock.millis() - startTime;
							statsSender.updateLatency( resource, latency );
							log.debug( "It took {}ms to to get the first byte for {}", latency, resource );
						}
						hasFirstByte = true;

						long bytesRead = 0;
						while( buf.position() < buf.limit() )
						{
							buf.get();
							bytesRead++;
						}
						length.addAndGet( bytesRead );
					}

					@Override
					protected void onResponseReceived( HttpResponse response )
							throws HttpException, IOException
					{
						log.debug( "Called onResponseReceived for {}", resource );
						this.response = HttpWebResponse.of( response, length );
					}

					@Override
					protected HttpWebResponse buildResult( HttpContext context )
							throws Exception
					{
						return response;
					}
				},
				new FutureCallback<HttpWebResponse>()
				{
					@Override
					public void completed( HttpWebResponse webResponse )
					{
						log.debug( "Completed request for {}", resource );
						boolean failed = request.isFailure( webResponse );
						if( failed )
						{
							failed( new RuntimeException( "Request reported webResponse constitutes a failure" ) );
						}
						else
						{
							long contentLength = webResponse.getContentLength();
							statsSender.updateResponse( resource, clock.millis() - startTime, contentLength );
							result.set( true );
						}
					}

					@Override
					public void failed( Exception error ) //TODO: Arguments not used
					{
						request.handleError( new RuntimeException( "Request to " + resource + " failed" ) );
						result.set( false );
					}

					@Override
					public void cancelled()
					{
						result.set( false );
					}

				}
		);

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

	public int cancelAll()
	{
		int runningCount = runningRequests.size();
		for( Future<?> future : runningRequests )
		{
			future.cancel( true );
		}
		return runningCount;
	}

}
