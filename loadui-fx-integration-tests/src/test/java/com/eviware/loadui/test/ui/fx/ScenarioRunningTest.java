package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ScenarioCreatedState;
import com.google.code.tempusfugit.temporal.Timeout;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
@Category( IntegrationTest.class )
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

			click( ".mini-playback-panel  .play-button" );

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
		pressExitProject();
		click( "Cancel" );

		//Then
		verifyThat( "Expected scenario to not be running", scenario.isRunning(), is( false ) );

	}


	@Test
	public void ShouldNotWaitMoreThanFiveSecondsWhenStoppingEmptyScenario() throws Exception
	{
		//Given
		scenario = hasScenarios.ensureScenarioIsLinkedIs( true );

		//When
		hasScenarios.startSingleScenario( scenario );
		click( ".mini-playback-panel  .play-button" );
		System.out.println( "clicked stop" );

		//Then
		AssertNodeDisappeared( "#abort-requests", timeout( seconds( 5 ) ) );

	}

	private void AssertNodeDisappeared( String query, Timeout timeout )
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

	private void pressExitProject()
	{
		click( "#closeProjectButton" );
		testRunner.waitForBlockingTaskToComplete();
	}


	@Override
	public TestState getStartingState()
	{
		return ScenarioCreatedState.STATE;
	}

}
