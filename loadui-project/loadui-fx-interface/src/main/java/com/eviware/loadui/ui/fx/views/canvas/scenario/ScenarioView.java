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
package com.eviware.loadui.ui.fx.views.canvas.scenario;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.ui.fx.MenuItemsProvider;
import com.eviware.loadui.ui.fx.MenuItemsProvider.HasMenuItems;
import com.eviware.loadui.ui.fx.MenuItemsProvider.Options;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.views.canvas.CanvasObjectView;
import com.eviware.loadui.ui.fx.views.canvas.MiniScenarioPlaybackPanel;
import com.eviware.loadui.ui.fx.views.canvas.PlaybackPanel;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class ScenarioView extends CanvasObjectView implements Releasable
{
	public static final String HELP_PAGE = "http://loadui.org/Working-with-loadUI/scenarios.html";
	private final Options MENU_ITEM_OPTIONS = Options.are().open();
	private final Controller controller;

	public ScenarioView( SceneItem scenario )
	{
		super( scenario );
		getStyleClass().add( "scenario-view" );
		controller = new Controller();

		FXMLUtils.load( content, controller,
				ScenarioView.class.getResource( ScenarioView.class.getSimpleName() + ".fxml" ) );

		HasMenuItems hasMenuItems = MenuItemsProvider.createWith( this, getCanvasObject(), MENU_ITEM_OPTIONS );
		menuButton.getItems().setAll( hasMenuItems.items() );
		final ContextMenu ctxMenu = ContextMenuBuilder.create().items( hasMenuItems.items() ).build();

		setOnContextMenuRequested( new EventHandler<ContextMenuEvent>()
		{
			@Override
			public void handle( ContextMenuEvent event )
			{
				// never show contextMenu when on top of the menuButton
				if( !NodeUtils.isMouseOn( menuButton ) )
				{
					MenuItemsProvider.showContextMenu( menuButton, ctxMenu );
					event.consume();
				}
			}
		} );
	}

	public SceneItem getScenario()
	{
		return ( SceneItem )getCanvasObject();
	}

	@Override
	public void delete()
	{
		log.debug( "Deleting scenario view for {}", getCanvasObject().getLabel() );
		super.delete();
	}

	@Override
	public void release()
	{
		log.debug( "Releasing scenario view for {}", getCanvasObject().getLabel() );
		controller.release();
	}

	private final class Controller implements Releasable
	{
		@FXML
		private VBox vBox;

		@FXML
		private ImageView miniature;

		private PlaybackPanel playbackPanel;

		@FXML
		void initialize()
		{
			playbackPanel =  new MiniScenarioPlaybackPanel( getScenario() );
			vBox.getChildren().add( 0, playbackPanel );
			String base64 = getScenario().getAttribute( "miniature_fx2", null );

			if( base64 != null )
				miniature.setImage( NodeUtils.fromBase64Image( base64 ) );
			else
				miniature.setImage( new Image( ScenarioView.class.getResourceAsStream( "grid.png" ) ) );

			miniature.setOnMouseClicked( new EventHandler<MouseEvent>()
			{
				@Override
				public void handle( MouseEvent event )
				{
					if( event.getButton() == MouseButton.PRIMARY )
					{
						if( event.getClickCount() == 2 )
						{
							fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, getCanvasObject() ) );
						}
					}
				}
			} );
		}

		@Override
		public void release()
		{
			 playbackPanel.release();
		}
	}
}
