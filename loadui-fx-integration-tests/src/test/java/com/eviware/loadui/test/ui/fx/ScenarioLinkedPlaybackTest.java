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
import com.google.code.tempusfugit.temporal.Condition;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.TimeoutException;

import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category( IntegrationTest.class )
public class ScenarioLinkedPlaybackTest extends FxIntegrationTestBase
{

	@After
	public void teardown() throws Exception
	{
		ensureProjectIsNotRunning();
		super.teardown();
	}

	@Test
	public void shouldFollowProject_when_linked() throws Exception
	{
		SceneItem scenario = ensureScenarioIsLinkedIs( true );

		for( int i = 0; i < 3; i++ )
		{
			robot.clickPlayStopButton();

			assertTrue( scenario.isRunning() );

			robot.clickPlayStopButton();
			waitForNodeToDisappear( "#abort-requests" );

			assertFalse( scenario.isRunning() );
		}
	}

	@Test
	public void shouldNotFollowProject_when_unLinked() throws Exception
	{
		SceneItem scenario = ensureScenarioIsLinkedIs( false );

		for( int i = 0; i < 4; i++ )
		{
			robot.clickPlayStopButton();
			// using a very long timeout because of bug: LOADUI-1152
			waitForNodeToDisappear( "#abort-requests", timeout( seconds( 20 ) ) );

			assertFalse( scenario.isRunning() );
		}
	}

	@Test
	public void shouldStopOnLimit_when_isLinked() throws Exception
	{
		ProjectItem project = getProjectItem();
		ensureScenarioIsLinkedIs( true );

		setTestTimeLimitTo( 2 );
		robot.clickPlayStopButton();

		waitOrTimeout( new IsProjectRunning( project, true ), timeout( seconds( 2 ) ) );

		waitOrTimeout( new IsProjectRunning( project, false ), timeout( seconds( 4 ) ) );
	}

	private void clickOnLinkScenarioButton()
	{
		click( "#link-scenario" ).sleep( 500 );
	}

	private SceneItem ensureScenarioIsLinkedIs( final boolean follow )
	{
		final SceneItem scenario = ScenarioCreatedState.STATE.getScenario();
		if( scenario.isFollowProject() != follow )
		{
			clickOnLinkScenarioButton();
		}
		try
		{
			waitOrTimeout( new Condition()
			{
				@Override
				public boolean isSatisfied()
				{
					return scenario.isFollowProject() == follow;
				}
			}, timeout( seconds( 2 ) ) );
		}
		catch( InterruptedException | TimeoutException e )
		{
			e.printStackTrace();
			fail( "Problem while waiting for scenario to be in Linked Mode" );
		}
		return scenario;
	}

	@Override
	public TestState getStartingState()
	{
		return ScenarioCreatedState.STATE;
	}
}
