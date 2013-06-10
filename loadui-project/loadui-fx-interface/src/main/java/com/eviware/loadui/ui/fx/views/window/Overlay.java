package com.eviware.loadui.ui.fx.views.window;

import com.eviware.loadui.ui.fx.util.FXMLUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

/**
 * @Author Henrik
 */
public class Overlay extends Group
{
	@FXML
	private Rectangle rectangle;

	public Overlay()
	{
		FXMLUtils.load(this);
	}

	@FXML
	public void initialize()
	{
		sceneProperty().addListener(new InvalidationListener()
		{
			@Override
			public void invalidated( Observable observable )
			{
				if( getScene() != null )
				{
					rectangle.widthProperty().bind(getScene().widthProperty());
					rectangle.heightProperty().bind(getScene().heightProperty());
				}
			}
		});
	}

	public void show( Node... nodes )
	{
		getChildren().addAll(nodes);
	}

	public void hide( Node... nodes )
	{
		getChildren().removeAll(nodes);
	}
}
