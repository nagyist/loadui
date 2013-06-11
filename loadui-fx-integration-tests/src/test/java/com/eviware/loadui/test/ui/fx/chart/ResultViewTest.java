/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.test.ui.fx.chart;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.eviware.loadui.ui.fx.util.test.FXTestUtils.getOrFail;
import static com.eviware.loadui.ui.fx.util.test.matchers.ContainsNodesMatcher.contains;
import static com.eviware.loadui.ui.fx.util.test.matchers.VisibleNodesMatcher.visible;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Category( IntegrationTest.class )
public class ResultViewTest extends FxIntegrationTestBase
{
	public static final String ARCHIVE = "#archive-node-list";
	public static final String RECENT = "#result-node-list";
	public static final String TEST_RUNS = ".execution-view";

	@After
	public void cleanup()
	{
		if( resultsViewWindowIsOpen() )
		{
			controller.closeCurrentWindow();
		}
	}

	@Test
	public void executionLanesWorking()
	{
		runTestFor( 2, SECONDS );
		runTestFor( 2, SECONDS );

		openManageTestRunsDialog();

		assertThat( RECENT, contains( 2, TEST_RUNS ) );
		assertThat( ARCHIVE, contains( 0, TEST_RUNS ) );

		archiveResult( firstRecentTestRun() );

		assertThat( RECENT, contains( 1, TEST_RUNS ) );
		assertThat( ARCHIVE, contains( 1, TEST_RUNS ) );

		archiveResult( firstRecentTestRun() );

		assertThat( RECENT, contains( 0, TEST_RUNS ) );
		assertThat( ARCHIVE, contains( 2, TEST_RUNS ) );
	}


	@Test
	public void menuOptionsAreCorrectAndWorking()
	{
		runTestFor( 2, SECONDS );

		openManageTestRunsDialog();

		// check recent execution menu's options
		controller.click( "#result-0 #menuButton" );

		assertThat( "#open-item", is( visible() ) );
		assertThat( "#delete-item", is( visible() ) );
		assertThat( "#rename-item", is( not( visible() ) ) );

		// check if Open option works
		controller.click( "#open-item" );
		getOrFail( ".analysis-view" );
		getOrFail( "#statsTab" );

		controller.click( "#open-execution" ).sleep( 500 );

		// check archive execution menu's options
		controller.drag( "#result-0" ).to( "#archive-node-list" ).click( "#archive-0 #menuButton" );
		assertThat( "#open-item", is( visible() ) );
		assertThat( "#delete-item", is( visible() ) );
		assertThat( "#rename-item", is( visible() ) );

		// test rename function
		renameTestRun();
		MenuButton menuButton = ( MenuButton )getOrFail( "#archive-0 #menuButton" );
		assertEquals( "Renamed Execution", menuButton.textProperty().get() );

		// delete execution
		controller.click( "#archive-0 #menuButton" ).click( "#delete-item" ).click( ".confirmation-dialog #default" );
		assertThat( ARCHIVE, contains( 0, TEST_RUNS ) );
	}

	private void renameTestRun()
	{
		controller.click( "#rename-item" ).type( "Renamed Execution" ).type( KeyCode.ENTER );
	}

	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	private Node firstRecentTestRun()
	{
		return getOrFail( RECENT + " #result-0" );
	}

	private void openManageTestRunsDialog()
	{
		controller.click( "#statsTab" ).sleep( 500 ).click( "#open-execution" ).sleep( 500 );
	}

	private void archiveResult( Node result0 )
	{
		controller.drag( result0 ).to( "#archive-node-list" );
	}

	private boolean resultsViewWindowIsOpen()
	{
		return !TestFX.findAll( ".analysis-view" ).isEmpty();
	}

}
