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
package com.eviware.loadui.ui.fx.views.canvas;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.execution.TestExecutionUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressIndicatorBuilder;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleButtonBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Region;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PlayButton extends StackPane implements Releasable
{
	private final ToggleButton toggleButton = ToggleButtonBuilder
			.create()
			.styleClass( "play-button" )
			.graphic(
					HBoxBuilder
							.create()
							.children( RegionBuilder.create().styleClass( "graphic" ).build(),
									RegionBuilder.create().styleClass( "secondary-graphic" ).build() ).build() ).build();

	private final CanvasItem canvas;
	private final BooleanProperty playingProperty = new SimpleBooleanProperty();
	private final BoundPropertiesManager selectedPropManager = new BoundPropertiesManager();
	private final CanvasRunningListener canvasRunningListener;

	protected static final Logger log = LoggerFactory.getLogger( PlayButton.class );

	protected final ChangeListener<Boolean> playCanvas = new ChangeListener<Boolean>()
	{
		@Override
		public void changed( ObservableValue<? extends Boolean> observable, Boolean wasPlaying, Boolean isPlaying )
		{
			log.debug( "Play Button state changed, isPlaying? " + isPlaying + ", isCanvasRunning? " + canvas.isRunning() );
			if( isPlaying && !canvas.isRunning() )
			{
				TestExecutionUtils.startCanvas( canvas );
			}
			else if( canvas.isRunning() && !isPlaying )
			{
				TestExecutionUtils.stopCanvas( canvas );
			}
		}
	};

	private PlayButton( @Nonnull final CanvasItem canvas )
	{
		this.canvas = canvas;
		playingProperty.addListener( playCanvas );
		playingProperty.setValue( canvas.isRunning() );
		toggleButton.selectedProperty().bindBidirectional( playingProperty );

		Circle border = CircleBuilder.create().styleClass( "play-button-border" ).radius( 14 ).build();
		Region inner = RegionBuilder.create().styleClass( "inner-spinner-overlay" ).build();
		Region outer = RegionBuilder.create().styleClass( "outer-spinner-overlay" ).build();
		ProgressIndicator indicator = ProgressIndicatorBuilder.create().build();

		inner.visibleProperty().bind( toggleButton.selectedProperty() );
		outer.visibleProperty().bind( toggleButton.selectedProperty() );
		indicator.visibleProperty().bind( toggleButton.selectedProperty() );
		border.visibleProperty().bind( toggleButton.selectedProperty().not() );

		canvasRunningListener = new CanvasRunningListener();
		canvas.addEventListener( BaseEvent.class, canvasRunningListener );

		getChildren().setAll( outer, indicator, inner, border, toggleButton );
		setMaxSize( 27, 27 );
	}

	public void bindToSelectedProperty( BooleanProperty property )
	{
		selectedPropManager.bindToSelectedProperty( property );
	}

	@Override
	public void release()
	{
		canvas.removeEventListener( BaseEvent.class, canvasRunningListener );
		selectedPropManager.removeAllBindings();
		log.debug( "Trying to delete button on canvas {}", canvas.getLabel() );
		PlayButtonFactory.release( this );
	}

	private class CanvasRunningListener implements EventHandler<BaseEvent>
	{

		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event.getKey().equals( CanvasItem.RUNNING ) )
				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						playingProperty.set( canvas.isRunning() );
					}
				} );
		}
	}

	private class BoundPropertiesManager
	{

		List<BooleanProperty> boundProps = Lists.newArrayList();

		public void bindToSelectedProperty( BooleanProperty property )
		{
			boundProps.add( property );
			property.bind( toggleButton.selectedProperty() );
		}

		public void removeAllBindings()
		{
			for( BooleanProperty property : boundProps )
				property.unbind();
			boundProps.clear();
		}

	}

	public static final class PlayButtonFactory
	{

		private static PlayButtonFactory instance;

		private final ChangeListener<? super Boolean> buttonListener = new ChangeListener<Boolean>()
		{
			@Override
			public void changed( ObservableValue<? extends Boolean> observable, Boolean wasOn, Boolean isOn )
			{
				disableAllExcept( observable, isOn );
			}
		};

		public static synchronized PlayButtonFactory getInstance()
		{
			if( instance == null )
			{
				instance = new PlayButtonFactory();
			}
			return instance;
		}

		private List<PlayButton> buttons = Lists.newArrayList();

		private Predicate<PlayButton> isButtonSelected = new Predicate<PlayButton>()
		{
			@Override
			public boolean apply( @Nullable PlayButton input )
			{
				return input != null && input.toggleButton.selectedProperty().get();
			}
		};

		public PlayButton createButtonFor( CanvasItem canvas )
		{
			log.debug( "Creating PlayButton for canvas {}", canvas.getLabel() );
			PlayButton button = new PlayButton( canvas );
			button.toggleButton.selectedProperty().addListener( buttonListener );
			if( Iterables.any( buttons, isButtonSelected ) )
				button.toggleButton.setDisable( true );
			buttons.add( button );
			return button;
		}

		private static void release( PlayButton button )
		{
			boolean rem = instance.buttons.remove( button );
			log.debug( "AFTER REMOVING BUTTON, THERE ARE {} BUTTONS, REMOVED ANY? {}", instance.buttons.size(), rem );
		}

		private void disableAllExcept( ObservableValue<?> observable, boolean disableOthers )
		{
			log.debug( "Disabling buttons, currently there are {} buttons", buttons.size() );
			for( PlayButton button : buttons )
			{
				boolean isOther = button.toggleButton.selectedProperty() != observable;
				if( isOther )
					button.setDisable( disableOthers );
			}
		}

	}


}
