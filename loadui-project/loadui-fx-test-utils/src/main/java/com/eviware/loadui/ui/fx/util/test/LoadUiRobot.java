package com.eviware.loadui.ui.fx.util.test;

import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.stage.Window;

import java.awt.*;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.eviware.loadui.ui.fx.util.test.TestFX.find;
import static com.eviware.loadui.ui.fx.util.test.TestFX.findAll;

public class LoadUiRobot
{
	public enum Component
	{
		FIXED_RATE_GENERATOR( "generators", "Fixed Rate" ), TABLE_LOG( "output", "Table Log" ), WEB_PAGE_RUNNER(
			"runners", "Web Page Runner" );

		public final String category;
		public final String name;

		private Component( String category, String name )
		{
			this.category = category;
			this.name = name;
		}
	}

	private Queue<Point> predefinedPoints = Lists.newLinkedList( ImmutableList.of( new Point( 250, 250 ), new Point(
			450, 450 ) ) );
	private TestFX controller;

	private LoadUiRobot( TestFX controller )
	{
		this.controller = controller;
	}

	public static LoadUiRobot usingController( TestFX controller )
	{
		return new LoadUiRobot( controller );
	}

	public ComponentHandle createComponent( final Component component )
	{
		Preconditions.checkNotNull( predefinedPoints.peek(),
				"All predefined points (x,y) for component placement are used. Please add new ones." );
		return createComponentAt( component, predefinedPoints.poll() );
	}

	public ComponentHandle createComponentAt( final Component component, Point targetPoint )
	{
		final int numberOfComponents = TestFX.findAll( ".canvas-object-view" ).size();
		Set<Node> oldOutputs = findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> oldInputs = findAll( ".canvas-object-view .terminal-view.input-terminal" );

		expandCategoryOf( component );

		Window window = find( "#runners.category" ).getScene().getWindow();
		int windowX = ( int )window.getX();
		int windowY = ( int )window.getY();
		controller.drag( predicateForIconOf( component ) )
				.to( windowX + targetPoint.x, windowY + targetPoint.y );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return TestFX.findAll( ".canvas-object-view" ).size() == numberOfComponents + 1;
			}
		}, 25000 );

		Set<Node> outputs = findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> inputs = findAll( ".canvas-object-view .terminal-view.input-terminal" );
		inputs.removeAll( oldInputs );
		outputs.removeAll( oldOutputs );

		return new ComponentHandle( inputs, outputs, controller, this );
	}

	public Predicate<Node> predicateForIconOf( final Component component )
	{
		return new Predicate<Node>()
		{
			@Override
			public boolean apply( Node input )
			{
				if( input.getClass().getSimpleName().equals( "ComponentDescriptorView" ) )
				{
					return input.toString().equals( component.name );
				}
				return false;
			}
		};
	}

	public void expandCategoryOf( Component component )
	{
		if( TestFX.findAll( "#" + component.category + ".category .expander-button" ).isEmpty() )
		{
			scrollToCategoryOf( component );
		}
		controller.click( "#" + component.category + ".category .expander-button" );
	}

	public void scrollToCategoryOf( Component component )
	{
		int maxToolboxCategories = 50;
		while( TestFX.findAll( "#" + component.category + ".category" ).isEmpty() )
		{
			if( --maxToolboxCategories < 0 )
				throw new RuntimeException( "Could not find component category " + component.category
						+ " in component ToolBox." );
			controller.move( "#runners.category" ).scroll( 10 );
		}
	}

	public Node getComponentNode( Component component, String... optionalName )
	{
		if( optionalName.length > 0 )
		{
			return findComponentByName( optionalName[0], true );
		}
		else
		{
			return findComponentByName( component.name, false );
		}
	}

	private Node findComponentByName( String name, boolean exactMatch )
	{
		for( Node compNode : findAll( ".canvas-object-view" ) )
		{
			Set<Node> textLabels = findAll( "Label", find( "#topBar", compNode ) );
			for( Node label : textLabels )
			{
				String componentLabel = ( ( Label )label ).getText();
				boolean foundMatch = exactMatch ? componentLabel.equals( name ) : componentLabel.startsWith( name );
				if( foundMatch )
				{
					return compNode;
				}
			}
		}
		return null;
	}

	public void clickPlayStopButton()
	{
		controller.click( ".project-playback-panel .play-button" );
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
		controller.sleep( 2000 );
	}
}
