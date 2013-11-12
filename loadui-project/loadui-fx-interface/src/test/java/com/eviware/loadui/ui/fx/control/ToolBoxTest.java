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
package com.eviware.loadui.ui.fx.control;

import static java.util.Arrays.*;
import static org.loadui.testfx.Assertions.verifyThat;
import static org.loadui.testfx.GuiTest.find;
import static org.loadui.testfx.GuiTest.targetWindow;
import static org.loadui.testfx.GuiTest.wrap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.loadui.testfx.matchers.NodeExistsMatcher.exists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javafx.geometry.VerticalDirection;
import org.loadui.testfx.categories.TestFX;
import org.loadui.testfx.FXScreenController;
import org.loadui.testfx.FXTestUtils;
import org.loadui.testfx.GuiTest;
import com.eviware.loadui.ui.fx.views.window.PaneOverlayHolder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.SettableFuture;

@Category(TestFX.class)
public class ToolBoxTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static final Multimap<Color, Rectangle> rectangles = LinkedListMultimap.create();
	private static final List<Rectangle> clicked = new ArrayList<>();
	private static Stage stage;
	private static GuiTest controller;
	static ToolBox<Rectangle> toolbox;
	static final List<Rectangle> allRects = asList( buildRect( Color.RED ), buildRect( Color.RED ),
			buildRect( Color.BLUE ), buildRect( Color.GREEN ), buildRect( Color.RED ), buildRect( Color.YELLOW ),
			buildRect( Color.BLUE ), buildRect( Color.ORANGE ),
			buildRect( Color.LAVENDERBLUSH ), buildRect( Color.LAVENDERBLUSH ), buildRect( Color.LAVENDERBLUSH ), buildRect( Color.LAVENDERBLUSH ),
			buildRect( Color.LAVENDERBLUSH ), buildRect( Color.LAVENDERBLUSH ), buildRect( Color.LAVENDERBLUSH ), buildRect( Color.LAVENDERBLUSH ),
			buildRect( Color.LAVENDERBLUSH ), buildRect( Color.LAVENDERBLUSH ), buildRect( Color.LAVENDERBLUSH ), buildRect( Color.LAVENDERBLUSH ));
	static final List<Rectangle> rectsToAdd = asList( RectangleBuilder.create().fill( Color.AQUA ).build() );

	public static class ToolboxTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			toolbox = new ToolBox<>( "ToolBox" );
			toolbox.setMaxWidth( 120 );
			toolbox.setMaxHeight( 400 );
			List<Rectangle> everything = new ArrayList<Rectangle>( allRects );
			everything.addAll( rectsToAdd);
			toolbox.setComparator( Ordering.explicit( everything ) );
			toolbox.setCategoryComparator( Ordering.explicit( Color.RED.toString(), Color.BLUE.toString(),
					Color.GREEN.toString(), Color.YELLOW.toString(), Color.ORANGE.toString(), Color.LAVENDERBLUSH.toString(),"Renamed" ) );

			PaneOverlayHolder root = new PaneOverlayHolder();
			root.add( toolbox );

			primaryStage.setScene( SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
					.width( 600 ).height( 750 ).root( root ).build() );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = wrap( new FXScreenController() );
		FXTestUtils.launchApp( ToolboxTestApp.class );
		stage = targetWindow( stageFuture.get( 5, TimeUnit.SECONDS ) );
		FXTestUtils.bringToFront( stage );
	}

	@Before
	public void setup() throws Exception
	{
		clicked.clear();
		rectangles.clear();
		for( Rectangle r : allRects )
		{
			rectangles.put( ( Color )r.getFill(), r );
		}

		final SettableFuture<Boolean> future = SettableFuture.create();
		Platform.runLater( new Runnable()
		{

			@Override
			public void run()
			{
				toolbox.getItems().clear();
				toolbox.getItems().setAll( allRects );
				future.set( true );
			}
		} );

		targetWindow( stage );
		future.get( 5, TimeUnit.SECONDS );
	}

	@Test
	public void shouldExpandToShowCategory() throws Exception
	{
		Rectangle rectangle0 = Iterables.get( rectangles.get( Color.RED ), 0 );
		Rectangle rectangle1 = Iterables.get( rectangles.get( Color.RED ), 1 );
		Rectangle rectangle2 = Iterables.get( rectangles.get( Color.RED ), 2 );

		controller.click( ".expander-button" ).click( rectangle2 ).click( rectangle0 ).click( rectangle1 );

		assertThat( clicked, is( asList( rectangle2, rectangle0, rectangle1 ) ) );

		assertTrue( GuiTest.findAll( ".tool-box-expander" ).size() == 1 );

		controller.target( stage ).click( ".tool-box .title" );
		assertTrue( GuiTest.findAll( ".tool-box-expander" ).size() == 0 );
	}

	@Test
	public void shouldHandleTwelveRectangles() throws Exception
	{
		controller.move( toolbox )
		.scroll( 5, VerticalDirection.DOWN )
		.click(".expander-button");

		controller.sleep( 115000 );


	}

	@Test
	public void shouldScrollUsingButtons() throws Exception
	{
		testScrolling( new Runnable()
							{
								@Override
								public void run()
								{
									controller.click( ".nav.up" );
								}
							}, new Runnable()
							{
								@Override
								public void run()
								{
									controller.click( ".nav.down" );
								}
							}
		);
	}

	@Test
	public void shouldScrollUsingMouseWheel() throws Exception
	{
		Button prevButton = find( ".nav.up" );
		controller.click( prevButton ).click( prevButton ).click( prevButton );
		testScrolling( new Runnable()
							{
								@Override
								public void run()
								{
									controller.move( ".tool-box" ).scroll( -1 );
								}
							}, new Runnable()
							{
								@Override
								public void run()
								{
									controller.move( ".tool-box" ).scroll( 1 );
								}
							}
		);
	}

	private static void testScrolling( Runnable prev, Runnable next ) throws Exception
	{
		Rectangle rectangle0 = Iterables.get( rectangles.get( Color.RED ), 0 );
		Rectangle rectangle1 = Iterables.get( rectangles.get( Color.BLUE ), 0 );
		Rectangle rectangle2 = Iterables.get( rectangles.get( Color.GREEN ), 0 );
		Rectangle rectangle3 = Iterables.get( rectangles.get( Color.YELLOW ), 0 );
		Rectangle rectangle4 = Iterables.get( rectangles.get( Color.ORANGE ), 0 );

		Button prevButton = find( ".nav.up" );
		Button nextButton = find( ".nav.down" );
		assertThat( prevButton.isDisabled(), is( true ) );
		assertThat( nextButton.isDisabled(), is( false ) );

		assertThat( rectangle0.getScene(), notNullValue() );
		assertThat( rectangle1.getScene(), notNullValue() );
		assertThat( rectangle2.getScene(), nullValue() );

		next.run();
		assertThat( prevButton.isDisabled(), is( false ) );
		assertThat( rectangle0.getScene(), nullValue() );
		assertThat( rectangle2.getScene(), notNullValue() );

		next.run();
		next.run();
		assertThat( rectangle3.getScene(), notNullValue() );
		assertThat( rectangle4.getScene(), notNullValue() );
		assertThat( nextButton.isDisabled(), is( true ) );

		prev.run();
		assertThat( nextButton.isDisabled(), is( false ) );
		assertThat( rectangle2.getScene(), notNullValue() );
		assertThat( rectangle4.getScene(), nullValue() );
	}

	@Test
	public void shouldChangeWhenAddingItemsAtRuntime() throws Exception
	{
		final Rectangle red = Iterables.get( rectangles.get( Color.RED ), 0 );
		final Rectangle blue = Iterables.get( rectangles.get( Color.BLUE ), 0 );
		final Rectangle green = Iterables.get( rectangles.get( Color.GREEN ), 0 );

		class Results
		{
			final SettableFuture<Object> clearTest = SettableFuture.create();
			final SettableFuture<Object> addTwoItemsTest = SettableFuture.create();
			final SettableFuture<Object> addItemTest = SettableFuture.create();
			final SettableFuture<Object> afterScrollingTest = SettableFuture.create();
		}
		final Results results = new Results();

		runLaterSettingRectangles( new Runnable()
		{
			@Override
			public void run()
			{
				toolbox.getItems().clear();
			}
		}, results.clearTest );
		runLaterSettingRectangles( new Runnable()
		{
			@Override
			public void run()
			{
				toolbox.getItems().add( red );
				toolbox.getItems().add( blue );
			}
		}, results.addTwoItemsTest );
		runLaterSettingRectangles( new Runnable()
		{
			@Override
			public void run()
			{
				toolbox.getItems().add( green );
			}
		}, results.addItemTest );

		Object clearTest = results.clearTest.get( 2, TimeUnit.SECONDS );
		Object addTwoItemsTest = results.addTwoItemsTest.get( 2, TimeUnit.SECONDS );
		Object addItemTest = results.addItemTest.get( 2, TimeUnit.SECONDS );

		assertFalse( clearTest instanceof Exception );
		assertFalse( addTwoItemsTest instanceof Exception );
		assertFalse( addItemTest instanceof Exception );

		assertTrue( ( ( Set<?> )clearTest ).isEmpty() );

		assertTrue( ( ( Set<?> )addTwoItemsTest ).containsAll( asList( red, blue ) ) );
		assertTrue( ( ( Set<?> )addItemTest ).containsAll( asList( red, blue ) ) ); // no change until clicking scroll button

		controller.click( ".nav.down" );

		runLaterSettingRectangles( new Runnable()
		{
			@Override
			public void run()
			{

			}
		}, results.afterScrollingTest );

		Object afterScrollingTest = results.afterScrollingTest.get( 1, TimeUnit.SECONDS );

		assertFalse( afterScrollingTest instanceof Exception );
		assertTrue( ( ( Set<?> )afterScrollingTest ).containsAll( asList( blue, green ) ) );

	}

	@Test
	public void shouldChangeWhenRemovingItemsAtRuntime() throws Exception
	{
		final Rectangle orange = Iterables.get( rectangles.get( Color.ORANGE ), 0 );

		Button nextButton = find( ".nav.down" );
		for( int i = 0; i < 5; i++ )
			controller.click( nextButton );

		assertThat( orange.getScene(), notNullValue() );

		final SettableFuture<Boolean> future = SettableFuture.create();

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				toolbox.getItems().remove( orange );
				future.set( true );
			}
		} );

		assertTrue( future.get( 1, TimeUnit.SECONDS ) );
		assertThat( orange.getScene(), nullValue() );

	}

	@Test
	public void shouldChangeWhenRenamingCategory() throws Exception
	{
		final Rectangle orange = Iterables.get( rectangles.get( Color.ORANGE ), 0 );

		Button nextButton = find( ".nav.down" );
		for( int i = 0; i < 5; i++ )
			controller.click( nextButton );

		assertThat( orange.getScene(), notNullValue() );

		final SettableFuture<Boolean> future = SettableFuture.create();

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				ToolBox.setCategory( orange, "Renamed" );
				future.set( true );
			}
		} );

		assertTrue( future.get( 1, TimeUnit.SECONDS ) );
		controller.sleep( 500 ).click( nextButton );

		verifyThat( "Renamed", exists() );
	}

	private void runLaterSettingRectangles( final Runnable runnable, final SettableFuture<Object> future )
			throws Exception
	{
		final SettableFuture<Boolean> runnableDone = SettableFuture.create();
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					runnable.run();
					System.out.println( "Runnable done" );
					runnableDone.set( true );
				}
				catch( Exception e )
				{
					future.set( e );
					runnableDone.set( false );
				}
			}
		} );

		boolean doneOk = runnableDone.get( 5, TimeUnit.SECONDS );
		if( doneOk )
		{
			System.out.println( "DoneOK so we can set the rectangles now" );
			controller.sleep( 250 );
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					future.set( GuiTest.findAll( ".tool-box .rectangle" ) );
					System.out.println( "Set the future rectangles" );
				}
			} );
		}
		else
		{
			System.out.println( "Did not set rectangles, there was an Exception" );
		}

	}

	private static Rectangle buildRect( Color color )
	{
		final Rectangle rectangle = RectangleBuilder.create().width( 50 ).height( 75 ).fill( color ).styleClass( "rectangle" ).build();
		ToolBox.setCategory( rectangle, color.toString() );

		rectangle.addEventHandler( MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				clicked.add( rectangle );
			}
		} );

		return rectangle;
	}

}
