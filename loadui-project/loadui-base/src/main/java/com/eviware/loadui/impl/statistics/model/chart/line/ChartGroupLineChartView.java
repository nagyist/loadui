/*
 * Copyright 2011 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.statistics.model.chart.line;

import com.eviware.loadui.api.statistics.model.ChartGroup;

/**
 * LineChartView for a ChartGroup.
 * 
 * @author dain.nilsson
 */
public class ChartGroupLineChartView extends AbstractLineChartView
{
	private final ChartGroup chartGroup;

	public ChartGroupLineChartView( LineChartViewProvider provider, ChartGroup chartGroup )
	{
		super( provider, chartGroup, CHART_GROUP_PREFIX );
		this.chartGroup = chartGroup;
	}

	@Override
	protected void segmentAdded( LineSegment segment )
	{
		if( segment instanceof ChartLineSegment )
			putSegment( segment );
	}

	@Override
	protected void segmentRemoved( LineSegment segment )
	{
		if( segment instanceof ChartLineSegment )
			deleteSegment( segment );
	}

	@Override
	public String toString()
	{
		return chartGroup.getTitle();
	}
}