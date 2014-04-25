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
package com.eviware.loadui.api.statistics;

import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;

import javax.annotation.CheckForNull;

/**
 * Writes statistics data to a Track. Each call to update allows the
 * StatisticsWriter to internally buffer data, which is aggregated and written
 * to the underlying storage periodically. A StatisticsWriter can expose several
 * Statistics which allow reading the stored statistics and current values.
 *
 * @author dain.nilsson
 */
public interface StatisticsWriter extends Addressable
{

	/**
	 * Updates the StatisticsWriter with new data, which may trigger data to be
	 * flushed to the underlying Track, or may just buffer it in memory.
	 *
	 * @param timestamp
	 * @param value
	 */
	public void update( long timestamp, Number value );

	/**
	 * Forces any buffered but not yet written data to be stored. This should
	 * manually be called when ending a test Execution.
	 */
	public void flush();

	/**
	 * Returns an Entry based on raw data acquired from calls to update().
	 *
	 * @return
	 */
	public Entry output();

	/**
	 * Gets the associated StatisticVariable.
	 *
	 * @return
	 */
	public StatisticVariable getStatisticVariable();

	/**
	 * Gets the Track for the StatisticsWriter, for the current Execution.
	 *
	 * @return
	 */
	public TrackDescriptor getTrackDescriptor();

	/**
	 * Gets the type of the StatisticsWriter, which should be unique. This can be
	 * the same as the associated StatisticsWriterFactory.getType().
	 *
	 * @return
	 */
	public String getType();

	/**
	 * Resets the state of the StatisticsWriter.
	 */
	public void reset();

	/**
	 * Get a description for a specific metric.
	 */
	@CheckForNull
	public String getDescriptionForMetric( String metricName );

	public enum SampleStats
	{
		AVERAGE( "The average %v." ),
		COUNT,
		SUM,
		STD_DEV( "The standard deviation of %v." ),
		STD_DEV_SUM,
		PERCENTILE_25TH( "The 25th percentile of %v." ),
		PERCENTILE_75TH( "The 75th percentile of %v." ),
		PERCENTILE_90TH( "The 90th percentile of %v." ),
		MEDIAN( "The median value of %v." ),
		MIN( "The mininum value of %v." ),
		MAX( "The maximum value of %v." );

		public final String description;

		SampleStats()
		{
			this.description = this.name() + " of %v.";
		}

		SampleStats( String description )
		{
			this.description = description;
		}
	}

	public enum CounterStats
	{
		TOTAL( "The number of %v in total since the last time the project was started or resetted." ),
		PER_SECOND( "The number of %v per second." );

		public final String description;

		CounterStats()
		{
			this.description = this.name() + " of %v.";
		}

		CounterStats( String description )
		{
			this.description = description;
		}
	}

	public enum VariableStats
	{
		VALUE( "The number of %v." );

		public final String description;

		VariableStats()
		{
			this.description = this.name() + " of %v.";
		}

		VariableStats( String description )
		{
			this.description = description;
		}
	}
}
