package com.eviware.loadui.impl.model.canvas.project;

import com.eviware.loadui.api.model.SceneItem;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author renato
 */
public class CanvasCompleteAwaiterTest
{
	CanvasCompleteAwaiter awaiter;
	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 2 );


	@Test( expected = RuntimeException.class )
	public void rethrowsExceptionIfSceneThrowsExceptionWhenAskedIfComplete() throws Exception
	{
		SceneItem scene1 = mockSceneThrowing( new RuntimeException() );
		Collection<? extends SceneItem> scenes = Arrays.asList( scene1 );

		awaiter = new CanvasCompleteAwaiter( scenes, scheduler );

		awaiter.startAndWait( 10L, TimeUnit.SECONDS );
	}

	@Test( expected = TimeoutException.class )
	public void timesOutWhenSceneIsNotCompletedWithinTimeout() throws Exception
	{
		Collection<? extends SceneItem> scenes = Arrays.asList( mockScene( true, false ) );

		awaiter = new CanvasCompleteAwaiter( scenes, scheduler );

		awaiter.startAndWait( 100, TimeUnit.MILLISECONDS );
	}

	@Test( expected = TimeoutException.class )
	public void timesOutWhenNotAllScenesAreCompletedWithinTimeout() throws Exception
	{
		Collection<? extends SceneItem> scenes = Arrays.asList(
				mockScene( true, true ),
				mockScene( true, true ),
				mockScene( true, false ),
				mockScene( true, true )
		);

		awaiter = new CanvasCompleteAwaiter( scenes, scheduler );

		awaiter.startAndWait( 100, TimeUnit.MILLISECONDS );
	}

	@Test
	public void terminatesImmediatelyWhenNoScenesExist() throws Exception
	{
		Collection<? extends SceneItem> scenes = Arrays.asList();

		awaiter = new CanvasCompleteAwaiter( scenes, scheduler );

		awaiterTerminatesImmediately();
	}

	@Test
	public void terminatesImmediatelyWhenAllScenesAreAlreadyCompletedFromStart() throws Exception
	{
		Collection<? extends SceneItem> scenes = Arrays.asList(
				mockScene( true, true ),
				mockScene( true, true ),
				mockScene( true, true ) );

		awaiter = new CanvasCompleteAwaiter( scenes, scheduler );

		awaiterTerminatesImmediately();
	}

	@Test
	public void terminatesImmediatelyWhenAllScenesAreNotRunning() throws Exception
	{
		Collection<? extends SceneItem> scenes = Arrays.asList(
				mockScene( false, false ),
				mockScene( false, false ) );

		awaiter = new CanvasCompleteAwaiter( scenes, scheduler );

		awaiterTerminatesImmediately();
	}

	@Test
	public void awaitsForScenesToCompleteWithinTimeLimit() throws Exception
	{
		SceneItem scene = mock( SceneItem.class );
		when( scene.isRunning() ).thenReturn( true );
		when( scene.isCompleted() ).thenReturn( false ).thenReturn( false ).thenReturn( true );

		awaiter = new CanvasCompleteAwaiter( Arrays.asList( scene ), scheduler );
		awaiter.startDelay = 0L;
		awaiter.period = 50L;
		final List<Exception> exceptions = Lists.newArrayList();
		final CountDownLatch latch = new CountDownLatch( 1 );

		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					awaiter.startAndWait( 200, TimeUnit.MILLISECONDS );
				}
				catch( Exception e )
				{
					exceptions.add( e );
				}
				finally
				{
					latch.countDown();
				}
			}
		} ).start();

		boolean done = latch.await( 1, TimeUnit.SECONDS );
		assertTrue( "Thread running awaiter completed within timeout", done );

		Thread.sleep( 100L ); // allow task to run again in case it failed to be canceled

		assertThat( exceptions, is( empty() ) );
		verify( scene, times( 3 ) ).isCompleted();
	}

	@Test
	public void executorCanBeUsedAgainAfterBeingUsedForAwaiter() throws Exception
	{
		Collection<? extends SceneItem> scenes = Arrays.asList();

		awaiter = new CanvasCompleteAwaiter( scenes, scheduler );

		awaiterTerminatesImmediately();

		assertCanRunTaskOnScheduler();
	}

	@Test
	public void executorCanBeUsedAgainAfterAwaiterTaskThrowsException() throws Exception
	{
		SceneItem scene = mockSceneThrowing( new RuntimeException() );
		awaiter = new CanvasCompleteAwaiter( Arrays.asList( scene ), scheduler );

		try
		{
			awaiter.startAndWait( 1, TimeUnit.SECONDS );
			fail( "No Exception thrown by Awaiter" );
		}
		catch( RuntimeException e )
		{
			// ok to throw
		}

		assertCanRunTaskOnScheduler();
	}

	@Test
	public void anyTaskCurrentlyRunningInTheSchedulerIsNotAffectedByAwaiterTaskTimeout() throws Exception
	{
		Collection<? extends SceneItem> scenes = Arrays.asList( mockScene( true, false ) );
		awaiter = new CanvasCompleteAwaiter( scenes, scheduler );

		CountDownLatch latch = runOnSchedulerGettingLatchToWait( taskThatLasts500Millis() );

		try
		{
			awaiter.startAndWait( 100, TimeUnit.MILLISECONDS );
			fail( "Should timeout" );
		}
		catch( TimeoutException e )
		{
			// ok to timeout
		}

		latch.await( 1, TimeUnit.SECONDS );
	}

	private SceneItem mockScene( boolean isRunning, boolean isCompleted )
	{
		SceneItem scene = mock( SceneItem.class );
		when( scene.isRunning() ).thenReturn( isRunning );
		when( scene.isCompleted() ).thenReturn( isCompleted );
		return scene;
	}

	private SceneItem mockSceneThrowing( Throwable throwable )
	{
		SceneItem scene1 = mock( SceneItem.class );
		when( scene1.isRunning() ).thenThrow( throwable );
		when( scene1.isCompleted() ).thenThrow( throwable );
		return scene1;
	}

	private void awaiterTerminatesImmediately() throws Exception
	{
		final long TIME_LIMIT = 400L;
		long startT = System.currentTimeMillis();

		awaiter.startAndWait( 10, TimeUnit.SECONDS );

		assertThat( System.currentTimeMillis(), lessThan( startT + TIME_LIMIT ) );
	}

	private void assertCanRunTaskOnScheduler() throws InterruptedException
	{
		runOnSchedulerGettingLatchToWait().await( 1, TimeUnit.SECONDS );
	}

	private CountDownLatch runOnSchedulerGettingLatchToWait( final Runnable... runnables )
	{
		final CountDownLatch latch = new CountDownLatch( 1 );
		scheduler.schedule( new Runnable()
		{
			@Override
			public void run()
			{
				for( Runnable toRun : runnables ) toRun.run();
				latch.countDown();
			}
		}, 0, TimeUnit.SECONDS );
		return latch;
	}

	private Runnable taskThatLasts500Millis()
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep( 500 );
				}
				catch( InterruptedException e )
				{
					throw new RuntimeException( e );
				}
			}
		};
	}

}
