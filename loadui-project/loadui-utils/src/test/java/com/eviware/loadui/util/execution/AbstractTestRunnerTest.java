package com.eviware.loadui.util.execution;

import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.model.CanvasItem;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

/**
 * @author renato
 */
public class AbstractTestRunnerTest
{
	private AbstractTestRunner testRunner;

	@Before
	public void setup()
	{
		testRunner = new AbstractTestRunner( Executors.newCachedThreadPool() )
		{
			@Override
			public TestExecution enqueueExecution( CanvasItem canvas )
			{
				return null;
			}

			@Override
			public List<TestExecution> getExecutionQueue()
			{
				return null;
			}
		};

	}

	@Test
	public void taskIsRunWhenPhaseIsTriggered() throws Exception
	{
		TestExecutionTask task1 = mock( TestExecutionTask.class );
		TestExecution execution = mock( TestExecution.class );

		testRunner.registerTask( task1, Phase.PRE_START );
		ListenableFuture<Void> future = testRunner.runPhase( Phase.PRE_START, execution );
		future.get( 5, TimeUnit.SECONDS );

		verify( task1 ).invoke( execution, Phase.PRE_START );
		verifyNoMoreInteractions( task1 );
	}

	@Test
	public void taskIsNotRunOnAnyPhaseItWasNotRegisteredOn() throws Exception
	{
		TestExecutionTask task1 = mock( TestExecutionTask.class );
		TestExecution execution = mock( TestExecution.class );

		testRunner.registerTask( task1, Phase.PRE_START );
		List<ListenableFuture<Void>> futures = Arrays.asList(
				testRunner.runPhase( Phase.START, execution ),
				testRunner.runPhase( Phase.POST_START, execution ),
				testRunner.runPhase( Phase.STOP, execution ),
				testRunner.runPhase( Phase.POST_STOP, execution ) );

		for( ListenableFuture<Void> future : futures )
			future.get( 5, TimeUnit.SECONDS );

		verifyZeroInteractions( task1 );
	}

	@Test
	public void taskCanBeRunOnlyOnceWhenRequested() throws Exception
	{
		TestExecutionTask task1 = mock( TestExecutionTask.class );
		TestExecution execution = mock( TestExecution.class );

		testRunner.runTaskOnce( task1, Phase.PRE_START );
		for( int i = 0; i < 5; i++ )
		{
			ListenableFuture<Void> future = testRunner.runPhase( Phase.PRE_START, execution );
			future.get( 5, TimeUnit.SECONDS );
		}

		verify( task1, times( 1 ) ).invoke( execution, Phase.PRE_START );
		verifyNoMoreInteractions( task1 );
	}

	@Test
	public void runOnceTaskIsNotRunOnAnyPhaseItWasNotRegisteredOn() throws Exception
	{
		TestExecutionTask task1 = mock( TestExecutionTask.class );
		TestExecution execution = mock( TestExecution.class );

		testRunner.runTaskOnce( task1, Phase.PRE_START );
		List<ListenableFuture<Void>> futures = Arrays.asList(
				testRunner.runPhase( Phase.START, execution ),
				testRunner.runPhase( Phase.POST_START, execution ),
				testRunner.runPhase( Phase.STOP, execution ),
				testRunner.runPhase( Phase.POST_STOP, execution ) );

		for( ListenableFuture<Void> future : futures )
			future.get( 5, TimeUnit.SECONDS );

		verifyZeroInteractions( task1 );
	}

}
