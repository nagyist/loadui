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
import org.loadui.testfx.GuiTest;
import javafx.scene.Node;

import java.util.Set;

public class LastResultOpenedState extends TestState
{
	public static final LastResultOpenedState STATE = new LastResultOpenedState();

	private LastResultOpenedState()
	{
		super( "Last Result Opened", ProjectLoadedWithoutAgentsState.STATE );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		GUI.getController().click( ".project-playback-panel .play-button" ).sleep( 1500 )
				.click( ".project-playback-panel .play-button" ).sleep( 5000 ).click( "#statsTab" )
				.click( "#open-execution" ).doubleClick( "#result-0" );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		Set<Node> resultViewSet = GuiTest.findAll( ".result-view" );
		if( !resultViewSet.isEmpty() )
		{
			GUI.getController().closeCurrentWindow();
		}
	}
}
