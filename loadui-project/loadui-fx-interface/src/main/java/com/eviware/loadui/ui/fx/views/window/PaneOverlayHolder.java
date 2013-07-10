package com.eviware.loadui.ui.fx.views.window;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class PaneOverlayHolder extends Pane implements OverlayHolder
{

	private Overlay overlay = new Overlay();

	public PaneOverlayHolder()
	{
		overlay.setId( "Overlay-for-drag-n-drop" );
		getChildren().add( overlay );
	}

	@Override
	public Overlay getOverlay()
	{
		return overlay;
	}

	public void add( Node child )
	{
		getChildren().add( getChildren().size() - 1, child );
	}
}
