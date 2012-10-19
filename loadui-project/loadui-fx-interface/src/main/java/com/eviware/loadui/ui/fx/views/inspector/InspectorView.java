package com.eviware.loadui.ui.fx.views.inspector;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.TimelineBuilder;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabBuilder;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import com.eviware.loadui.api.ui.inspector.Inspector;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.google.common.base.Function;

public class InspectorView extends AnchorPane
{
	private final BooleanProperty minimizedProperty = new SimpleBooleanProperty( this, "minimized", true );

	private final ObservableList<Inspector> inspectors = FXCollections.observableArrayList();

	private StackPane tabHeaderArea;

	@FXML
	private TabPane tabPane;

	@FXML
	private HBox buttonBar;

	@FXML
	private Button helpButton;

	public InspectorView()
	{
		FXMLUtils.load( this );

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				init();
			}
		} );
	}

	public ObservableList<Inspector> getInspectors()
	{
		return inspectors;
	}

	private void init()
	{
		tabHeaderArea = ( StackPane )tabPane.lookup( ".tab-header-area" );
		tabHeaderArea.addEventHandler( MouseEvent.ANY, new DragBehavior() );

		buttonBar.setPrefHeight( tabHeaderArea.prefHeight( -1 ) );

		helpButton.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				String helpUrl = ( ( Inspector )tabPane.getSelectionModel().getSelectedItem().getUserData() ).getHelpUrl();
				if( helpUrl != null )
				{
					UIUtils.openInExternalBrowser( helpUrl );
				}
			}
		} );

		Bindings.bindContent( tabPane.getTabs(), ObservableLists.transform( inspectors, new Function<Inspector, Tab>()
		{
			@Override
			public Tab apply( Inspector inspector )
			{
				Object panel = inspector.getPanel();
				if( !( panel instanceof Node ) )
				{
					panel = new Label( "Unsupported inspector panel." );
				}
				return TabBuilder.create().userData( inspector ).text( inspector.getName() ).content( ( Node )panel )
						.build();
			}
		} ) );

		tabPane.getSelectionModel().selectedItemProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				double oldHeight = getMaxHeight();
				double newHeight = boundHeight( oldHeight );
				if( newHeight < oldHeight - 5.0 )
				{
					TimelineBuilder
							.create()
							.keyFrames(
									new KeyFrame( Duration.seconds( 0.1 ), new KeyValue( maxHeightProperty(), newHeight,
											Interpolator.EASE_BOTH ) ) ).build().playFromStart();
				}
				else
				{
					setMaxHeight( newHeight );
				}
			}
		} );

		setMaxHeight( boundHeight( 0 ) );
	}

	private double boundHeight( double desiredHeight )
	{
		double headerHeight = tabHeaderArea.prefHeight( -1 );
		Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
		if( selectedTab != null )
		{
			Node selectedNode = selectedTab.getContent();
			if( selectedNode != null )
			{
				desiredHeight = Math.min( desiredHeight, headerHeight + selectedNode.maxHeight( -1 ) );
			}
		}

		return Math.max( headerHeight, desiredHeight );
	}

	private final class DragBehavior implements EventHandler<MouseEvent>
	{
		private boolean dragging = false;
		private double startY = 0;
		private double lastHeight = 250;

		private final EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				minimizedProperty.set( !minimizedProperty.get() );
			}
		};

		@Override
		public void handle( MouseEvent event )
		{
			if( event.getEventType() == MouseEvent.MOUSE_PRESSED )
			{
				startY = event.getScreenY() + getHeight();
			}
			else if( event.getEventType() == MouseEvent.DRAG_DETECTED )
			{
				dragging = true;
				minimizedProperty.set( false );
			}
			else if( event.getEventType() == MouseEvent.MOUSE_DRAGGED )
			{
				if( dragging )
				{
					setMaxHeight( boundHeight( startY - event.getScreenY() ) );
				}
			}
			else if( event.getEventType() == MouseEvent.MOUSE_RELEASED )
			{
				dragging = false;
				if( getHeight() > getMaxHeight() )
				{
					minimizedProperty.set( true );
				}
			}
			else if( event.getEventType() == MouseEvent.MOUSE_CLICKED )
			{
				if( event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 )
				{
					double target = boundHeight( 0 );
					if( minimizedProperty.get() )
					{
						target = boundHeight( lastHeight );
					}
					else
					{
						lastHeight = getHeight();
					}

					TimelineBuilder
							.create()
							.keyFrames(
									new KeyFrame( Duration.seconds( 0.2 ), new KeyValue( maxHeightProperty(), target,
											Interpolator.EASE_BOTH ) ) ).onFinished( eventHandler ).build().playFromStart();
				}
			}
		}
	}
}