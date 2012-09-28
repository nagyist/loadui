package com.eviware.loadui.ui.fx.views.project;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import com.eviware.loadui.ui.fx.views.canvas.CounterDisplay;
import com.eviware.loadui.ui.fx.views.canvas.CounterDisplay.Formatting;
import com.eviware.loadui.ui.fx.views.canvas.PlaybackPanel;
import com.eviware.loadui.ui.fx.views.canvas.ToolbarCounterDisplay;

public class ProjectPlaybackPanel extends PlaybackPanel
{
	public ProjectPlaybackPanel()
	{
		setMaxHeight( 28 );
		setMaxWidth( 550 );
		setAlignment( Pos.CENTER );

		ComboBox<Image> distibutionMode = new ComboBox<>( FXCollections.observableArrayList( image( "mode-local.png" ),
				image( "mode-distributed.png" ) ) );
		final Callback<ListView<Image>, ListCell<Image>> cellFactory = new Callback<ListView<Image>, ListCell<Image>>()
		{
			@Override
			public ListCell<Image> call( ListView<Image> arg0 )
			{
				return new ListCell<Image>()
				{
					private final ImageView imageView = new ImageView();

					@Override
					protected void updateItem( Image item, boolean empty )
					{
						super.updateItem( item, empty );

						if( item == null || empty )
						{
							setGraphic( null );
						}
						else
						{
							imageView.setImage( item );
							setGraphic( imageView );
						}
					}
				};
			}
		};
		distibutionMode.setCellFactory( cellFactory );
		distibutionMode.setButtonCell( cellFactory.call( null ) );
		distibutionMode.getSelectionModel().selectFirst();

		getChildren().setAll( playStack(), separator(), distibutionMode, separator(), time, separator(), requests,
				separator(), failures, separator(), resetButton(), limitsButton() );
	}

	@Override
	protected CounterDisplay timeCounter()
	{
		return new ToolbarCounterDisplay( TIME, Formatting.TIME );
	}

	@Override
	protected CounterDisplay timeRequests()
	{
		return new ToolbarCounterDisplay( REQUESTS );
	}

	@Override
	protected CounterDisplay timeFailures()
	{
		return new ToolbarCounterDisplay( FAILURES );
	}
}
