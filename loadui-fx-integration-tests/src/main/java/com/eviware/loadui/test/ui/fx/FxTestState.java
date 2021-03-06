package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.ui.fx.util.test.ComponentHandle;
import com.eviware.loadui.ui.fx.util.test.LoadUiRobot;
import org.loadui.testfx.GuiTest;

import java.util.concurrent.TimeUnit;

/**
 * @Author Henrik
 */
public abstract class FxTestState extends TestState
{

	private final FxIntegrationBase fxTestSupport = new FxIntegrationBase();

	protected LoadUiRobot robot = fxTestSupport.robot;
	protected GuiTest controller = fxTestSupport;

	protected FxTestState( String name )
	{
		super( name );
	}

	public void create( LoadUiRobot.Component component )
	{
		fxTestSupport.create( component );
	}

	public void runTestFor( int number, TimeUnit unit )
	{
		fxTestSupport.runTestFor( number, unit );
	}

	public ComponentHandle connect( LoadUiRobot.Component component )
	{
		return fxTestSupport.connect( component );
	}

	public void waitForNode( String domQuery )
	{
		fxTestSupport.waitForNode( domQuery );
	}

	public void waitForNodeToDisappear( String domQuery )
	{
		fxTestSupport.waitForNodeToDisappear( domQuery );
	}
}
