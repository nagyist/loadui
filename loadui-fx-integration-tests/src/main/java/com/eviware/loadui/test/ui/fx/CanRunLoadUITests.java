package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.test.IntegrationTestUtils;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.collect.Sets;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.util.Set;

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
public class CanRunLoadUITests extends FxIntegrationBase
{

	public void focusOnReportWindow()
	{
		Stage stage = BeanInjector.getBean( Stage.class );

		if( stage.isFocused() )
			push( KeyCode.ALT, KeyCode.TAB ).sleep( 500 );
	}

	public Component getNewWindowFocusOwnerIfAny( Set<Window> existingSwingWindows )
	{
		Set<Window> newWindows = Sets.difference( Sets.newHashSet( Window.getWindows() ), existingSwingWindows );

		assertThat( newWindows, hasSize( 1 ) );

		return newWindows.iterator().next().getFocusOwner();
	}

	public void assertReportFileCreated()
	{
		File resultsDir = new File( System.getProperty( LoadUI.LOADUI_HOME ), "results" );

		assertThat( resultsDir, isDirectory() );

		File projectResultsDir = IntegrationTestUtils.newestDirectoryIn( resultsDir );

		assertNotNull( projectResultsDir );
		assertThat( projectResultsDir, isDirectory() );
		assertThat( projectResultsDir.list(), hasItemInArray( "summary.jp" ) );
	}

	public void assertNewWindowOpenWithReport( Set<Window> existingSwingWindows )
	{
		Component reportWindowFocusOwner = getNewWindowFocusOwnerIfAny( existingSwingWindows );

		assertThat( reportWindowFocusOwner.toString(), containsString( "jasperreport" ) );
	}

	public void clickOnCreateReportButton()
	{
		waitUntil( "#summaryButton", is( enabled() ) );
		click( "#summaryButton" ).sleep( 1000 );
	}

	public void abortRequestsIfPossible()
	{
		if( !findAll( "#abort-requests" ).isEmpty() )
		{
			click( "#abort-requests" );
			waitUntil( "#abort-requests", is( not( visible() ) ) );
		}
	}

}
