package com.eviware.loadui.test.ui.fx;

import org.junit.AfterClass;
import org.junit.Before;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.ui.fx.util.test.LoadUiRobot;
import com.eviware.loadui.ui.fx.util.test.TestFX;

public abstract class FxIntegrationTest
{
	protected TestFX controller;
	protected LoadUiRobot robot;

	abstract TestState getStartingState();

	@AfterClass
	public static void leaveState() throws Exception
	{
		//		startingState.getParent().enter();
	}

	@Before
	public void setup()
	{
		getStartingState().enter();
		controller = GUI.getController();
		robot = LoadUiRobot.usingController( controller );
	}
}
