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
package com.eviware.loadui.ui.fx.views.project;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.views.canvas.PlayButton;
import com.eviware.loadui.ui.fx.views.canvas.ToolbarPlaybackPanel;
import javafx.beans.property.Property;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBoxBuilder;

final public class ProjectPlaybackPanel extends ToolbarPlaybackPanel<ProjectItem>
{
	private ToggleButton linkButton;
	private HBox linkButtonContainer;
	private Property<Boolean> linkedProperty;
	private HBox playbackContainer;

	public ProjectPlaybackPanel( ProjectItem canvas )
	{
		super( canvas );
		linkButtonContainer = HBoxBuilder.create().build();
		playbackContainer = HBoxBuilder.create().alignment( Pos.CENTER ).spacing( 9 ).children(
				separator(),
				playButton,
				separator(),
				time,
				requests,
				failures,
				resetButton(),
				limitsButton()
		).build();

		getChildren().setAll( VBoxBuilder.create().spacing( 0 ).children(
				playbackContainer,
				linkButtonContainer
		).build() );

		setPadding( new Insets( 7, 0, 0, 0 ) );
		getStyleClass().add( "project-playback-panel" );
		setMaxWidth( 750 );
	}

	public ToggleButton addLinkButton( SceneItem scenario )
	{

		if( !linkButtonContainer.getChildren().isEmpty() )
		{
			linkButtonContainer.getChildren().clear();
		}

		linkedProperty = getLinkedProperty( scenario );
		linkButton = linkScenarioButton( linkedProperty );

		linkButton.selectedProperty().bindBidirectional( linkedProperty );
		playButton.bindToSelectedProperty( linkButton.disableProperty() );

		linkButtonContainer.getChildren().add( linkButton );
		return linkButton;
	}

	public boolean removeLinkButton()
	{
		if( !linkButtonContainer.getChildren().isEmpty() )
		{
			linkButton.selectedProperty().unbindBidirectional( linkedProperty );
			linkButton.disableProperty().unbind();
			linkButtonContainer.getChildren().clear();
			return true;
		}
		else
		{
			return false;
		}
	}

	public PlayButton getPlayButton()
	{
		return playButton;
	}

	public void addToPanel( Node node )
	{
		playbackContainer.getChildren().add( 3, node );
	}
}
