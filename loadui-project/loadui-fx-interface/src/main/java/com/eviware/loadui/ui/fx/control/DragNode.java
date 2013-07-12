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

import com.eviware.loadui.ui.fx.api.input.Draggable;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.util.ManualObservable;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.util.UIUtils;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.TimelineBuilder;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Adds the ability to drag an object from a source Node, which can potentially
 * be dropped on a target.
 *
 * @author dain.nilsson
 * @author Henrik
 */
public class DragNode implements Draggable
{
	private static final Duration REVERT_DURATION = new Duration( 300 );
	private static final String DRAG_NODE_PROP_KEY = DragNode.class.getName();
	private static final DragNodeBehavior BEHAVIOR = new DragNodeBehavior();

	private static ManualObservable onReleased = new ManualObservable();

	public static Observable onReleased()
	{
		return onReleased;
	}

	public static void install( Node node, DragNode dragNode )
	{
		BEHAVIOR.install( node, dragNode );
	}

	public static DragNode install( Node node, Node draggableNode )
	{
		DragNode dragNode = new DragNode( draggableNode );
		node.setCursor( Cursor.MOVE );
		BEHAVIOR.install( node, dragNode );

		return dragNode;
	}

	public static void uninstall( Node node )
	{
		BEHAVIOR.uninstall( node );
	}

	private final ObjectProperty<Node> nodeProperty = new SimpleObjectProperty<>();

	private boolean hideOriginalNodeWhenDragging = false;

	public DragNode hideOriginalNodeWhenDragging()
	{
		hideOriginalNodeWhenDragging = true;
		return this;
	}

	public boolean isHideOriginalNodeWhenDragging()
	{
		return hideOriginalNodeWhenDragging;
	}

	public Node getNode()
	{
		return nodeProperty.get();
	}

	private ReadOnlyBooleanWrapper draggingProperty;

	private ReadOnlyBooleanWrapper draggingPropertyImpl()
	{
		if( draggingProperty == null )
		{
			draggingProperty = new ReadOnlyBooleanWrapper( false );
		}

		return draggingProperty;
	}

	private void setDragging( boolean dragging )
	{
		if( isDragging() != dragging )
		{
			draggingPropertyImpl().set( dragging );
		}
	}

	@Override
	public ReadOnlyBooleanProperty draggingProperty()
	{
		return draggingPropertyImpl().getReadOnlyProperty();
	}

	@Override
	public boolean isDragging()
	{
		return draggingProperty != null && draggingProperty.get();
	}

	private ReadOnlyBooleanWrapper acceptableProperty;

	private ReadOnlyBooleanWrapper acceptablePropertyImpl()
	{
		if( acceptableProperty == null )
		{
			acceptableProperty = new ReadOnlyBooleanWrapper( false );
		}

		return acceptableProperty;
	}

	private void setAcceptable( boolean acceptable )
	{
		if( isAcceptable() != acceptable )
		{
			acceptablePropertyImpl().set( acceptable );
		}
	}

	@Override
	public ReadOnlyBooleanProperty acceptableProperty()
	{
		return acceptablePropertyImpl().getReadOnlyProperty();
	}

	@Override
	public boolean isAcceptable()
	{
		return acceptableProperty != null && acceptableProperty.get();
	}

	private ObjectProperty<Object> dataProperty;

	@Override
	public ObjectProperty<Object> dataProperty()
	{
		if( dataProperty == null )
		{
			dataProperty = new SimpleObjectProperty<>( this, "data" );
		}

		return dataProperty;
	}

	@Override
	public void setData( Object data )
	{
		dataProperty().set( data );
	}

	@Override
	public Object getData()
	{
		return dataProperty == null ? null : dataProperty.get();
	}

	private BooleanProperty revertProperty;

	public BooleanProperty revertProperty()
	{
		if( revertProperty == null )
		{
			revertProperty = new SimpleBooleanProperty( this, "revert", true );
		}

		return revertProperty;
	}

	public boolean isRevert()
	{
		return revertProperty == null || revertProperty.get();
	}

	public void setRevert( boolean revert )
	{
		revertProperty().set( revert );
	}

	private Node dragSource;
	private Node currentlyHovered;
	private Point2D startPoint = new Point2D( 0, 0 );
	private Point2D lastPoint = new Point2D( 0, 0 );

	public DragNode( Node node )
	{
		nodeProperty.set( node );
	}

	public Node getDragSource()
	{
		return dragSource;
	}

	public void setVisible( boolean visible )
	{
		getNode().setVisible( visible );
	}

	public void setX( double x )
	{
		getNode().setLayoutX( x );
	}

	public void setY( double y )
	{
		getNode().setLayoutY( y );
	}

	public double getX()
	{
		return getNode().getLayoutX();
	}

	public double getY()
	{
		return getNode().getLayoutY();
	}

	public double getWidth()
	{
		return getNode().getBoundsInLocal().getWidth();
	}

	public double getHeight()
	{
		return getNode().getBoundsInLocal().getHeight();
	}

	private void revert()
	{
		if( !isRevert() || isAcceptable() )
		{
			hide();
			return;
		}

		DoubleProperty xProp = new SimpleDoubleProperty( getX() );
		xProp.addListener( new ChangeListener<Number>()
		{
			@Override
			public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
			{
				setX( newValue.doubleValue() );
			}
		} );
		DoubleProperty yProp = new SimpleDoubleProperty( getY() );
		yProp.addListener( new ChangeListener<Number>()
		{
			@Override
			public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
			{
				setY( newValue.doubleValue() );
			}
		} );

		TimelineBuilder
				.create()
				.keyFrames(
						new KeyFrame( REVERT_DURATION, new KeyValue( xProp, startPoint.getX(), Interpolator.EASE_BOTH ),
								new KeyValue( yProp, startPoint.getY(), Interpolator.EASE_BOTH ) ) )
				.onFinished( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent event )
					{
						hide();
					}
				} ).build().playFromStart();
	}

	private void hide()
	{
		if( getNode() != null && getNode().getScene() != null )
		{
			UIUtils.getOverlayFor( getNode().getScene() ).hide( getNode() );
		}
		DragNode.this.setVisible( false );
		DragNode.this.getDragSource().setVisible( true );
	}

	private static class DragNodeBehavior
	{
		private final EventHandler<MouseEvent> PRESSED_HANDLER = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				Node source = (Node) event.getSource();
				DragNode dragNode = (DragNode) source.getProperties().get( DRAG_NODE_PROP_KEY );
				if( dragNode != null )
				{
					double xOffset = dragNode.getNode().getLayoutBounds().getMinX();
					double yOffset = dragNode.getNode().getLayoutBounds().getMinY();

					dragNode.startPoint = new Point2D( event.getSceneX() - dragNode.getWidth() / 2 - xOffset, event.getSceneY()
							- dragNode.getHeight() / 2 - yOffset );
					dragNode.setX( dragNode.startPoint.getX() );
					dragNode.setY( dragNode.startPoint.getY() );
					dragNode.setVisible( true );

					UIUtils.getOverlayFor( source.getScene() ).show( dragNode.getNode() );

					if( dragNode.isHideOriginalNodeWhenDragging() )
						source.setVisible( false );

					dragNode.setDragging( true );
					dragNode.dragSource.fireEvent( new DraggableEvent( null, dragNode.dragSource, dragNode.getNode(),
							DraggableEvent.DRAGGABLE_STARTED, dragNode, event.getSceneX(), event.getSceneY() ) );
				}
			}
		};

		private final EventHandler<MouseEvent> DRAGGED_HANDLER = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				Node source = (Node) event.getSource();
				final DragNode dragNode = (DragNode) source.getProperties().get( DRAG_NODE_PROP_KEY );
				if( dragNode != null )
				{
					positionNodeAtMouseEvent( event, dragNode );

					Window window = source.getScene().getWindow();

					Point2D scenePoint = new Point2D( event.getSceneX(), event.getSceneY() );
					Node currentNode = null;
					while( currentNode == null && window != null )
					{
						Scene scene = window.getScene();
						scenePoint = new Point2D( event.getScreenX() - window.getX() - scene.getX(), event.getScreenY()
								- window.getY() - scene.getY() );
						currentNode = NodeUtils.findFrontNodeAtCoordinate( scene.getRoot(), scenePoint, dragNode.getNode(),
								UIUtils.getOverlayFor( window.getScene() ) );

						window = UIUtils.getParentWindow( window );
					}

					dragNode.lastPoint = scenePoint;

					if( dragNode.currentlyHovered != currentNode )
					{
						dragNode.setAcceptable( false );
						if( dragNode.currentlyHovered != null )
						{
							dragNode.currentlyHovered.fireEvent( new DraggableEvent( null, dragNode.getNode(),
									dragNode.currentlyHovered, DraggableEvent.DRAGGABLE_EXITED, dragNode, event.getSceneX(),
									event.getSceneY() ) );
						}
						if( currentNode != null )
						{
							currentNode.fireEvent( new DraggableEvent( new Runnable()
							{
								@Override
								public void run()
								{
									dragNode.setAcceptable( true );
								}
							}, dragNode.getNode(), currentNode, DraggableEvent.DRAGGABLE_ENTERED, dragNode, scenePoint.getX(),
									scenePoint.getY() ) );
						}

						dragNode.currentlyHovered = currentNode;
					}

					dragNode.dragSource.fireEvent( new DraggableEvent( null, dragNode.dragSource, dragNode.getNode(),
							DraggableEvent.DRAGGABLE_DRAGGED, dragNode, scenePoint.getX(), scenePoint.getY() ) );
				}
			}

			private void positionNodeAtMouseEvent( MouseEvent event, DragNode dragNode )
			{
				double xOffset = dragNode.getNode().getLayoutBounds().getMinX();
				double yOffset = dragNode.getNode().getLayoutBounds().getMinY();
				dragNode.setX( event.getSceneX() - dragNode.getWidth() / 2 - xOffset );
				dragNode.setY( event.getSceneY() - dragNode.getHeight() / 2 - yOffset );
			}
		};

		private final EventHandler<MouseEvent> RELEASED_HANDLER = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				onReleased.fireInvalidation();

				Node source = (Node) event.getSource();
				final DragNode dragNode = (DragNode) source.getProperties().get( DRAG_NODE_PROP_KEY );

				if( dragNode != null )
				{
					if( dragNode.currentlyHovered != null )
					{
						dragNode.currentlyHovered.fireEvent( new DraggableEvent( null, dragNode.getNode(),
								dragNode.currentlyHovered, DraggableEvent.DRAGGABLE_EXITED, dragNode, event.getSceneX(), event
								.getSceneY() ) );

						if( dragNode.isAcceptable() )
						{
							dragNode.currentlyHovered.fireEvent( new DraggableEvent( null, dragNode.getNode(),
									dragNode.currentlyHovered, DraggableEvent.DRAGGABLE_DROPPED, dragNode, dragNode.lastPoint
									.getX(), dragNode.lastPoint.getY() ) );
						}
					}

					dragNode.currentlyHovered = null;
					dragNode.revert();
					dragNode.setAcceptable( false );
					dragNode.setDragging( false );
					dragNode.dragSource.fireEvent( new DraggableEvent( null, dragNode.dragSource, dragNode.getNode(),
							DraggableEvent.DRAGGABLE_STOPPED, dragNode, event.getSceneX(), event.getSceneY() ) );
				}
			}
		};

		private void install( Node node, DragNode dragNode )
		{
			if( node == null )
			{
				return;
			}

			dragNode.dragSource = node;
			node.getProperties().put( DRAG_NODE_PROP_KEY, dragNode );
			node.addEventHandler( MouseEvent.DRAG_DETECTED, PRESSED_HANDLER );
			node.addEventHandler( MouseEvent.MOUSE_DRAGGED, DRAGGED_HANDLER );
			node.addEventHandler( MouseEvent.MOUSE_RELEASED, RELEASED_HANDLER );
		}

		private void uninstall( Node node )
		{
			if( node == null )
			{
				return;
			}

			node.removeEventHandler( MouseEvent.DRAG_DETECTED, PRESSED_HANDLER );
			node.removeEventHandler( MouseEvent.MOUSE_DRAGGED, DRAGGED_HANDLER );
			node.removeEventHandler( MouseEvent.MOUSE_RELEASED, RELEASED_HANDLER );
			node.getProperties().remove( DRAG_NODE_PROP_KEY );
		}
	}
}
