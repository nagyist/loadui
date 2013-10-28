package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.loadui.testfx.Assertions.verifyThat;
import static org.loadui.testfx.matchers.ContainsNodesMatcher.contains;


@Category( IntegrationTest.class )
public class AssertionTest extends FxIntegrationTestBase
{

	private final String chart = ".main-chart-group";

	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@Test
	public void shouldBeAbleToCreateChartFromAssertion()
	{
		// GIVEN
		openAssertionsPanel();
		createAssertion();
		closeAssertionsPanel();

		// WHEN
		createChartFromAssertion();

		// THEN
		verifyThat( "#chartList", contains(1, chart ) );
	}

	private void createChartFromAssertion()
	{
		click( "#statsTab" ).drag( ".item-holder" ).to( "#chartList" );
	}

	private void closeAssertionsPanel()
	{
		doubleClick( "#toggleInspector" );
	}

	private void createAssertion()
	{
		drag( "#componentToolBox .items" ).to( ".item-box .placeholder" );

		click( "Requests" ).click( "Per Second" ).push( KeyCode.TAB ).type( "10" ).push( KeyCode.TAB ).type( "30" )
				.click( "Create" );
	}

	private void openAssertionsPanel()
	{
		click( "Assertions" ).drag(".inspector-view .tab-header-background").by(0, -400)
				.drop();
	}
}
