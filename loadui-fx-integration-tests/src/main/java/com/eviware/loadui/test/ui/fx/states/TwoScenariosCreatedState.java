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

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import com.eviware.loadui.util.test.TestUtils;

import java.util.concurrent.Callable;

import static org.loadui.testfx.GuiTest.findAll;

public class TwoScenariosCreatedState extends TestState
{
	public static final TwoScenariosCreatedState STATE = new TwoScenariosCreatedState();
	public static final String[] SCENARIO_NAME = { "Scenario 1", "Scenario 2" };

	private TwoScenariosCreatedState()
	{
		super( "Scenario Created" );
	}

	protected TwoScenariosCreatedState( String name )
	{
		super( name );
	}

	@Override
	protected TestState parentState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		log.debug( "Creating scenarios." );
		GUI.getOpenSourceGui().getController().drag( "#newScenarioIcon" ).by( 300, 0 ).drop();
		System.out.println( "Will now click on the Scenario Name" );
		GUI.getOpenSourceGui().getController()
				.sleep( 10000 )
				.click( "#scenario-name" )
				.type( SCENARIO_NAME[0] ).click( "#default" );
		GUI.getOpenSourceGui().getController().drag( "#newScenarioIcon" ).by( 400, 100 ).drop().sleep( 100 ).click( "#scenario-name" )
				.type( SCENARIO_NAME[1] ).click( "#default" );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return findAll( ".scenario-view" ).size() == 2;
			}
		} );

	}

	@Override
	protected void exitToParent() throws Exception
	{
		log.debug( "Deleting scenarios." );

		GUI.getOpenSourceGui().getController().click( ".scenario-view #menu" ).click( "#delete-item" ).click( ".confirmation-dialog #default" );
		GUI.getOpenSourceGui().getController().click( ".scenario-view #menu" ).click( "#delete-item" ).click( ".confirmation-dialog #default" );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return findAll( ".scenario-view" ).isEmpty();
			}
		} );
	}
}
