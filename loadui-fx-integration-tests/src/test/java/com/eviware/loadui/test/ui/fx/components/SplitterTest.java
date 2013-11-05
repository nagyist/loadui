package com.eviware.loadui.test.ui.fx.components;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import javafx.scene.Node;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Iterator;
import java.util.Set;

import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.loadui.testfx.Assertions.verifyThat;
import static org.loadui.testfx.Matchers.hasLabel;

/**
 * Created with IntelliJ IDEA.
 * User: Sara
 * Date: 2013-10-29
 * Time: 13:50
 * To change this template use File | Settings | File Templates.
 */
@Category( IntegrationTest.class )
public class SplitterTest extends FxIntegrationTestBase
{

	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;

	}

	@Ignore
	@Test
	public void TestSplitter()
	{
		connect( FIXED_RATE_GENERATOR ).to( SPLITTER ).to( LOOP, LOOP );
		turnKnobIn( FIXED_RATE_GENERATOR ).to( 1 );

		runTestFor( 6, SECONDS );

		Set<Node> allLoops = findAll( ".loop" );
		Iterator<Node> tableLogsIterator = allLoops.iterator();
		Node loop1 = tableLogsIterator.next();
		Node loop2 = tableLogsIterator.next();

		System.out.println( "loop1: " + loop1 + " loop2: " + loop2 );

		verifyThat( findAll( ".cell .text", loop1 ), hasLabel( "3" ) );
		verifyThat( findAll( ".cell .text", loop2 ), hasLabel( "2" ) );




	}



}



