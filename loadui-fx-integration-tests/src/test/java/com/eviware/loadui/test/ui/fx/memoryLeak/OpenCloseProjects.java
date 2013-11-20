package com.eviware.loadui.test.ui.fx.memoryLeak;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.SimpleWebTestState;
import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class OpenCloseProjects extends FxIntegrationTestBase
{
	int mb = 1024*1024;
	Runtime runtime = Runtime.getRuntime();
	double usedMemory_start;

	@Override
	public TestState getStartingState()
	{
		return SimpleWebTestState.STATE;
	}

	@Test
	public void enterState() throws Exception
	{

		measureUsedMemory();

		int count = 0;

		while( count < 1 )
		{


			doubleClick( "Assertions" );

			drag( "#componentToolBox .items" ).to( ".item-box .placeholder" );
			click( "Time Taken" ).click( "Std Dev" ).push( KeyCode.TAB ).type( "0" ).push( KeyCode.TAB ).type( "10" )
					.click( "Create" );




			count = count + 1;
			System.out.println("Count är nu: " + count);
		}



		assertNoMemoryLeaked();
	}

	private void assertNoMemoryLeaked()
	{
		double usedMemory_end = getUsedMemory();
		assertThat( usedMemory_end, is( not( greaterThan( usedMemory_start * 1.20 ) ) ) );
	}

	private void measureUsedMemory()
	{
		System.gc();
		usedMemory_start = getUsedMemory();
	}

	private long getUsedMemory()
	{
		return (runtime.totalMemory() - runtime.freeMemory()) / mb;
	}


}
