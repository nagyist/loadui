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
package com.eviware.loadui.impl.execution;

import com.eviware.loadui.api.execution.ExecutionResult;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestState;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.execution.AbstractTestRunner;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.MoreExecutors;

import javax.annotation.CheckForNull;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.Collections.singleton;

public class TestRunnerImpl extends AbstractTestRunner implements Releasable
{
	private final ExecutorService testExecutor = MoreExecutors.listeningDecorator( Executors.newSingleThreadExecutor() );
	@CheckForNull
	private TestController currentTestController;

	public TestRunnerImpl( ExecutorService executor )
	{
		super( executor );
	}

	@Override
	public TestExecution enqueueExecution( CanvasItem canvas )
	{
		if( currentTestController != null && currentTestController.execution.getState() != TestState.COMPLETED )
			log.warn( "Programmer error!! Should not try to enqueue execution while another is running" );
		TestController controller = new TestController( new TestExecutionImpl( canvas ) );
		currentTestController = controller;
		return controller.execution;
	}

	@Override
	public List<TestExecution> getExecutionQueue()
	{
		//FIXME we should try to get rid of this method as it does not make sense since we removed execution queues
		return ImmutableList.copyOf( ( currentTestController == null ) ?
				Collections.<TestExecution>emptyList() : singleton( currentTestController.execution ) );
	}

	@Override
	public void release()
	{
		testExecutor.shutdown();
	}

	class TestController
	{
		private final TestExecutionImpl execution;
		private final Future<ExecutionResult> resultFuture;
		private boolean stopping = false;

		private TestController( TestExecutionImpl execution )
		{
			this.execution = execution;
			execution.setController( this );
			resultFuture = testExecutor.submit( new TestRunnable( execution, this ) );
		}

		public Future<ExecutionResult> getExecutionFuture()
		{
			return resultFuture;
		}

		public void initStop()
		{
			synchronized( this )
			{
				stopping = true;
				notifyAll();
			}
		}

		private void awaitStopping()
		{
			synchronized( this )
			{
				while( !stopping )
				{
					try
					{
						wait();
					}
					catch( InterruptedException e )
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	private class TestRunnable implements Callable<ExecutionResult>
	{
		private final TestExecutionImpl execution;
		private final TestController controller;

		public TestRunnable( TestExecutionImpl execution, TestController controller )
		{
			this.execution = execution;
			this.controller = controller;
		}

		@Override
		public ExecutionResult call()
		{
			log.debug( "Starting TestExecution: {}", execution );
			execution.setState( TestState.STARTING );
			awaitFuture( runPhase( Phase.PRE_START, execution ) );
			execution.setState( TestState.RUNNING );
			awaitFuture( runPhase( Phase.START, execution ) );
			awaitFuture( runPhase( Phase.POST_START, execution ) );

			controller.awaitStopping();

			execution.setState( TestState.STOPPING );
			awaitFuture( runPhase( Phase.PRE_STOP, execution ) );
			awaitFuture( runPhase( Phase.STOP, execution ) );
			awaitFuture( runPhase( Phase.POST_STOP, execution ) );
			execution.setState( TestState.COMPLETED );
			log.debug( "Completed TestExecution: {}", execution );

			//TODO: Create ExecutionResult
			return null;
		}

	}

}
