package com.eviware.loadui.test.ui.fx.reports;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.test.IntegrationTestUtils;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.collect.Sets;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.awt.*;
import java.io.File;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.time4tea.rsync.matcher.FileMatchers.isDirectory;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.loadui.testfx.matchers.EnabledMatcher.enabled;

/**
 * @author renato
 */
@Category( IntegrationTest.class )
public class ReportGenerationTest extends FxIntegrationTestBase
{

	@Test
	public void shouldGenerateReportAfterRunningATest()
	{
		Set<Window> existingSwingWindows = Sets.newHashSet( Window.getWindows() );

		runTestFor( 2, SECONDS );
		waitForNodeToDisappear( "#abort-requests" );
		waitUntil( "#summaryButton", is( enabled() ) );
		click( "#summaryButton" ).sleep( 1000 );

		Set<Window> newWindows = Sets.difference( Sets.newHashSet( Window.getWindows() ), existingSwingWindows );

		assertThat( newWindows, hasSize( 1 ) );

		Component reportWindowFocusOwner = newWindows.iterator().next().getFocusOwner();

		assertThat( reportWindowFocusOwner.toString(), containsString( "jasperreport" ) );

		Stage stage = BeanInjector.getBean( Stage.class );

		if( stage.isFocused() )
			push( KeyCode.ALT, KeyCode.TAB ).sleep( 500 );

		closeCurrentWindow();

		File resultsDir = new File( System.getProperty( LoadUI.LOADUI_HOME ), "results" );

		assertThat( resultsDir, isDirectory() );

		File projectResultsDir = IntegrationTestUtils.newestDirectoryIn( resultsDir );

		assertThat( projectResultsDir, is( notNullValue() ) );
		assertThat( projectResultsDir, isDirectory() );
		assertThat( projectResultsDir.list(), hasItemInArray( "summary.jp" ) );

	}

	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}
}
