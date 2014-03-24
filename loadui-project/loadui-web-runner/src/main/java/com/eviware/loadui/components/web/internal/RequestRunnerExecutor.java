package com.eviware.loadui.components.web.internal;

import com.eviware.loadui.components.web.RequestRunner;
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

	public static final int MAX_REQUEST_THREADS = 5;
	public static final long REQUEST_TIMEOUT_IN_MINUTES = 2; // minutes

	private final List<ExecutorService> executors = Collections.synchronizedList( new ArrayList<ExecutorService>() );
	private final List<Future<Void>> currentlyRunningRequests = Collections.synchronizedList( new ArrayList<Future<Void>>() );

	private final ThreadLocal<ExecutorService> localRequestsExecutor =
			new ThreadLocal<ExecutorService>()
			{

				@Override
				protected ExecutorService initialValue()
				{
					ExecutorService service = Executors.newFixedThreadPool( MAX_REQUEST_THREADS );
					executors.add( service );
					log.debug( "Created new Executor for running requests, now there are {} executors",
							executors.size() );
					return service;
				}

			};

	private final ExecutorService overalExecutor = Executors.newCachedThreadPool();

	public Future<List<Future<Void>>> runAll( Collection<RequestRunner.Request> requests ) throws Exception
	{
		synchronized( overalExecutor )
		{
			return overalExecutor.submit( new RequestExecutor( requests ) );
		}
	}

	public int cancelAll()
	{
		int cancelledTasks = 0;
		synchronized( currentlyRunningRequests )
		{
			for( Future<Void> future : currentlyRunningRequests )
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

	private class RequestExecutor implements Callable<List<Future<Void>>>
	{

		private final Collection<RequestRunner.Request> requests;

		public RequestExecutor( Collection<RequestRunner.Request> requests )
		{
			this.requests = requests;
		}

		@Override
		public List<Future<Void>> call() throws Exception
		{
			ExecutorService executor = localRequestsExecutor.get();
			List<Future<Void>> futures = new ArrayList<>( requests.size() );
			for( RequestRunner.Request request : requests )
			{
				futures.add( executor.submit( request ) );
			}
			currentlyRunningRequests.addAll( futures );
			for( Future<Void> futureRequest : futures )
			{
				waitForCompletion( futureRequest );
			}
			currentlyRunningRequests.removeAll( futures );
			return futures;
		}

		private void waitForCompletion( Future<Void> future )
		{
			try
			{
				future.get( REQUEST_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES );
			}
			catch( Exception e )
			{
				log.debug( "Problem while waiting for request to complete", e );
			}
		}

	}

}
