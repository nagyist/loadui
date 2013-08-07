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
import com.eviware.loadui.test.ui.fx.FxTestState;
import javafx.scene.Node;

import static org.loadui.testfx.GuiTest.find;
import static org.loadui.testfx.GuiTest.findAll;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.FIXED_RATE_GENERATOR;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.WEB_PAGE_RUNNER;
import static org.loadui.testfx.matchers.ContainsNodesMatcher.contains;
import static org.junit.Assert.assertThat;

public class SimpleWebTestState extends FxTestState
{
	public static final SimpleWebTestState STATE = new SimpleWebTestState();

	private SimpleWebTestState()
	{
		this( "Simple Web Test", ProjectLoadedWithoutAgentsState.STATE );
	}

	protected SimpleWebTestState( String name, TestState parent )
	{
		super( name, parent );
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		connect( FIXED_RATE_GENERATOR ).to( WEB_PAGE_RUNNER );

		controller.click( ".component-view .text-field" ).type( "win-srvmontest" );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		controller.click( "#designTab" );

		int maxTries = 2;
		int tries = 0;
		while( tries++ < maxTries && !findAll( ".component-view" ).isEmpty() )
			controller.click( ".component-view #menu" ).click( "#delete-item" ).click( "#default" );

		assertThat( ".component-layer", contains( 0, ".component-view" ) );
	}
}
