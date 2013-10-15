package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.ui.fx.util.test.ComponentHandle;
import com.eviware.loadui.ui.fx.util.test.LoadUiRobot;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.TestUtils;
import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.Timeout;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import org.loadui.testfx.GuiTest;

import java.awt.*;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.loadui.testfx.matchers.VisibleNodesMatcher.visible;

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
	}

	public void create( LoadUiRobot.Component component )
	{
		robot.createComponent( component );
	}

	public void createAt( LoadUiRobot.Component component, Point targetPoint )
	{
		robot.createComponentAt( component, targetPoint );
	}

	public void runTestFor( int number, TimeUnit unit )
	{
		System.out.println("     runTestFor");
		runTestFor( number, unit, RunBlocking.BLOCKING );
	}

	public void runTestFor( final int number, final TimeUnit unit, RunBlocking blocking )
	{
		if( blocking == RunBlocking.NON_BLOCKING )
		{
			System.out.println("     NON_BLOCKING");
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
			System.out.println("     BLOCKING");
		}
	}

	public void clickPlayStopButton()
	{
		click( ".project-playback-panel .play-button" );
	}

	public void waitForBlockingTaskToComplete()
	{
		waitUntil( ".task-progress-indicator", is( not( visible() ) ) );
	}

	protected void ensureProjectIsNotRunning()
	{
		final ProjectItem project = getProjectItem();
		if( project.isRunning() )
		{
			try
			{
				waitOrTimeout( new IsProjectRunning( project, false ), timeout( seconds( 5 ) ) );
				System.out.println( "Project stopped running" );
			}
			catch( InterruptedException | TimeoutException e )
			{
				e.printStackTrace();
				robot.clickPlayStopButton();
				ensureProjectIsNotRunning();
			}
		}
		waitForNodeToDisappear( "#abort-requests" );
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

	public void waitForNodeToDisappear( final String domQuery, Timeout timeout )
	{
		try
		{
			waitOrTimeout( new Condition()
			{
				@Override
				public boolean isSatisfied()
				{
					return findAll( domQuery ).isEmpty();
				}
			}, timeout );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}

	}

	public void waitForNodeToDisappear( final String domQuery )
	{
		waitForNodeToDisappear( domQuery, timeout( seconds( 5 ) ) );
	}

	public void setTestTimeLimitTo( int seconds )
	{
		click( "#set-limits" ).click( "#time-limit" ).doubleClick()
				.type( Integer.toString( seconds ) ).click( "#default" );
	}

	public KnobHandle turnKnobIn( LoadUiRobot.Component component )
	{
		final Node componentNode = robot.getComponentNode( component );
		System.out.println( "Component node: " + componentNode );
		Node knob = find( ".knob", componentNode );
		return new KnobHandle( knob );
	}

	public void ensureResultViewWindowIsClosed()
	{
		if( isResultViewWindowIsOpen() )
		{
			closeCurrentWindow();
		}
	}

	public boolean isResultViewWindowIsOpen()
	{
		return !GuiTest.findAll( ".result-view" ).isEmpty();
	}

	protected ProjectItem getProjectItem()
	{
		Collection<? extends ProjectItem> projects = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace()
				.getProjects();
		return projects.iterator().next();
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
