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
package com.eviware.loadui.ui.fx.views.analysis.reporting;

import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.util.ManualObservable;
import com.eviware.loadui.ui.fx.views.analysis.linechart.LineChartViewNode;
import com.google.common.collect.Maps;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.LineChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.eviware.loadui.util.statistics.ZoomLevel.ALL;

public class LineChartUtils
{
	protected static final Logger log = LoggerFactory.getLogger( LineChartUtils.class );

	public static Map<ChartView, Image> createImages( Collection<StatisticPage> pages,
																	  ObservableValue<Execution> executionProperty, Execution comparedExecution )
	{
		HashMap<ChartView, Image> images = Maps.newHashMap();
		for( StatisticPage page : pages )
			for( ChartGroup chartGroup : page.getChildren() )
				images.putAll( createImages( chartGroup, executionProperty, comparedExecution ) );

		return images;
	}

	public static Map<ChartView, Image> createImages( ChartGroup chartGroup,
																	  ObservableValue<Execution> executionProperty, Execution comparedExecution )
	{
		HashMap<ChartView, Image> images = new HashMap<>();

		ChartView groupChartView = chartGroup.getChartView();
		images.put( groupChartView, generateChartImage( groupChartView, executionProperty, comparedExecution ) );

		boolean agentsExpanded = Boolean.valueOf( chartGroup.getAttribute( "agentsExpanded", "false" ) );

		if( 1 == 2 /* "group".equals( expand ) */ )
		{
			for( Chart chart : chartGroup.getChildren() )
			{
				ChartView chartView = chartGroup.getChartViewForChart( chart );
				images.put( chartView, generateChartImage( chartView, executionProperty, comparedExecution ) );
			}
		}
		else if( agentsExpanded )
		{
			for( String source : chartGroup.getSources() )
			{
				ChartView chartView = chartGroup.getChartViewForSource( source );
				images.put( chartView, generateChartImage( chartView, executionProperty, comparedExecution ) );
			}
		}
		return images;
	}

	public static Map<ChartView, Image> createImages( Collection<StatisticPage> pages, Execution execution,
																	  Execution comparedExecution )
	{
		Property<Execution> executionProperty = new SimpleObjectProperty<>( execution );
		return createImages( pages, executionProperty, comparedExecution );
	}

	public static Image generateChartImage( ChartView chartView, ObservableValue<Execution> executionProperty,
														 Execution comparedExecution )
	{
		if( chartView instanceof LineChartView )
		{
			int height = Math.max( ( int )Double.parseDouble( chartView.getAttribute( "height", "0" ) ), 100 );
			return createImage( ( LineChartView )chartView, 505, height, executionProperty, comparedExecution );
		}
		return null;
	}

	public static Image createImage( LineChartView chartView, int width, int height,
												ObservableValue<Execution> executionProperty, Execution comparedExecution )
	{
		ManualObservable chartUpdater = new ManualObservable();

		final LineChartViewNode chartViewNode = new LineChartViewNode( executionProperty, chartView, chartUpdater );
		chartViewNode.setZoomLevel( ALL );

		final LineChart<Long, Number> lineChart = chartViewNode.getLineChart();

		Snapshotter snapshotter = new Snapshotter( chartViewNode, lineChart );
		return snapshotter.createSnapshot();
	}

}
