package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.TimeUnit;

import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.*;
import static org.loadui.testfx.Assertions.assertNodeExists;

/**
 * Created with IntelliJ IDEA.
 * User: Sara
 * Date: 2013-10-03
 * Time: 11:12
 * To change this template use File | Settings | File Templates.
 */

@Category( IntegrationTest.class )
public class ComponentsSmoketest extends FxIntegrationTestBase
{
	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@Test
	public void canCreate_FixedRateGenerator()
	{
		create( FIXED_RATE_GENERATOR );

		assertNodeExists( ".knob" );
		assertNodeExists( ".options-slider" );
	}

	@Test
	public void canCreate_VarianceGenerator()
	{
		create( VARIANCE );

		assertNodeExists( "#terminalNode" );
		assertNodeExists( ".sliding-area" );
	}

	@Test
	public void canCreate_RandomGenerator()
	{
		create( RANDOM );

		assertNodeExists( ".knob .bounded" );
		assertNodeExists( ".sliding-area" );
	}

	@Test
	public void canCreate_RampSequenceGenerator()
	{
		create( RAMP_SEQUENCE );

		assertNodeExists( ".component-view" );

		assertNodeExists( "#sec" );
		assertNodeExists( "#min" );

	}

	@Test
	public void canCreate_RampGenerator()
	{
		create( RAMP );

		assertNodeExists( "#sec" );
		assertNodeExists( "#min" );

		/*turnKnobIn( RAMP).to( 5 );
		sleep( 500 );

		//assertNodeExists( "5 / Sec up" );

		assertNodeExists( hasLabel( startsWith("5 / Sec") ) );
		*/
	}

	@Test
	public void canCreate_UsageGenerator()
	{
		create( USAGE );

		assertNodeExists( "#menu" );
		assertNodeExists( ".check-box" );

		turnKnobIn( USAGE ).to( 20 );




	}

	@Test
	public void canCreate_FixedLoadGenerator()
	{
		create( FIXED_LOAD );

		assertNodeExists( "#compact" );
		assertNodeExists( "#inputTerminalPane" );

		click( "#off" );
		click( "#on" );
		turnKnobIn( FIXED_LOAD ).to( 1 );
		turnKnobIn( FIXED_LOAD, 2 ).to( 3 );

	}

	@Test
	public void canCreate_WebPageRunner()
	{
		create( WEB_PAGE_RUNNER );

		assertNodeExists( "#compact" );
		assertNodeExists( "#inputTerminalPane" );

		click( ".component-view .text-field" ).type( "win-srvmontest" );
		click( "Run Once" );
		click( "Reset" );


	}

	@Test
	public void canCreate_ScriptRunner()
	{
		create( SCRIPT_RUNNER );


		assertNodeExists( "#outputTerminalPane" );
		assertNodeExists( ".component-view #menuButton" );

		click( ".component-view #menuButton" );
	}

	@Test
	public void canCreate_ProcessRunner()
	{
		create( PROCESS_RUNNER );

		assertNodeExists( "#compact" );
		assertNodeExists( ".separator" );

		click( ".component-view .text-field" ).type( "Calculator" );
		click( "Run Once" );

	}

	@Test
	public void canCreate_GebRunner()
	{
		create( GEB_RUNNER );

		assertNodeExists( "#topBar" );
		assertNodeExists( ".check-box" );

		click( "#menu" );
		sleep( 100 );
		click( "Delete" );
		click( "Delete" );


	}

	@Test
	public void canCreate_Loop()
	{
		create( LOOP );

		assertNodeExists( "#terminalNode" );
		assertNodeExists( "#topBar" );

		turnKnobIn( LOOP ).to( 5 );
		click( "#menu" );
		click( "#rename-item" );
		doubleClick( ".text-field" ).type( "Loopnr3" );
		click( "Rename" );

	}

	@Test
	public void canCreate_Splitter()
	{
		create( SPLITTER );

		assertNodeExists( ".axis" );
		assertNodeExists( "#terminalNode" );


		click( "Random" );

		drag( ".component-view .thumb" ).by( 6 * 32, 0 ).drop();
		drag( ".component-view .thumb" ).by( -4 * 32, 0 ).drop();

	}

	@Test
	public void canCreate_Delay()
	{
		create( DELAY );

		assertNodeExists( "#outputTerminalPane" );
		assertNodeExists( "#topBar" );

		turnKnobIn( DELAY ).to( 600 );
		click( "Exponential" );
		click( "Gaussian" );
		turnKnobIn( DELAY, 2 ).to( 20 );


	}

	@Test
	public void canCreate_Condition()
	{
		create( CONDITION );

		assertNodeExists( "#buttonBar" );
		assertNodeExists( ".check-box" );

		click( "#menu" );
		click( "#rename-item" );
		doubleClick( ".text-field" ).type( "Condition2" );
		click( "Rename" );

		turnKnobIn( CONDITION ).to( 10 );
		turnKnobIn( CONDITION, 2 ).to( 500 );

	}

	@Test
	public void canCreate_Interval()
	{
		create( INTERVAL );

		assertNodeExists( "#outputTerminalPane" );
		assertNodeExists( "#terminalNode" );

	}

	@Test
	public void canCreate_Scheduler()
	{
		create( SCHEDULER );

		assertNodeExists( ".menu-button" );
		assertNodeExists( "#compact" );

	}

	@Test
	public void canCreate_TableLog()
	{
		create( TABLE_LOG );

		assertNodeExists( ".table-view" );
		assertNodeExists( "#outputTerminalPane" );

		click( "#menu" );
		click( "Settings" );
		push( KeyCode.TAB ).type( "100" ).push( KeyCode.ENTER );
		sleep( 300 );
		click( "#menu" );
		click( "Delete" );
		click( "Delete" );


	}

}
