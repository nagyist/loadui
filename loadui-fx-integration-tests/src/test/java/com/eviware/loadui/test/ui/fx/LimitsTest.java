package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.test.ui.fx.states.SimpleWebTestState;
import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Sara
 * Date: 2013-10-02
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public class LimitsTest extends FxIntegrationTestBase
{
	@Override
	public TestState getStartingState()
	{
		return SimpleWebTestState.STATE;
	}

	@Test
	public void shouldStopTestWhenRequestsReachLimit()
	{
		ProjectItem project = getProjectItem();



		click( "#set-limits" ).doubleClick( "#request-limit" ).type( "30" ).click( "#default" );

		robot.clickPlayStopButton();

		assertTrue( project.isRunning() );

		waitForNode( "#abort-requests" );
		waitForNodeToDisappear( "#abort-requests" );

		assertTrue( !project.isRunning() );


	}

	@Test
	public void shouldStopTestWhenTimeLimitIsReached() throws Exception
	{
		ProjectItem project = getProjectItem();

		click( "#set-limits" ).doubleClick( "#time-limit" ).type( "3" ).click( "#default" );

		robot.clickPlayStopButton();

		assertTrue( project.isRunning() );

		sleep( 4000 );

		assertTrue( !project.isRunning() );
	}




	@Test
	public void shouldStopTestWhenFailuresReachLimit()
	{
		ProjectItem project = getProjectItem();

		click( ".component-view .text-field" ).type( "sdjsdhfsaasjhsdf" );

		click( "#set-limits" ).doubleClick( "#failure-limit" ).type( "30" ).click( "#default" );

		robot.clickPlayStopButton();

		assertTrue( project.isRunning() );

		waitForNode( "#abort-requests" );
		waitForNodeToDisappear( "#abort-requests" );

		assertTrue( !project.isRunning() );

}
}