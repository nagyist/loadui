package com.eviware.loadui.test.ui.fx.components;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.eviware.loadui.test.matchers.EmptyMatcher.empty;
import static com.eviware.loadui.test.ui.fx.tablelog.TableLogTestSupport.tableRows;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.Assertions.verifyThat;

/**
 * Created with IntelliJ IDEA.
 * User: Sara
 * Date: 2013-10-24
 * Time: 16:01
 * To change this template use File | Settings | File Templates.
 */
@Category( IntegrationTest.class )
public class DelayTest extends FxIntegrationTestBase
{

	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@After
	public void ensureProjectNotRunning() throws Exception
	{
		ensureProjectIsNotRunning();
		super.teardown();
	}

	@Test
	public void TestDelay()
	{
		connect( FIXED_RATE_GENERATOR ).to( DELAY ).to( TABLE_LOG );

		turnKnobIn( FIXED_RATE_GENERATOR ).to( 1 );
		sleep( 1000 );
		assertNodeExists( "1 / Sec" );

		turnKnobIn( DELAY ).to( 3000 );

		robot.clickPlayStopButton();

		sleep( 6000 );

		verifyThat( tableRows(), is( not( empty() ) ) );
		robot.clickPlayStopButton();

	}



}
