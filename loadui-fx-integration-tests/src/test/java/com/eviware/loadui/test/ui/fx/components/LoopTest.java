package com.eviware.loadui.test.ui.fx.components;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.*;

/**
 * Created with IntelliJ IDEA.
 * User: Sara
 * Date: 2013-10-30
 * Time: 14:17
 * To change this template use File | Settings | File Templates.
 */
@Category( IntegrationTest.class )
public class LoopTest extends FxIntegrationTestBase
{
	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@Test
	public void TestLoop()
	{
		connect( FIXED_RATE_GENERATOR ).to( LOOP ).to( WEB_PAGE_RUNNER );



	}



}
