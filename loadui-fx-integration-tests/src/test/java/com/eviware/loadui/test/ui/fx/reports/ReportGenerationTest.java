package com.eviware.loadui.test.ui.fx.reports;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.CanRunLoadUITests;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.HasScenarios;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.awt.*;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author renato
 */
@Category(IntegrationTest.class)
public class ReportGenerationTest extends FxIntegrationTestBase
{
	HasScenarios hasScenarios = new HasScenarios();
	CanRunLoadUITests testRunner = new CanRunLoadUITests();

	@Test
	public void shouldGenerateReportAfterRunningATestInMainCanvas()
	{
		Set<Window> existingSwingWindows = Sets.newHashSet( Window.getWindows() );

		runTestFor( 2, SECONDS );
		testRunner.clickOnCreateReportButton();

		testRunner.assertNewWindowOpenWithReport( existingSwingWindows );

		testRunner.focusOnReportWindow();
		closeCurrentWindow();

		testRunner.assertReportFileCreated();

	}

	@Test
	public void shouldGenerateReportAfterRunningATestInDifferentScenarios()
	{
		Set<Window> existingSwingWindows = Sets.newHashSet( Window.getWindows() );

		hasScenarios.createScenario( 300, 0 );
		hasScenarios.createScenario( 600, 0 );

		hasScenarios.clickOnLinkScenarioButton();

		try
		{
			runTestFor( 2, SECONDS );
		}
		catch( RuntimeException e )
		{
			// required here because of bug LOADUI-1152
			testRunner.abortRequestsIfPossible();
		}

		testRunner.clickOnCreateReportButton();

		testRunner.assertNewWindowOpenWithReport( existingSwingWindows );

		testRunner.focusOnReportWindow();
		closeCurrentWindow();

		testRunner.assertReportFileCreated();

	}

	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@After
	public void cleanup()
	{
		try
		{
			testRunner.abortRequestsIfPossible();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
