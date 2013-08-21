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
package com.eviware.loadui.test.ui.fx;

import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.FIXED_RATE_GENERATOR;
import static javafx.scene.input.KeyCode.A;
import static javafx.scene.input.KeyCode.CONTROL;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.states.SimpleWebTestState;
import javafx.scene.Node;
import org.loadui.testfx.GuiTest;
import javafx.scene.input.KeyCode;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.util.BeanInjector;

@Category( IntegrationTest.class )
public class ProjectPlaybackTest extends FxIntegrationTestBase
{
	@Test
	public void shouldPlayAndStop() throws Exception
	{
		click( ".project-playback-panel .play-button" ).sleep( 5000 );

		Collection<? extends ProjectItem> projects = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace()
				.getProjects();
		ProjectItem project = projects.iterator().next();
		assertTrue( project.isRunning() );

		click( ".project-playback-panel .play-button" ).sleep( 4000 );
		assertTrue( !project.isRunning() );
	}

	@Test
	public void shouldStopOnLimit() throws Exception
	{
		ProjectItem project = ProjectLoadedWithoutAgentsState.STATE.getProject();

		long veryHighLoad = 10_000;
		turnKnobIn( FIXED_RATE_GENERATOR ).to( veryHighLoad );

		increaseMaxConcurrentRequests();

		click( "#set-limits" ).click( "#time-limit" ).doubleClick().type( "6" ).click( "#default" )
				.sleep( 1000 ).click( ".project-playback-panel .play-button" ).sleep( 4000 );

		assertTrue( project.isRunning() );

		sleep( 9000 );
		assertTrue( !project.isRunning() );
	}

	private void increaseMaxConcurrentRequests()
	{
		click( ".web-page-runner .menu-button" ).click( "Settings" ).click( "#max-concurrent-requests" ).doubleClick().type( "1000" ).click( "#default" );
	}

	@Override
	public TestState getStartingState()
	{
		return SimpleWebTestState.STATE;
	}
}
