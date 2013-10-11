package com.eviware.loadui.test.ui.fx.tablelog;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.eviware.loadui.test.matchers.EmptyMatcher.empty;
import static com.eviware.loadui.test.ui.fx.FxIntegrationBase.RunBlocking.NON_BLOCKING;
import static com.eviware.loadui.test.ui.fx.tablelog.TableLogTestSupport.*;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.FIXED_RATE_GENERATOR;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.TABLE_LOG;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class TableLogTest extends FxIntegrationTestBase
{
	//TODO: Needs to reset state between tests.
	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@Test
	public void should_haveNoRows_whenCreated()
	{
		// GIVEN
		create( TABLE_LOG );

		// THEN
		assertThat( tableRows(), is( empty() ) );
	}

	@Test
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
	public void should_beAbleToHandle_hugeLoadsWithoutFreezingTheGUI()
	{
		// GIVEN
		connect( FIXED_RATE_GENERATOR ).to( TABLE_LOG );

		// AND
		long veryHighLoad = 20_000;
		turnKnobIn( FIXED_RATE_GENERATOR ).to( veryHighLoad );

		// WHEN
		robot.pointAtPlayStopButton();
		runTestFor( 2, SECONDS, NON_BLOCKING );

		waitForProjectToHaveRunningAs( true );
		sleep( 1500 );

		// THEN
		assertCanRunEventInJavaFxThreadWithin( 1, SECONDS );

	}

	@After
	public void cleanup()
	{
		waitForProjectToHaveRunningAs( false );
		robot.deleteAllComponentsFromProjectView();
	}

	//TODO test log files work when the user chooses to use them

}
