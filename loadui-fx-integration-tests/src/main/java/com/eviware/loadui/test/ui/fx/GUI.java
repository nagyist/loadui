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

import com.eviware.loadui.util.BeanInjector;
import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.WaitFor;
import javafx.stage.Stage;
import org.loadui.testfx.FXTestUtils;
import org.loadui.testfx.GuiTest;
import org.osgi.framework.BundleContext;

import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;

public class GUI
{
	protected static GUI instance;

	private ControllerFXWrapper controller;
	private Stage stage;
	private GuiTest robot;

	protected GUI()
	{
		ControllerFXWrapper localController = null;
		Stage localStage = null;
		GuiTest localRobot = null;

		try
		{
			localController = createController();

			localStage = localController.getStage();
			final Stage finalStage = localStage;

			WaitFor.waitOrTimeout( new Condition()
			{
				@Override
				public boolean isSatisfied()
				{
					return finalStage.getScene() != null;
				}
			}, timeout( seconds( 60 ) ) );

			BeanInjector.setBundleContext( localController.getBundleContext() );

			Thread.sleep( 1000 );

			FXTestUtils.bringToFront( localStage );
			localRobot = new GuiTest();
			GuiTest.targetWindow( localStage );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		controller = localController;
		stage = localStage;
		robot = localRobot;
	}

	protected ControllerFXWrapper createController()
	{
		return new ControllerFXWrapper();
	}

	public static synchronized GUI getOpenSourceGui()
	{
		if( instance == null )
		{
			instance = new GUI();
		}
		return instance;
	}

	public GuiTest getController()
	{
		return robot.target( getStage() );
	}

	public Stage getStage()
	{
		return stage;
	}

	public BundleContext getBundleContext()
	{
		return controller.getBundleContext();
	}

}

