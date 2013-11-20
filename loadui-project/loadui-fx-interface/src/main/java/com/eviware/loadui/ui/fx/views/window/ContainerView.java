package com.eviware.loadui.ui.fx.views.window;

import com.eviware.loadui.util.ReleasableUtils;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.List;

public class ContainerView extends StackPane
{
	StackPane container = new StackPane();


	public ContainerView()
	{
		super();
		getChildren().setAll( container );

	}

	public boolean hasView()
	{
		return !container.getChildren().isEmpty();
	}

	public Node getView()
	{
		if( hasView() )
		{
			return container.getChildren().get( 0 );
		}
		else
		{
			throw new IllegalStateException( "ContainerView has no view" );
		}
	}

	public void setView( Node view )
	{
		releaseViewIfPossible();
		container.getChildren().setAll( view );
	}

	private void releaseViewIfPossible()
	{
		List<Node> children = container.getChildren();
		if( !hasView() ) return;

		Node child = children.get( 0 );
		ReleasableUtils.release( child );
	}
}