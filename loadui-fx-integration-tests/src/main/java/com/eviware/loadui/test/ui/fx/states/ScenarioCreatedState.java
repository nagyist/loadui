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
package com.eviware.loadui.test.ui.fx.states;


import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.FxIntegrationBase;
import com.eviware.loadui.test.ui.fx.FxTestState;
import com.eviware.loadui.test.ui.fx.GUI;


public class ScenarioCreatedState extends FxTestState
{
	public static final ScenarioCreatedState STATE = new ScenarioCreatedState();
	public static final String SCENARIO_NAME = "Scenario 1";

	private ScenarioCreatedState()
	{
		super( "Scenario 1 Created" );
	}

	protected ScenarioCreatedState( String name )
	{
		super( name );
	}

	@Override
	protected TestState parentState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	public SceneItem getScenario()
	{
		return FxIntegrationBase.getProjectItem().getSceneByLabel( SCENARIO_NAME );
	}

	@Override
	protected void enterFromParent()
	{
		log.debug( "Creating scenario." );
		GUI.getOpenSourceGui().getController().drag( "#newScenarioIcon" ).by( 500, -150 ).drop();

		waitForNode( ".scenario-view" );
	}

	@Override
	protected void exitToParent()
	{
		log.debug( "Deleting scenario." );

//		waitUntil( new Callable<Boolean>()
//		{
//			@Override
//			public Boolean call() throws Exception
//			{
//				ToggleButton playButton = find( ".play-button" );
//
//				boolean isStopping = true;
//				try{
//					find(".task-progress-indicator");
//				} catch( NoNodesFoundException e )
//				{
//					isStopping = false;
//				}
//
//				return !playButton.isSelected() && !isStopping;
//			}
//		}, is( true ) );

		GUI.getOpenSourceGui().getController().click( ".scenario-view #menu" ).click( "#delete-item" ).click( ".confirmation-dialog #default" );

		waitForNodeToDisappear( ".scenario-view" );
	}
}
