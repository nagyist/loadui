package com.eviware.loadui.components.web.internal;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.components.web.RequestRunner;
import com.eviware.loadui.components.web.WebRunnerStatsSender;
import com.eviware.loadui.webdata.HttpWebResponse;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class RequestRunnerExecutor
{
	static final Logger log = LoggerFactory.getLogger( RequestRunnerExecutor.class );

	private final WebRunnerStatsSender statsSender;
	private final CloseableHttpAsyncClient httpClient;
	private final Clock clock;
	private final Set<Future<HttpWebResponse>> runningRequests = Sets.newConcurrentHashSet();

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
		final String resource = request.getResource();

		statsSender.updateRequestSent( request.getResource() );

		final long startTime = clock.millis();

		final Future<HttpWebResponse> futureResponse = httpClient.execute(
				HttpAsyncMethods.createGet( request.getUri() ),
				new WebRunnerByteConsumer( startTime, resource, statsSender, clock ),
				new HttpWebResponseCallback( startTime, resource, statsSender, clock, request, result )
		);

		result.addListener( new Runnable()
		{
			@Override
			public void run()
			{
				boolean removedOk = runningRequests.remove( futureResponse );
				if( !removedOk ) log.warn( "Failed to remove running request" );
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
