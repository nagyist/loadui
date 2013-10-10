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
@Category( IntegrationTest.class )
public class ReportGenerationTest extends FxIntegrationTestBase
{
	HasScenarios helper = new HasScenarios();
	CanRunLoadUITests tester = new CanRunLoadUITests();

	@Test
	public void shouldGenerateReportAfterRunningATestInMainCanvas()
	{
		Set<Window> existingSwingWindows = Sets.newHashSet( Window.getWindows() );

		runTestFor( 2, SECONDS );
		tester.clickOnCreateReportButton();

		tester.assertNewWindowOpenWithReport( existingSwingWindows );

		tester.focusOnReportWindow();
		closeCurrentWindow();

		tester.assertReportFileCreated();

	}

	@Test
	public void shouldGenerateReportAfterRunningATestInDifferentScenarios()
	{
		Set<Window> existingSwingWindows = Sets.newHashSet( Window.getWindows() );

		helper.createScenario( 300, 0 );
		helper.createScenario( 600, 0 );

		helper.clickOnLinkScenarioButton();

		try
		{
			runTestFor( 2, SECONDS );
		}
		catch( RuntimeException e )
		{
			// required here because of bug LOADUI-1152
			tester.abortRequestsIfPossible();
		}

		tester.clickOnCreateReportButton();

		tester.assertNewWindowOpenWithReport( existingSwingWindows );

		tester.focusOnReportWindow();
		closeCurrentWindow();

		tester.assertReportFileCreated();

	}

	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@After
	public void cleanup()
	{
		tester.abortRequestsIfPossible();
	}
}
