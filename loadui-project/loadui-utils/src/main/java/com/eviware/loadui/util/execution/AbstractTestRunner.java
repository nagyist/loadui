/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.util.execution;

import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Provides a partial implementation of TestRunner, taking care of managing
 * TestExecutionTasks.
 *
 * @author dain.nilsson
 */
public abstract class AbstractTestRunner implements TestRunner
{
	public static final Logger log = LoggerFactory.getLogger( TestRunner.class );

	private final ListeningExecutorService executorService;

	@GuardedBy( "tasks" )
	private final Multimap<Phase, TestExecutionTask> tasks = Multimaps.newSetMultimap(
			new HashMap<Phase, Collection<TestExecutionTask>>(), new Supplier<Set<TestExecutionTask>>()
	{
		@Override
		public Set<TestExecutionTask> get()
		{
			return Collections.newSetFromMap( new WeakHashMap<TestExecutionTask, Boolean>() );
		}
	} );
	@GuardedBy( "runOnceTasks" )
	private final Multimap<Phase, TestExecutionTask> runOnceTasks = Multimaps.newSetMultimap(
			new HashMap<Phase, Collection<TestExecutionTask>>(), new Supplier<Set<TestExecutionTask>>()
	{
		@Override
		public Set<TestExecutionTask> get()
		{
			return new HashSet<>();
		}
	} );

	public AbstractTestRunner( ExecutorService executorService )
	{
		this.executorService = MoreExecutors.listeningDecorator( executorService );
	}

	@Override
	public void registerTask( TestExecutionTask task, Phase... phases )
	{

		for( Phase phase : phases )
		{
			synchronized( tasks )
			{
				tasks.put( phase, task );
			}
		}
	}

	@Override
	public void runTaskOnce( TestExecutionTask task, Phase phase )
	{
		synchronized( runOnceTasks )
		{
			runOnceTasks.put( phase, task );
		}
	}

	@Override
	public synchronized void unregisterTask( TestExecutionTask task, Phase... phases )
	{
		for( Phase phase : phases )
		{
			synchronized( tasks )
			{
				tasks.remove( phase, task );
			}
		}
	}

	/**
	 * Runs all the tasks for the given Phase, passing the given TestExecution to
	 * the tasks. No internal checking is done to ensure that only one Phase of
	 * one Execution is being run at one time, so care should be taken externally
	 * to always wait for a submitted Phase to complete before running another
	 * one.
	 *
	 * @param phase to run
	 * @param execution an execution
	 * @return a ListenableFuture representing pending completion of the phase
	 */
	@SuppressFBWarnings( justification = "ExecutorService.submit CAN take a null result.", value = "NP_NONNULL_PARAM_VIOLATION" )
	protected ListenableFuture<Void> runPhase( Phase phase, TestExecution execution )
	{
		return executorService.submit( new PhaseRunner( phase, execution ), null );
	}

	/**
	 * Waits for a Future to complete, logging any checked exceptions thrown.
	 * Returns true if the Future completed without throwing any checked
	 * exceptions.
	 *
	 * @param future to wait for
	 * @return true if the future returned successfully, false if an error happened
	 */
	protected boolean awaitFuture( Future<?> future )
	{
		try
		{
			future.get();
			return true;
		}
		catch( InterruptedException | ExecutionException e )
		{
			log.error( "Error invoking TestExecutionTask", e );
		}
		return false;
	}

	private class PhaseRunner implements Runnable
	{
		private final Phase phase;
		private final TestExecution execution;

		public PhaseRunner( Phase phase, TestExecution execution )
		{
			this.phase = phase;
			this.execution = execution;
		}

		@Override
		public void run()
		{
			log.debug( "Starting phase: {}", phase );
			LinkedList<Future<?>> futures = Lists.newLinkedList();


			for( TestExecutionTask task : getTasksToRun() )
			{
				futures.add( executorService.submit( new TaskRunner( task ) ) );
			}

			for( Future<?> future : futures )
			{
				awaitFuture( future );
			}
			log.debug( "Completed phase: {}", phase );
		}

		private Collection<TestExecutionTask> getTasksToRun()
		{
			Collection<TestExecutionTask> tasksToRun = new ArrayList<>();
			synchronized( runOnceTasks )
			{
				tasksToRun.addAll( runOnceTasks.removeAll( phase ) );
			}
			synchronized( tasks )
			{
				tasksToRun.addAll( tasks.get( phase ) );
			}
			return tasksToRun;
		}

		private class TaskRunner implements Runnable
		{
			private final TestExecutionTask task;

			public TaskRunner( TestExecutionTask task )
			{
				this.task = task;
			}

			@Override
			public void run()
			{
				task.invoke( execution, phase );
			}
		}
	}
}
