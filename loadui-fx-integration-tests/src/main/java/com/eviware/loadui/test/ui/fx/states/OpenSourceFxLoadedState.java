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

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.FxIntegrationBase;
import com.eviware.loadui.test.ui.fx.GUI;
import javafx.stage.Stage;
import org.loadui.testfx.FXTestUtils;
import org.loadui.testfx.GuiTest;

import java.io.File;
import java.util.NoSuchElementException;

public class OpenSourceFxLoadedState extends TestState
{
	public static final TestState STATE = new OpenSourceFxLoadedState();
	private FxIntegrationBase base = new FxIntegrationBase();
	private boolean firstTimeEntering = true;

	protected OpenSourceFxLoadedState()
	{
		super( "OS FX Loaded" );
	}

	protected OpenSourceFxLoadedState( String name )
	{
		super( name );
	}

	@Override
	protected TestState parentState()
	{
		return TestState.ROOT;
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		getGui().getBundleContext();
		System.setProperty( "groovy.root", new File( LoadUI.getWorkingDir(), ".groovy" ).getAbsolutePath() );
		if( firstTimeEntering )
		{
			base.waitForNode( ".getting-started-dialog" );
			closeWindow( "Welcome to LoadUI" );
			closeWindow( "New version available" );
			firstTimeEntering = false;
		}
		getGui().getController().click( "#mainButton" ).click( "#mainButton" ).sleep( 500 );
	}

	protected GUI getGui()
	{
		return GUI.getOpenSourceGui();
	}

	protected void closeWindow( final String windowTitle ) throws Exception
	{
		try
		{
			final Stage dialog = GuiTest.findStageByTitle( windowTitle );

			FXTestUtils.invokeAndWait( new Runnable()
			{
				@Override
				public void run()
				{
					log.debug( "Closing window: '" + windowTitle + "'" );
					dialog.close();
				}
			}, 1 );
		}
		catch( NoSuchElementException e )
		{
			// No need to close a window if it's not open.
		}
	}

	@Override
	protected void exitToParent() throws Exception
	{
	}
}
