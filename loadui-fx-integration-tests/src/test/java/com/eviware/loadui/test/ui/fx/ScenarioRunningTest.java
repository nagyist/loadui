package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ScenarioCreatedState;
import com.eviware.loadui.util.execution.TestExecutionUtils;
import com.eviware.loadui.util.test.TestUtils;
import com.google.code.tempusfugit.temporal.Timeout;
import javafx.scene.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.loadui.testfx.matchers.EnabledMatcher;

import java.util.concurrent.Callable;

import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.fail;
import static org.loadui.testfx.Assertions.verifyThat;

/**
 * Author: maximilian.skog
 * Date: 2013-10-30
 * Time: 10:29
 */
@Category(IntegrationTest.class)
public class ScenarioRunningTest extends SimpleWebTestBase
{
	HasScenarios hasScenarios = new HasScenarios();
	CanRunLoadUITests testRunner = new CanRunLoadUITests();
	SceneItem scenario = null;

	@Before
	public void init()
	{
		scenario = hasScenarios.ensureScenarioIsLinkedIs( true );
	}

	@Override
	public void cleanup() throws Exception
	{
		try
		{
			testRunner.abortRequestsIfPossible();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		if( scenario != null && scenario.isRunning() )
		{
			if( exists( "#abort-requests" ) )
			{
				clickOnAbortButton();
			}

			scenario = hasScenarios.ensureScenarioIsLinkedIs( true );

			stopScenario();

			try
			{
				waitForNodeToDisappear( "#abort-requests", timeout( seconds( 2 ) ) );
			}
			catch( RuntimeException e )
			{
				clickOnAbortButton();
				testRunner.waitForBlockingTaskToComplete();
			}

		}

	}

	@Test
	public void ShouldStopScenarioWhenPressingExitProjectButton() throws Exception
	{
		//When
		hasScenarios.startSingleScenario( scenario );
		sleep( 500 );
		click( "#closeProjectButton" );
		waitForBlockingTaskToCompleteOrAbort();
		testRunner.waitForBlockingTaskToComplete();

		waitAndClickIfExists( "#cancel" );

		//Then
		verifyScenarioIsRunningIs( false );
		waitAndVerifyThatExecutionStoppedWithinFiveSecounds();

	}

	private void waitAndVerifyThatExecutionStoppedWithinFiveSecounds()
	{
		waitUntil( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return TestExecutionUtils.isExecutionRunning();
			}
		}, is( false ), 5 );
	}

	@Test
	public void shouldNotWaitMoreThanFiveSecondsWhenStoppingEmptyScenario() throws Exception
	{
		//Given
		scenario = hasScenarios.ensureScenarioIsLinkedIs( true );

		//When
		hasScenarios.startSingleScenario( scenario );
		sleep( 500 );
		stopScenario();

		//Then
		assertNodeDisappeared( "#abort-requests", timeout( seconds( 5 ) ) );

	}

	@Test
	public void shouldTrulyStopScenarioAndExecutionWhenAbortingCloseBlockingTask() throws Exception
	{
		//Given
		scenario = hasScenarios.ensureScenarioIsLinkedIs( true );


		//When
		hasScenarios.startSingleScenario( scenario );
		waitUntilPlayButtonIsEnabled();
		stopScenario();

		//Then
		waitAndClickIfExists( "#abort-requests" );

		testRunner.waitForBlockingTaskToComplete();
		waitAndVerifyThatExecutionStoppedWithinFiveSecounds();
		verifyScenarioIsRunningIs( false );
	}

	public void waitAndClickIfExists( final String query )
	{
		TestUtils.awaitConditionSilent( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return exists( query );
			}
		}, 2 );

		if(exists( query ))
		{
			click( query );
		}
	}

	private void waitUntilPlayButtonIsEnabled()
	{
		Node playbutton = find( ".mini-playback-panel  .play-button" );
		waitUntil( playbutton, EnabledMatcher.enabled() );
	}

	private void stopScenario()
	{
		click( ".mini-playback-panel  .play-button" );
	}

	private void verifyScenarioIsRunningIs( Boolean bool )
	{
		verifyThat( "Expected that scenario isRunning is " + bool, scenario.isRunning(), is( bool ) );
	}


	private void waitForBlockingTaskToCompleteOrAbort()
	{
		try
		{
			waitForNodeToDisappear( ".task-progress-indicator", timeout( seconds( 10 ) ) );
		}
		catch( RuntimeException e )
		{
			clickOnAbortButton();
			assertNodeDisappeared( ".task-progress-indicator", timeout( seconds( 5 ) ) );
		}

	}

	private void assertNodeDisappeared( String query, Timeout timeout )
	{
		try
		{
			waitForNodeToDisappear( query, timeout );
		}
		catch( RuntimeException e )
		{
			fail( "Expected node that matched query " + query + " to disappear, captured screenshot: " + captureScreenshot().getAbsolutePath() );
		}
	}


	@Override
	public TestState getStartingState()
	{
		return ScenarioCreatedState.STATE;
	}

}
