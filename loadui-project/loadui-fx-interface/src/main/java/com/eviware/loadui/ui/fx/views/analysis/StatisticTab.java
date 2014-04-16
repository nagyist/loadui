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
package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static com.eviware.loadui.ui.fx.util.Properties.forLabel;
import static com.google.common.base.Strings.isNullOrEmpty;

import com.eviware.loadui.util.StringUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.Chart.Owner;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.ui.fx.api.NonSingletonFactory;
import com.eviware.loadui.ui.fx.api.analysis.ChartGroupView;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.DefaultNonSingletonFactory;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Function;

public class StatisticTab extends Tab implements Releasable
{
	private static final Logger log = LoggerFactory.getLogger( StatisticTab.class );
	private final StatisticPage page;
	private final Observable poll;
	private ObservableList<ChartGroupView> chartGroupViews;
	private ObservableList<Node> chartGroupNodes;
	private StringProperty tabTitle;

	@FXML
	protected VBox chartList;

	private final Function<ChartGroupView, Node> chartGroupViewToNode = new Function<ChartGroupView, Node>()
	{
		@Override
		public Node apply( ChartGroupView chartGroupView )
		{
			return chartGroupView.getNode();
		}
	};

	protected NonSingletonFactory getNonSingletonFactory()
	{
		NonSingletonFactory factory = BeanInjector.getNonCachedBeanOrNull( NonSingletonFactory.class );
		if( factory != null )
			return factory;
		else
			return DefaultNonSingletonFactory.get();
	}

	public final static StatisticPage createStatisticPage( StatisticPages pages, @Nullable String label )
	{
		return pages.createPage( isNullOrEmpty( label ) ? "Page " + ( pages.getChildCount() + 1 ) : label );
	}

	public static final ChartGroup createChartGroup( StatisticPage page, String chartType, String label )
	{
		return page.createChartGroup( isNullOrEmpty( chartType ) ? LineChartView.class.getName() : chartType,
				isNullOrEmpty( label ) ? "Chart " + ( page.getChildCount() + 1 ) : label );
	}

	public StatisticTab( StatisticPage page, Observable poll )
	{
		this.page = page;
		this.poll = poll;
		FXMLUtils.load( this );
	}

	public void setCurrentExecution( final ObservableValue<Execution> currentExecution )
	{
		currentExecution.addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				// good for general debugging purposes
				if( currentExecution.getValue() != null )
					log.info( "currentExecution set to: {} with id: {} ", currentExecution.getValue().getLabel(),
							currentExecution.getValue().getId() );
				else
					log.info( "currentExecution set to: null" );
			}
		} );

		chartGroupViews = transform( fx( ofCollection( page ) ), new Function<ChartGroup, ChartGroupView>()
		{
			@Override
			public ChartGroupView apply( ChartGroup chartGroup )
			{
				return getNonSingletonFactory().createChartGroupView( chartGroup, currentExecution, poll );
			}
		} );
		chartGroupNodes = ObservableLists.transform( chartGroupViews, chartGroupViewToNode );
		ObservableLists.releaseElementsWhenRemoved( chartGroupNodes );

		Bindings.bindContent( chartList.getChildren(), chartGroupNodes );
	}

	@FXML
	private void initialize()
	{
		tabTitle = forLabel( page );
		textProperty().bindBidirectional( tabTitle );
		setId( StringUtils.toCssName( page.getLabel() ) );

		MenuItem renameItem = new MenuItem( "Rename\u2026" );
		renameItem.setId( "tab-rename" );
		renameItem.setOnAction( new EventHandler<ActionEvent>()
		{
			public void handle( ActionEvent _ )
			{
				chartList.fireEvent( IntentEvent.create( IntentEvent.INTENT_RENAME, page ) );
			}
		} );
		MenuItem deleteItem = new MenuItem( "Delete" );
		deleteItem.setId( "tab-delete" );
		deleteItem.setOnAction( new EventHandler<ActionEvent>()
		{
			public void handle( ActionEvent e )
			{
				getOnClosed().handle( e );
			}
		} );

		ContextMenu menu = new ContextMenu();
		menu.getItems().addAll( renameItem, deleteItem );
		setContextMenu( menu );

		setOnClosed( new EventHandler<Event>()
		{
			@Override
			public void handle( Event _ )
			{
				release();
				page.delete();
			}
		} );

		getContent().addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( DraggableEvent event )
			{
				if( event.getData() instanceof Chart.Owner )
				{
					if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED )
					{
						//TODO make content area somehow highlighted
						event.accept();
						event.consume();
					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
					{
						Owner owner = ( Owner )event.getData();
						log.debug( "Creating new Chart Group" );
						ChartGroup group = createChartGroup( page, null, null );
						StatisticDroppedHandler.createSubChart( chartList, group, owner );
						event.consume();
					}
				}
			}
		} );
	}

	@Override
	public String toString()
	{
		return page.getLabel();
	}

	public void release()
	{
		for( Node node : chartGroupNodes )
			NodeUtils.releaseRecursive( node );
	}

}
