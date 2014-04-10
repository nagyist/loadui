package com.eviware.loadui.components.web.internal;

import com.eviware.loadui.components.web.RequestRunner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class RequestRunnerExecutor
{
	static final Logger log = LoggerFactory.getLogger( RequestRunnerExecutor.class );

	public static final int MAX_REQUEST_THREADS = 6;
	public static final long REQUEST_TIMEOUT_IN_MINUTES = 2; // minutes

	private final List<ExecutorService> executors = Collections.synchronizedList( new ArrayList<ExecutorService>() );
	private final List<Future<Boolean>> currentlyRunningRequests = Collections.synchronizedList( new ArrayList<Future<Boolean>>() );

	private final LoadingCache<Thread, ExecutorService> threadCache;


	private final ExecutorService overalExecutor = Executors.newFixedThreadPool( 50 );

	public RequestRunnerExecutor()
	{
		this.threadCache = CacheBuilder.newBuilder().build( new CacheLoader<Thread, ExecutorService>()
		{
			@Override
			public ExecutorService load( Thread _thread ) throws Exception
			{
				ExecutorService service = Executors.newFixedThreadPool( MAX_REQUEST_THREADS );
				executors.add( service );
				log.debug( "Created new Executor for running requests, now there are {} executors. Thread name: {}",
						executors.size(), _thread.getName() );
				return service;
			}
		} );
	}

	public Future<Boolean> runPageRequest( RequestRunner.PageUriRequest pageUriRequest )
	{
		return overalExecutor.submit( pageUriRequest );
	}

	public Future<List<Future<Boolean>>> runAll( Collection<RequestRunner.Request> requests ) throws Exception
	{
		return overalExecutor.submit( new RequestExecutor( requests ) );
	}

	public int cancelAll()
	{
		int cancelledTasks = 0;
		synchronized( currentlyRunningRequests )
		{
			for( Future<Boolean> future : currentlyRunningRequests )
			{
				boolean cancelled = future.cancel( true );
				if( cancelled ) cancelledTasks++;
			}
		}
		return cancelledTasks;
	}

	public List<Runnable> shutdown()
	{
		List<Runnable> cancelledRunnables = new ArrayList<>();
		synchronized( executors )
		{
			for( ExecutorService service : executors )
			{
				cancelledRunnables.addAll( service.shutdownNow() );
			}
		}
		cancelledRunnables.addAll( overalExecutor.shutdownNow() );
		return cancelledRunnables;
	}

	public List<ExecutorService> getExecutors()
	{
		synchronized( executors )
		{
			return Collections.unmodifiableList( executors );
		}
	}

	private class RequestExecutor implements Callable<List<Future<Boolean>>>
	{

		private final Collection<RequestRunner.Request> requests;

		public RequestExecutor( Collection<RequestRunner.Request> requests )
		{
			this.requests = requests;
		}

		@Override
		public List<Future<Boolean>> call() throws Exception
		{
			ExecutorService executor = threadCache.get( Thread.currentThread() );
			List<Future<Boolean>> futures = new ArrayList<>( requests.size() );
			for( RequestRunner.Request request : requests )
			{
				futures.add( executor.submit( request ) );
			}
			currentlyRunningRequests.addAll( futures );
			for( Future<Boolean> futureRequest : futures )
			{
				boolean shouldContinue = waitForCompletion( futureRequest );
				if( !shouldContinue )
					break;
			}
			currentlyRunningRequests.removeAll( futures );
			return futures;
		}

		private boolean waitForCompletion( Future<Boolean> future )
		{
			try
			{
				return future.get( REQUEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES );
			}
			catch( Exception e )
			{
				log.debug( "Problem while waiting for request to complete", e );
				return false;
			}
		}

	}

}
