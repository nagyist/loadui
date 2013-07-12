package com.eviware.loadui.test.ui.fx.tablelog;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.google.common.util.concurrent.SettableFuture;
import javafx.application.Platform;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.eviware.loadui.test.ui.fx.tablelog.TableLogTestSupport.tableRows;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.FIXED_RATE_GENERATOR;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.TABLE_LOG;
import static com.eviware.loadui.ui.fx.util.test.matchers.EmptyMatcher.empty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category( IntegrationTest.class )
public class TableLogTest extends FxIntegrationTestBase
{
	//TODO: Needs to reset state between tests.
	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@Test
	@Ignore
	public void should_haveNoRows_whenCreated()
	{
		// GIVEN
		create( TABLE_LOG );

		// THEN
		assertThat( tableRows(), is( empty() ) );
	}

	@Test
	@Ignore
	public void should_displayRows_whenGettingInput()
	{
		// GIVEN
		connect( FIXED_RATE_GENERATOR ).to( TABLE_LOG );

		// WHEN
		runTestFor( 3, SECONDS );

		// THEN
		assertThat( tableRows(), is( not( empty() ) ) );
	}

	@Test
	public void should_beAbleToHandle_hugeLoadsNicely()
	{
		// GIVEN
		connect( FIXED_RATE_GENERATOR ).to( TABLE_LOG );

		// AND
		int veryHighLoad = 20000;
		turnKnobIn( FIXED_RATE_GENERATOR ).to( veryHighLoad );

		class ThreadHolder
		{
			Thread t;
		}
		final ThreadHolder holder = new ThreadHolder();

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				holder.t = Thread.currentThread();
			}
		} );

		// WHEN
		runTestFor( 2, SECONDS );

		// THEN
		ensureJavaFXThreadIsNotLockedForMoreThan( 1, SECONDS, holder.t );
	}

	private void ensureJavaFXThreadIsNotLockedForMoreThan( int time, TimeUnit unit, Thread toKill )
	{
		final SettableFuture<Boolean> future = SettableFuture.create();
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				future.set( true );
			}
		} );
		try
		{
			System.out.println( "Making assertion now!!!!!!!!!!" );
			assertThat( future.get( time, unit ), is( true ) );
		}
		catch( InterruptedException | TimeoutException | ExecutionException e )
		{
			System.out.println( "FAILED FAILED FAILED" );
			toKill.interrupt();
			fail( "Did not get back from javaFX Thread nicely, problem was: " + e );

		}
	}


}
