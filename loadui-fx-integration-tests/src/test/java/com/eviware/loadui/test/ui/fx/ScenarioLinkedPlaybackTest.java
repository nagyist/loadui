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

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ScenarioCreatedState;
import com.google.code.tempusfugit.concurrency.IntermittentTestRunner;
import com.google.code.tempusfugit.concurrency.annotations.Intermittent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.FIXED_RATE_GENERATOR;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.WEB_PAGE_RUNNER;
import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
import static junit.framework.Assert.assertFalse;

@Category( IntegrationTest.class )
@RunWith( IntermittentTestRunner.class )
@Intermittent( repetition = 3 )
public class ScenarioLinkedPlaybackTest extends SimpleWebTestBase
{
	HasScenarios hasScenarios = new HasScenarios();
	CanRunLoadUITests testRunner = new CanRunLoadUITests();


	@Before
	@Override
	public void setup() throws Exception
	{
		super.setup();
		ensureProjectIsNotRunning();
	}

	@After
	@Override
	public void teardown() throws Exception
	{
		try
		{
			testRunner.abortRequestsIfPossible();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		hasScenarios.exitScenarioIfPossible();
		ensureProjectIsNotRunning();
		setTestTimeLimitTo( 0 );
		super.teardown();
	}

	@Test
	public void shouldFollowProject_when_linked() throws Exception
	{
		SceneItem scenario = hasScenarios.ensureScenarioIsLinkedIs( true );

		for( int i = 0; i < 3; i++ )
		{
			robot.clickPlayStopButton();

			waitOrTimeout( new IsCanvasRunning( scenario, true ), timeout( seconds( 2 ) ) );

			robot.clickPlayStopButton();
			testRunner.letGuiReactToProjectStopping();

			waitOrTimeout( new IsCanvasRunning( scenario, false ), timeout( seconds( 2 ) ) );
		}
	}

	@Test
	public void shouldNotFollowProject_when_unLinked() throws Exception
	{
		SceneItem scenario = hasScenarios.ensureScenarioIsLinkedIs( false );

		for( int i = 0; i < 4; i++ )
		{
			robot.clickPlayStopButton();
			testRunner.letGuiReactToProjectStopping();

			assertFalse( scenario.isRunning() );
		}
	}

	@Test
	public void shouldStopOnLimit_when_isLinked() throws Exception
	{
		ProjectItem project = getProjectItem();
		hasScenarios.ensureScenarioIsLinkedIs( true );

		setTestTimeLimitTo( 2 );
		robot.clickPlayStopButton();

		waitOrTimeout( new IsCanvasRunning( project, true ), timeout( seconds( 2 ) ) );

		waitOrTimeout( new IsCanvasRunning( project, false ), timeout( seconds( 4 ) ) );

		testRunner.letGuiReactToProjectStopping();
	}

	@Test
	public void shouldStopOnLimit_when_isLinked_AndThereAreComponents_InProjectAndInScenario() throws Exception
	{
		ProjectItem project = getProjectItem();
		setTestTimeLimitTo( 2 );
		hasScenarios.ensureScenarioIsLinkedIs( true );

		connect( FIXED_RATE_GENERATOR ).to( WEB_PAGE_RUNNER );
		setWebPageRunnerUrlTo( VALID_URL_TO_HIT_ON_TESTS );

		hasScenarios.enterScenario();

		robot.resetPredefinedPoints();
		connect( FIXED_RATE_GENERATOR ).to( WEB_PAGE_RUNNER );
		setWebPageRunnerUrlTo( VALID_URL_TO_HIT_ON_TESTS );

		robot.clickPlayStopButton();

		waitOrTimeout( new IsCanvasRunning( project, true ), timeout( seconds( 2 ) ) );

		waitOrTimeout( new IsCanvasRunning( project, false ), timeout( seconds( 4 ) ) );

		testRunner.letGuiReactToProjectStopping();
	}

	@Override
	public TestState getStartingState()
	{
		return ScenarioCreatedState.STATE;
	}
}
