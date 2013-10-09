package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.junit.experimental.categories.Category;


@Category( IntegrationTest.class )
public class AssertionTest extends FxIntegrationTestBase
{
	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@Test
	public void shouldBeAbleToCreateChartFromAssertion()
	{
		click( "Assertions" );

		drag( "#componentToolBox .items" ).to( ".item-box .placeholder" );
		click( "Requests" ).click( "Per Second" ).push( KeyCode.TAB ).type( "10" ).push( KeyCode.TAB ).type( "30" )
				.click( "Create" )
				.click( "#statsTab" );
		drag( ".item-holder" ).to( "StackPane" ); //TODO need an ID for the StackPane for the test to be more robust.

	}


}
