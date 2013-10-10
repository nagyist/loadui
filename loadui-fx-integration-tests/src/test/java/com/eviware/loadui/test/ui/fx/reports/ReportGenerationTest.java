package com.eviware.loadui.test.ui.fx.reports;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.test.IntegrationTestUtils;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.HasScenarios;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.collect.Sets;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.awt.*;
import java.io.File;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertNotNull;
import static net.time4tea.rsync.matcher.FileMatchers.isDirectory;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.loadui.testfx.matchers.EnabledMatcher.enabled;
import static org.loadui.testfx.matchers.VisibleNodesMatcher.visible;

/**
 * @author renato
 */
@Category( IntegrationTest.class )
public class ReportGenerationTest extends FxIntegrationTestBase
{
	HasScenarios helper = new HasScenarios();

	@Test
	public void shouldGenerateReportAfterRunningATestInMainCanvas()
	{
		Set<Window> existingSwingWindows = Sets.newHashSet( Window.getWindows() );

		runTestFor( 2, SECONDS );
		clickOnCreateReportButton();

		assertNewWindowOpenWithReport( existingSwingWindows );

		focusOnReportWindow();
		closeCurrentWindow();

		assertReportFileCreated();

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
			abortRequestsIfPossible();
		}

		clickOnCreateReportButton();

		assertNewWindowOpenWithReport( existingSwingWindows );

		focusOnReportWindow();
		closeCurrentWindow();

		assertReportFileCreated();

	}

	private void focusOnReportWindow()
	{
		Stage stage = BeanInjector.getBean( Stage.class );

		if( stage.isFocused() )
			push( KeyCode.ALT, KeyCode.TAB ).sleep( 500 );
	}

	private Component getNewWindowFocusOwnerIfAny( Set<Window> existingSwingWindows )
	{
		Set<Window> newWindows = Sets.difference( Sets.newHashSet( Window.getWindows() ), existingSwingWindows );

		assertThat( newWindows, hasSize( 1 ) );

		return newWindows.iterator().next().getFocusOwner();
	}

	private void assertReportFileCreated()
	{
		File resultsDir = new File( System.getProperty( LoadUI.LOADUI_HOME ), "results" );

		assertThat( resultsDir, isDirectory() );

		File projectResultsDir = IntegrationTestUtils.newestDirectoryIn( resultsDir );

		assertNotNull( projectResultsDir );
		assertThat( projectResultsDir, isDirectory() );
		assertThat( projectResultsDir.list(), hasItemInArray( "summary.jp" ) );
	}

	private void assertNewWindowOpenWithReport( Set<Window> existingSwingWindows )
	{
		Component reportWindowFocusOwner = getNewWindowFocusOwnerIfAny( existingSwingWindows );

		assertThat( reportWindowFocusOwner.toString(), containsString( "jasperreport" ) );
	}

	private void clickOnCreateReportButton()
	{
		waitUntil( "#summaryButton", is( enabled() ) );
		click( "#summaryButton" ).sleep( 1000 );
	}

	private void abortRequestsIfPossible()
	{
		if( !findAll( "#abort-requests" ).isEmpty() )
		{
			click( "#abort-requests" );
			waitUntil( "#abort-requests", is( not( visible() ) ) );
		}
	}

	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@After
	public void cleanup()
	{
		abortRequestsIfPossible();
	}
}
