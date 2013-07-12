package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.ui.fx.util.test.ComponentHandle;
import com.eviware.loadui.ui.fx.util.test.LoadUiRobot;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.test.TestUtils;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;

import java.util.Set;
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
		robot = LoadUiRobot.usingController( controller );
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
		Node componentNode = robot.getComponentNode( component );
		Set<Node> knobs = findAll( ".knob", componentNode );
		System.out.println( "Found " + knobs.size() + " knobs: " + knobs );
		return new KnobHandle( knobs.iterator().next() );
	}

	public class KnobHandle
	{
		final Node knob;

		KnobHandle( Node knob )
		{
			this.knob = knob;
		}

		public KnobHandle to( int value )
		{
			controller.doubleClick( knob ).type( Integer.toString( value ) ).type( KeyCode.ENTER );
			return this;
		}

	}
}
