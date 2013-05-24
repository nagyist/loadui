package com.eviware.loadui.test.ui.fx;

import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.FIXED_RATE_GENERATOR;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.TABLE_LOG;
import static com.eviware.loadui.ui.fx.util.test.TestFX.findAll;
import static javafx.util.Duration.seconds;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javafx.scene.Node;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.ComponentHandle;

@Category( IntegrationTest.class )
public class TableLogTest extends FxIntegrationTest
{
	@Override
	TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@Test
	public void shouldHaveRows() throws Exception
	{
		ComponentHandle fixedRate = robot.createComponent( FIXED_RATE_GENERATOR );
		ComponentHandle tableLog = robot.createComponent( TABLE_LOG );

		fixedRate.connectTo( tableLog );

		assertTrue( numberOfTableRows().isEmpty() );

		robot.runTestFor( seconds( 3 ) );

		assertFalse( numberOfTableRows().isEmpty() );
	}

	//// Private implementation methods

	private Set<Node> numberOfTableRows()
	{
		return findAll( ".table-row-cell" );
	}
}
