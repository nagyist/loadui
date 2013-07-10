package com.eviware.loadui.ui.fx.views.window;

import com.eviware.loadui.ui.fx.util.FXMLUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author Henrik
 */
public class Overlay extends Group
{
	private static final Logger log = LoggerFactory.getLogger( Overlay.class );

	@FXML
	private Rectangle overlayRectangle;

	public Overlay()
	{
		FXMLUtils.load( this );
	}

	@FXML
	public void initialize()
	{
		sceneProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable observable )
			{
				if( getScene() != null )
				{
					overlayRectangle.widthProperty().bind( getScene().widthProperty() );
					overlayRectangle.heightProperty().bind( getScene().heightProperty() );
				}
			}
		} );
	}

	public void show( Node... nodes )
	{
		getChildren().addAll( nodes );
	}

	public void hide( Node... nodes )
	{
		getChildren().removeAll( nodes );
	}

	public static class NoOpOverlay extends Overlay
	{

		public NoOpOverlay()
		{
			log.warn( "Creating a NoOpOverlay! This only happens when there's an error in the code, eg. " +
					"a top-level panel which does not implement OverlayHolder contains children which try" +
					" to use LoadUI's drag-and-drop feature." );
		}

		@Override
		public void initialize()
		{
			// No Op
		}

		@Override
		public void show( Node... nodes )
		{
			// No Op
		}

		@Override
		public void hide( Node... nodes )
		{
			// No Op
		}
	}

}
