package com.eviware.loadui.ui.fx.util.test;

import com.eviware.loadui.util.test.TestUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Window;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.exceptions.NoNodesFoundException;

import java.awt.*;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static javafx.geometry.VerticalDirection.DOWN;
import static javafx.geometry.VerticalDirection.UP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.loadui.testfx.GuiTest.*;
import static org.loadui.testfx.Matchers.visible;
import static org.loadui.testfx.matchers.ContainsNodesMatcher.contains;
import static org.loadui.testfx.matchers.EnabledMatcher.enabled;

public class LoadUiRobot
{
	public enum Component
	{
		FIXED_RATE_GENERATOR( "generators", "Fixed Rate" ), TABLE_LOG( "output", "Table Log" ), WEB_PAGE_RUNNER(
			"runners", "Web Page Runner" ), VARIANCE( "generators", "Variance" ), RANDOM( "generators", "Random" ),
		RAMP_SEQUENCE( "generators", "Ramp Sequence" ), RAMP( "generators", "Ramp" ), USAGE( "generators", "Usage" ),
		FIXED_LOAD( "generators", "Fixed Load" ), SCRIPT_RUNNER( "runners", "Script Runner" ),
		PROCESS_RUNNER( "runners", "Process Runner" ), GEB_RUNNER( "runners", "Geb Runner" ), LOOP( "flow", "Loop" ),
		SPLITTER( "flow", "Splitter" ), DELAY( "flow", "Delay" ), CONDITION( "flow", "Condition" ),
		INTERVAL( "scheduler", "Interval" ), SCHEDULER( "scheduler", "Scheduler" ),
		SOUPUI_MOCKSERVICE( "misc", "soupUI MockService" ), SCENARIO( "vu-scenario", "VU Scenario" );

		public final String category;
		public final String name;

		private Component( String category, String name )
		{
			this.category = category;
			this.name = name;
		}

		public String cssClass()
		{
			return name.toLowerCase().replace( ' ', '-' );
		}
	}

	private Queue<Point> predefinedPoints;
	private GuiTest controller;

	{
		resetPredefinedPoints();
	}

	private LoadUiRobot( GuiTest controller )
	{
		this.controller = controller;
	}

	public void resetPredefinedPoints()
	{
		predefinedPoints = Lists.newLinkedList( ImmutableList.of( new Point( 250, 250 ), new Point(
				450, 450 ), new Point( 600, 200 ), new Point( 200, 600 ), new Point(600, 600) ) );
	}

	public static LoadUiRobot usingController( GuiTest controller )
	{
		return new LoadUiRobot( controller );
	}

	public ComponentHandle createComponent( final Component component )
	{
		if( findAll( ".canvas-object-view" ).isEmpty() )
		{
			resetPredefinedPoints();
		}

		checkNotNull( predefinedPoints.peek(),
				"All predefined points (x,y) for component placement are used. Please add new ones." );
		return createComponentAt( component, predefinedPoints.poll() );
	}

	public ComponentHandle createComponentAt( final Component component, Point targetPoint )
	{
		final int numberOfComponents = GuiTest.findAll( ".canvas-object-view" ).size();
		Set<Node> oldOutputs = findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> oldInputs = findAll( ".canvas-object-view .terminal-view.input-terminal" );

		expandCategoryOf( component );

		Window window = find( "#" + component.category + ".category" ).getScene().getWindow();
		int windowX = ( int )window.getX();
		int windowY = ( int )window.getY();
		controller.drag( matcherForIconOf( component ) )
				.to( windowX + targetPoint.x, windowY + targetPoint.y );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return GuiTest.findAll( ".canvas-object-view" ).size() == numberOfComponents + 1;
			}
		}, 25 );

		Set<Node> outputs = findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> inputs = findAll( ".canvas-object-view .terminal-view.input-terminal" );
		inputs.removeAll( oldInputs );
		outputs.removeAll( oldOutputs );

		return new ComponentHandle( inputs, outputs, controller, this );
	}

	public Matcher<Node> matcherForIconOf( final Component component )
	{
		return new TypeSafeMatcher<Node>()
		{
			@Override
			public boolean matchesSafely( Node node )
			{
				String className = node.getClass().getSimpleName();
				if( className.equals( "ComponentDescriptorView" ) || className.equals( "NewScenarioIcon" ) )
				{
					return node.toString().equals( component.name );
				}
				return false;
			}

			@Override
			public void describeTo( Description description )
			{
				//To change body of implemented methods use File | Settings | File Templates.
			}
		};
	}

	public void expandCategoryOf( Component component )
	{
		if( GuiTest.findAll( "#" + component.category + ".category .expander-button" ).isEmpty() )
		{
			scrollToCategoryOf( component );
		}
		controller.click( "#" + component.category + ".category .expander-button" );
	}

	public void scrollToCategoryOf( Component component )
	{
		String query = "#" + component.category + ".category";
		controller.move( ".canvas-view .tool-box .button .up" );

		for( VerticalDirection direction : ImmutableList.of( DOWN, UP ) )
		{
			int maxToolboxCategories = 10;
			while( GuiTest.findAll( query ).isEmpty() && --maxToolboxCategories >= 0 )
			{
				controller.scroll( 10, direction );
			}
		}

		if( GuiTest.findAll( query ).isEmpty() )
			throw new RuntimeException( "Could not find component category '" + component.category
					+ "' in component ToolBox." );
	}

	public Node getComponentNode( Component component )
	{
		return findComponentByName( component.name, false );
	}

	private Node findComponentByName( String name, boolean exactMatch )
	{
		for( Node compNode : findAll( ".canvas-object-view" ) )
		{
			Set<Node> textLabels = findAll( ".label", find( "#topBar", compNode ) );
			for( Node label : textLabels )
			{
				if( !( label instanceof Label ) ) continue;
				String componentLabel = ( ( Label )label ).getText();
				boolean foundMatch = exactMatch ? componentLabel.equals( name ) : componentLabel.startsWith( name );
				if( foundMatch )
				{
					return compNode;
				}
			}
		}
		throw new NoNodesFoundException( "No component found matching name " + name );
	}

	public void clickPlayStopButton()
	{
		Node playButton = find( ".project-playback-panel .play-button" );
		waitUntil( playButton, is( enabled() ) );
		controller.click( playButton );
	}

	public void pointAtPlayStopButton()
	{
		controller.move( ".project-playback-panel .play-button" );
	}

	public void runTestFor( int number, TimeUnit unit )
	{
		clickPlayStopButton();
		controller.sleep( unit.toMillis( number ) );
		clickPlayStopButton();
		waitUntil( "#abort-requests", is( not( visible() ) ) );
	}

	public void deleteAllComponentsFromProjectView()
	{
		waitUntil( "#abort-requests", is( not( visible() ) ) );

		controller.click( "#designTab" );

		int maxTries = 20;
		int tries = 0;
		while( tries++ < maxTries && !findAll( ".component-view" ).isEmpty() )
			controller.click( ".component-view #menu" ).click( "#delete-item" ).click( "#default" );

		assertThat( ".component-layer", contains( 0, ".component-view" ) );
		resetPredefinedPoints();
	}
}
