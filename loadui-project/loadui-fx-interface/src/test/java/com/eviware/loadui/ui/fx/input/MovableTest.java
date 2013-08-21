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
package com.eviware.loadui.ui.fx.input;

import static javafx.beans.binding.Bindings.when;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.loadui.testfx.categories.TestFX;
import org.loadui.testfx.FXTestUtils;
import org.loadui.testfx.GuiTest;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.GroupBuilder;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.ui.fx.api.input.DraggableEvent;

@Category( TestFX.class )
public class MovableTest extends GuiTest
{
	private static MovableImpl movable;
	private static Group group;

	private static void createRootNode()
	{
		Rectangle dragRect = RectangleBuilder.create().id( "dragrect" ).width( 25 ).height( 25 ).fill( Color.BLUE )
				.build();
		movable = MovableImpl.install( dragRect );

		Rectangle dropRect = RectangleBuilder.create().id( "droprect" ).width( 50 ).height( 50 ).layoutX( 100 )
				.layoutY( 100 ).build();
		dropRect.fillProperty().bind( when( movable.acceptableProperty() ).then( Color.GREEN ).otherwise( Color.RED ) );

		group = GroupBuilder.create().children( dropRect, dragRect ).build();
	}

	@BeforeClass
	public static void createWindow()
	{
		createRootNode();
		showNodeInStage( group );
	}

	@After
	public void restorePosition()
	{
		final Node node = movable.getNode();
		node.setLayoutX( 0 );
		node.setLayoutY( 0 );
		FXTestUtils.awaitEvents();
	}

	@Test
	public void shouldMove() throws Throwable
	{
		final Node movableNode = movable.getNode();

		assertThat( movable.isDragging(), is( false ) );

		MouseMotion dragging = drag( movableNode ).by( 100, 50 );

		assertThat( movable.isDragging(), is( true ) );

		dragging.drop();

		assertThat( movable.isDragging(), is( false ) );

		assertEquals( 100.0, movableNode.getLayoutX(), 1.0 );
		assertEquals( 50.0, movableNode.getLayoutY(), 1.0 );
	}

	@Test
	public void oldNodesShouldMove_after_newNodesHaveBeenAdded() throws Throwable
	{
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				group.getChildren().add(
						RectangleBuilder.create().id( "newrect" ).width( 15 ).height( 15 ).layoutX( 40 ).fill( Color.GRAY )
								.build() );
			}
		} );
		FXTestUtils.awaitEvents();

		shouldMove();
	}

	@Test
	public void shouldAcceptOnHover() throws Throwable
	{
		final Node movableNode = movable.getNode();
		final Node dropzone = find( "#droprect" );

		dropzone.addEventHandler( DraggableEvent.DRAGGABLE_ENTERED, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( DraggableEvent event )
			{
				event.accept();
				event.consume();
			}
		} );

		assertThat( movable.isAcceptable(), is( false ) );

		MouseMotion dragging = drag( movableNode ).via( dropzone );

		assertTrue( movable.isAcceptable() );

		dragging.by( -100, -50 );

		assertFalse( movable.isAcceptable() );

		dragging.drop();
	}

	@Test
	public void shouldDrop() throws Throwable
	{
		final Node movableNode = movable.getNode();
		final Node dropzone = find( "#droprect" );

		final CountDownLatch droppedLatch = new CountDownLatch( 1 );

		dropzone.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( DraggableEvent event )
			{
				if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED )
				{
					event.accept();
				}
				else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
				{
					droppedLatch.countDown();
				}
				event.consume();
			}
		} );

		drag( movableNode ).to( dropzone );

		droppedLatch.await( 2, TimeUnit.SECONDS );
	}
}
