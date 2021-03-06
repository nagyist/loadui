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
package com.eviware.loadui.ui.fx.views.analysis.linechart;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.ui.fx.MenuItemsProvider;
import com.eviware.loadui.ui.fx.MenuItemsProvider.HasMenuItems;
import com.eviware.loadui.ui.fx.MenuItemsProvider.Options;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.views.analysis.StatisticsDialog;
import com.eviware.loadui.util.statistics.ChartUtils;

public abstract class SegmentView<T extends Segment> extends StackPane implements Releasable, Deletable
{
	public static final String COLOR_ATTRIBUTE = "color";
	protected static final Logger log = LoggerFactory.getLogger( SegmentView.class );

	protected final T segment;

	protected final LineChartView lineChartView;

	@FXML
	protected Label segmentLabel;

	@FXML
	protected Rectangle legendColorRectangle;

	protected String color;

	public SegmentView( T segment, LineChartView lineChartView )
	{
		this.segment = segment;
		this.lineChartView = lineChartView;
		color = segment.getAttribute( COLOR_ATTRIBUTE, "no_color" );
	}

	public T getSegment()
	{
		return segment;
	}

	private String newColor()
	{
		LineChartView mainChart = ( LineChartView )( lineChartView.getChartGroup().getChartView() );

		ArrayList<String> currentColorList = new ArrayList<>();

		for( Segment s : mainChart.getSegments() )
		{
			currentColorList.add( s.getAttribute( COLOR_ATTRIBUTE, "no_color" ) );
		}

		return ChartUtils.getNewRandomColor( currentColorList );
	}

	public void setColor( String color )
	{
		this.color = color;
		legendColorRectangle.setFill( Color.web( color ) );
		segment.setAttribute( COLOR_ATTRIBUTE, color );
	}

	protected void init()
	{
		if( color.equals( "no_color" ) )
			setColor( newColor() );
		else
			legendColorRectangle.setFill( Color.web( color ) );
	}

	/**
	 * Sub-classes may set a default array of MenuItems on the given button with
	 * this method.
	 * 
	 * @param menuButton
	 *           to hold the menu
	 */
	protected void setMenuItemsFor( final MenuButton menuButton )
	{
		final HasMenuItems hasMenuItems = MenuItemsProvider.createWith( this, this,
				Options.are().delete( "Remove", false, new Runnable()
				{
					@Override
					public void run()
					{
						log.debug( "Removed SegmentView successfully" );
						if( !StatisticsDialog.thereAreSegmentsIn( lineChartView.getChartGroup() ) )
							lineChartView.getChartGroup().delete();
					}
				} ) );
		menuButton.getItems().setAll( hasMenuItems.items() );

		// this platform.runlater is here because of a bugfix related to not being able to see charts in reports when running from command line (LOADUI-781)
		Platform.runLater( new Runnable()
		{

			@Override
			public void run()
			{
				final ContextMenu ctxMenu = ContextMenuBuilder.create().items( hasMenuItems.items() ).build();

				Bindings.bindContentBidirectional( ctxMenu.getItems(), menuButton.getItems() );

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
		} );

	}

	@Override
	public void delete()
	{
		( ( Segment.Removable )segment ).remove();
	}

	@Override
	public void release()
	{
		Object listeners = getProperties().get( "Listeners" );
		Object targets = getProperties().get( "ListenerTargets" );
		if( listeners != null )
		{
			List<?> invalidationListeners = ( List<?> )listeners;
			List<?> observables = ( List<?> )targets;
			for( Object o : observables )
			{
				for( Object l : invalidationListeners )
				{
					if( l instanceof InvalidationListener && o instanceof Observable )
					{
						log.debug( "REMOVING LISTENER " + l + " from " + o );
						( ( Observable )o ).removeListener( ( InvalidationListener )l );
					}
				}
			}
		}
		getProperties().clear();

	}
}
