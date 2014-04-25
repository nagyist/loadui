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
package com.eviware.loadui.ui.fx.api.analysis;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.paint.Color;

import com.eviware.loadui.util.statistics.ZoomLevel;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.api.statistics.store.Execution;

public interface ExecutionChart
{
	public void setZoomLevel( ZoomLevel zoomLevel );

	public void setChartProperties( final ObservableValue<Execution> currentExecution, Observable poll );

	public Node getNode();

	public double getPosition();

	public void setPosition( double position );

	public LineChart<Long, Number> getLineChart();

	public long getSpan();

	public StringProperty titleProperty();

	public BooleanProperty scrollbarFollowStateProperty();

	public ZoomLevel getTickZoomLevel();

	public Color getColor( Segment segment, Execution execution );

	public Execution getCurrentExecution();
}
