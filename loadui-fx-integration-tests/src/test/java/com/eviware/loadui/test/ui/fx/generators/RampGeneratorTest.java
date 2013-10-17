package com.eviware.loadui.test.ui.fx.generators;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import org.junit.Test;

/**
 * User: Sara
 */
public class RampGeneratorTest extends FxIntegrationTestBase
{


	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@Test
	public void canRunRampGenerator()
	{

	}

}
