package com.eviware.loadui.test.ui.fx.memoryLeak;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.LoadUiRobot;
import javafx.scene.input.KeyCode;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class OpenCloseProjects extends FxIntegrationTestBase
{
	int mb = 1024 * 1024;
	Runtime runtime = Runtime.getRuntime();
	List<Double> usedMemory_eachIteration = new ArrayList();

	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}


	@Test
	public void enterState() throws Exception
	{
		int count = 0;

		while( count < 6 )
		{
			connect( LoadUiRobot.Component.FIXED_RATE_GENERATOR ).to( LoadUiRobot.Component.WEB_PAGE_RUNNER );
			click( ".component-view .text-field" ).type( "win-srvmontest" );

			doubleClick( "Assertions" );
			createAssertion();

		   createChart();
		   click( "Chart 1" ).click( "Delete" ).click( "#default" );

		   click( "Design" );
			doubleClick( "#Assertions" );

			deleteComponent();
			deleteComponent();


			count = count + 1;
			System.out.println( "Count är nu: " + count );

			usedMemory_eachIteration.add( measureUsedMemory() );
			assertNoMemoryLeaked();
		}
	}

	private void createAssertion()
	{
		drag( "#componentToolBox .items" ).to( ".item-box .placeholder" );
		click( "Time Taken" ).click( "Std Dev" ).push( KeyCode.TAB ).type( "0" ).push( KeyCode.TAB ).type( "10" )
				.click( "Create" );
	}

	private void createChart()
	{
		click( "#statsTab" );
		drag( ".analysis-view #web-page-runner-1" ).to( "#chartList" );
		click( "Add" );
		drag( ".item-holder" ).to( "#chartList" );

	}

	private void deleteComponent()
	{
		click( ".component-view #menu" );
		click( "Delete" );
		click( "#default" );
	}

	private void assertNoMemoryLeaked()
	{
		System.out.println( "Used memory per iteration: " + usedMemory_eachIteration );
		if( !usedMemory_eachIteration.isEmpty() )
			assertThat( usedMemory_eachIteration.get( usedMemory_eachIteration.size() - 1 ), is( not( greaterThan( usedMemory_eachIteration.get( 0 ) * 19.50 ) ) ) );
	}

	private double measureUsedMemory()
	{
		System.gc();
		sleep( 1000 );
		System.gc();
		sleep( 1000 );
		System.gc();
		sleep( 3000 );
		return getUsedMemory();
	}

	private long getUsedMemory()
	{
		return ( runtime.totalMemory() - runtime.freeMemory() ) / mb;
	}


}
