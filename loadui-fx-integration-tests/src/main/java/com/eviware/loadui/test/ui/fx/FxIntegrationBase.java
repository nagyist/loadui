package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.ui.fx.util.test.ComponentHandle;
import com.eviware.loadui.ui.fx.util.test.LoadUiRobot;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.test.TestUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.eviware.loadui.ui.fx.util.test.TestFX.findAll;

/**
 * @Author Henrik
 */
public class FxIntegrationBase
{

	protected final TestFX controller;
	protected final LoadUiRobot robot;

	public FxIntegrationBase()
	{
		controller = GUI.getController();
		robot = LoadUiRobot.usingController(controller);
	}

	public void create( LoadUiRobot.Component component )
	{
		robot.createComponent(component);
	}

	public void runTestFor( int number, TimeUnit unit )
	{
		robot.runTestFor(number, unit);
	}

	public ComponentHandle connect( LoadUiRobot.Component component )
	{
		return robot.createComponent(component);
	}

	public void waitForNode( final String domQuery )
	{
		TestUtils.awaitCondition(new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return !findAll(domQuery).isEmpty();
			}
		});
	}

	public void waitForNodeToDisappear( final String domQuery )
	{
		TestUtils.awaitCondition(new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return findAll(domQuery).isEmpty();
			}
		});
	}
}
