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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.loadui.testfx.matchers.VisibleNodesMatcher.visible;

/**
 * Created with IntelliJ IDEA.
 * User: Sara
 * Date: 2013-10-30
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */
@Category( IntegrationTest.class )
public class IntervalTest extends FxIntegrationTestBase
{

	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@Test
	public void TestInterval()
	{
		connect( INTERVAL ).to( FIXED_RATE_GENERATOR ).to( TABLE_LOG );

		turnKnobIn( FIXED_RATE_GENERATOR ).to( 2 );

		configureIntervalComponent();

		robot.clickPlayStopButton();

		sleep( 3000 );

		assertThat( tableRows(), is( empty() ) );

		sleep( 6000 );

		assertThat( tableRows(), is( not( empty() ) ) );

		robot.clickPlayStopButton();
		waitUntil( "#abort-requests", is( not( visible() ) ) );
	}

	private void configureIntervalComponent()
	{
		turnKnobIn( INTERVAL ).to( 3 );
		turnKnobIn( INTERVAL, 2 ).to( 2 );
		click( "Repeat" );
	}

}
