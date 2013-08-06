package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.ui.fx.util.test.ComponentHandle;
import com.eviware.loadui.ui.fx.util.test.LoadUiRobot;
import com.eviware.loadui.util.test.TestUtils;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import org.loadui.testfx.GuiTest;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @Author Henrik
 */
public class FxIntegrationBase extends GuiTest
{
	protected final LoadUiRobot robot;

	public FxIntegrationBase()
	{
		robot = LoadUiRobot.usingController( this );
	}

	public void create( LoadUiRobot.Component component )
	{
		robot.createComponent( component );
	}

	public void runTestFor( int number, TimeUnit unit )
	{
		robot.runTestFor( number, unit );
	}

	public ComponentHandle connect( LoadUiRobot.Component component )
	{
		return robot.createComponent( component );
	}

	public void waitForNode( final String domQuery )
	{
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return !findAll( domQuery ).isEmpty();
			}
		} );
	}

	public void waitForNodeToDisappear( final String domQuery )
	{
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return findAll( domQuery ).isEmpty();
			}
		} );
	}

	public KnobHandle turnKnobIn( LoadUiRobot.Component component )
	{
		final Node componentNode = robot.getComponentNode( component );
		System.out.println( "Component node: " + componentNode );
		Node knob = find( ".knob", componentNode );
		return new KnobHandle( knob );
	}

	public class KnobHandle
	{
		final Node knob;

		KnobHandle( Node knob )
		{
			this.knob = knob;
		}

		public KnobHandle to( long value )
		{
			doubleClick( knob ).type( Long.toString( value ) ).type( KeyCode.ENTER );
			return this;
		}

	}
}
