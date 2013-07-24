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
import com.eviware.loadui.ui.fx.views.canvas.ToolbarPlaybackPanel;
import javafx.beans.property.Property;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

final public class ProjectPlaybackPanel extends ToolbarPlaybackPanel<ProjectItem>
{
	private VBox playButtonContainer;
	private ToggleButton linkButton;
	private Property<Boolean> linkedProperty;

	public ProjectPlaybackPanel( ProjectItem canvas )
	{
		super( canvas );

		playButtonContainer = VBoxBuilder.create().spacing( 0 ).children(
				HBoxBuilder.create().alignment( Pos.CENTER ).spacing( 9 ).children(
						separator(),
						playButton,
						separator(),
						time,
						requests,
						failures,
						resetButton(),
						limitsButton()
				).build()
		).build();

		setPadding( new Insets( 7, 0, 0, 0 ) );
		getStyleClass().add( "project-playback-panel" );
		setMaxWidth( 750 );

		getChildren().setAll(
				playButtonContainer
		);
	}

	public ToggleButton addLinkButton( SceneItem scenario )
	{

		if( playButtonContainer.getChildren().size() > 1 )
		{
			playButtonContainer.getChildren().remove( 1 );
		}

		linkedProperty = getLinkedProperty( scenario );
		linkButton = linkScenarioButton( linkedProperty );

		linkButton.selectedProperty().bindBidirectional( linkedProperty );
		linkButton.disableProperty().bind( playButton.selectedProperty() );

		playButtonContainer.getChildren().add( 1, linkButton );
		return linkButton;
	}

	public boolean removeLinkButton()
	{
		if( playButtonContainer.getChildren().size() > 1 )
		{
			linkButton.selectedProperty().unbindBidirectional( linkedProperty );
			linkButton.disableProperty().unbind();

			playButtonContainer.getChildren().remove( linkButton );
			return true;
		}
		else
		{
			return false;
		}
	}
}
