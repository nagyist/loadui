package com.eviware.loadui.test.ui.fx.tutorials;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.FIXED_RATE_GENERATOR;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.WEB_PAGE_RUNNER;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.Matchers.hasLabel;

/**
 * Created with IntelliJ IDEA.
 * User: Sara
 * Date: 2013-10-17
 * Time: 12:01
 * To change this template use File | Settings | File Templates.
 */

@Category( IntegrationTest.class )
public class FirstLoadTest extends FxIntegrationTestBase

{
	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@Test
	public void canCreate_WebPageRunner()
	{
		connect( FIXED_RATE_GENERATOR ).to( WEB_PAGE_RUNNER );

		assertNodeExists( ".connection-view" );

		click( ".component-view .text-field" ).type( "win-srvmontest" );
		click( "Run Once" );

		robot.clickPlayStopButton();

		turnKnobIn( FIXED_RATE_GENERATOR ).to( 100 );

		assertNodeExists( hasLabel( "100 / Sec" ) );

		sleep( 3000 );

		robot.clickPlayStopButton();

		sleep( 2000 );





	}




}




