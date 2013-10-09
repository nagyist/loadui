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
import com.eviware.loadui.test.ui.fx.FxIntegrationBase;
import com.eviware.loadui.test.ui.fx.GUI;
import javafx.scene.Node;
import org.loadui.testfx.GuiTest;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class LastResultOpenedState extends TestState
{
	public static final LastResultOpenedState STATE = new LastResultOpenedState();

	private FxIntegrationBase integrationBase = new FxIntegrationBase();

	private LastResultOpenedState()
	{
		super( "Last Result Opened" );
	}

	protected LastResultOpenedState( String name )
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
		integrationBase.runTestFor( 2, TimeUnit.SECONDS );
		GUI.getOpenSourceGui().getController().click( "#statsTab" )
				.click( "#open-execution" )
				.doubleClick( "#result-0" );
		integrationBase.waitForNodeToDisappear( ".result-view" );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		Set<Node> resultViewSet = GuiTest.findAll( ".result-view" );
		if( !resultViewSet.isEmpty() )
		{
			GUI.getOpenSourceGui().getController().closeCurrentWindow();
		}
		GUI.getOpenSourceGui().getController().click( "#designTab" );
	}
}
