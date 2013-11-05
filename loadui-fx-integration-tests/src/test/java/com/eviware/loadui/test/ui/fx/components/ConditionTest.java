package com.eviware.loadui.test.ui.fx.components;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.eviware.loadui.test.matchers.EmptyMatcher.empty;
import static com.eviware.loadui.test.ui.fx.tablelog.TableLogTestSupport.tableRows;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.Assertions.verifyThat;

/**
 * Created with IntelliJ IDEA.
 * User: Sara
 * Date: 2013-10-28
 * Time: 13:52
 * To change this template use File | Settings | File Templates.
 */
@Category( IntegrationTest.class )
public class ConditionTest extends FxIntegrationTestBase
{

	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@Test
	public void falseCondition_shouldResultIn_noTrueOutput()
	{
		connect( FIXED_RATE_GENERATOR ).to( CONDITION ).to( TABLE_LOG );
		turnKnobIn( FIXED_RATE_GENERATOR ).to( 1 );
		click( "#arrow-button" ); click( "TriggerTimestamp" );

		robot.clickPlayStopButton();
		sleep( 5000 );

		assertThat( tableRows(), is( empty() ) );

		robot.clickPlayStopButton();
		sleep( 2000 );
	}

	@Test
	public void trueCondition_shouldResultIn_trueOutput()
	{
		connect( FIXED_RATE_GENERATOR ).to( CONDITION ).to( TABLE_LOG );
		turnKnobIn( FIXED_RATE_GENERATOR ).to( 1 );
		assertNodeExists( "1 / Sec" );

		turnKnobIn( CONDITION, 2 ).to( 99999999999999L );

		click( "#arrow-button" ); click( "TriggerTimestamp" );

		robot.clickPlayStopButton();
		sleep( 5000 );

		verifyThat( tableRows(), is( not( empty() ) ) );

		robot.clickPlayStopButton();
		sleep( 2000 );
	}
}
