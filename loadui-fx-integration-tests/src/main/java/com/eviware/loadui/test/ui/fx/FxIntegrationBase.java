package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.ui.fx.util.test.ComponentHandle;
import com.eviware.loadui.ui.fx.util.test.LoadUiRobot;
import com.eviware.loadui.util.test.TestUtils;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import org.loadui.testfx.GuiTest;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @Author Henrik
 */
public class FxIntegrationBase extends GuiTest
{
	protected final LoadUiRobot robot;

	public enum RunBlocking
	{
		BLOCKING, NON_BLOCKING
	}

	public FxIntegrationBase()
	{
		robot = LoadUiRobot.usingController( this );
		System.out.println( "This is the groovy home - " + System.getProperty( "groovy.root" ) );
	}

	public void create( LoadUiRobot.Component component )
	{
		System.setProperty( "groovy.root", System.getProperty( LoadUI.LOADUI_WORKING ) + File.separator + ".groovy" );
		robot.createComponent( component );
	}

	public void runTestFor( int number, TimeUnit unit )
	{
		runTestFor( number, unit, RunBlocking.BLOCKING );
	}

	public void runTestFor( final int number, final TimeUnit unit, RunBlocking blocking )
	{
		if( blocking == RunBlocking.NON_BLOCKING )
		{
			new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					robot.runTestFor( number, unit );
				}
			} ).start();
		}
		else
		{
			robot.runTestFor( number, unit );
		}

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

	public KnobHandle turnKnobIn( LoadUiRobot.Component component, int number )
	{
		final Node componentNode = robot.getComponentNode( component );
		System.out.println( "Component node: " + componentNode );
		Set<Node> knobs = findAll( ".knob", componentNode );

		Node knob = ( Node )Arrays.asList( knobs.toArray() ).get( number + 1 );

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
