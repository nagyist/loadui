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
package com.eviware.loadui.impl.statistics;

import com.eviware.loadui.api.statistics.*;
import com.eviware.loadui.api.statistics.store.Entry;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

/**
 * StatisticsWriter for calculating the average of given values.
 * 
 * @author dain.nilsson
 */
public class SampleStatisticsWriter extends AbstractStatisticsWriter
{
	public static final String TYPE = "SAMPLE";

	@SuppressWarnings( "unused" )
	private static final Logger log = LoggerFactory.getLogger( SampleStatisticsWriter.class );

	double sum = 0.0;
	long count = 0L;

	private final PriorityQueue<Double> sortedValues = new PriorityQueue<>();

	public SampleStatisticsWriter( StatisticsManager statisticsManager, StatisticVariable variable,
			Map<String, Class<? extends Number>> trackStructure, Map<String, Object> config )
	{
		super( statisticsManager, variable, trackStructure, config, new Aggregator() );
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public void update( long timestamp, Number value )
	{
		synchronized( this )
		{
			double doubleValue = value.doubleValue();
			this.sortedValues.add( doubleValue );
			sum += doubleValue;
			count++ ;
			if( lastTimeFlushed + delay <= System.currentTimeMillis() )
			{
				flush();
			}
		}
	}

	@Override
	public Entry output()
	{
		if( count == 0 )
		{
			return null;
		}
		double average = 0.0;
		double stdDev = 0.0;
		double sumTotalSquare = 0.0;
		double percentile25 = 0;
		double percentile50 = 0;
		double percentile75 = 0;
		double percentile90 = 0;

		average = sum / count;
		sumTotalSquare = 0;

		double previousValue = 0;
		int upperPercPos25 = 0, upperPercPos50 = 0, upperPercPos75 = 0, upperPercPos90 = 0;
		double diff25 = 0, diff50 = 0, diff75 = 0, diff90 = 0;

		// percentile precalculations
		if( sortedValues.size() != 1 )
		{
			double percentilePos25 = 0.25 * ( sortedValues.size() - 1 );
			double percentilePos50 = 0.50 * ( sortedValues.size() - 1 );
			double percentilePos75 = 0.75 * ( sortedValues.size() - 1 );
			double percentilePos90 = 0.9 * ( sortedValues.size() - 1 );
			if( percentilePos90 >= sortedValues.size() - 1 )
			{
				upperPercPos90 = sortedValues.size() - 1;
			}
			else
			{
				upperPercPos90 = ( int )Math.floor( percentilePos90 ) + 1;
			}
			upperPercPos75 = ( int )Math.floor( percentilePos75 ) + 1;
			upperPercPos50 = ( int )Math.floor( percentilePos50 ) + 1;
			upperPercPos25 = ( int )Math.floor( percentilePos25 ) + 1;
			diff25 = percentilePos25 - Math.floor( percentilePos25 );
			diff50 = percentilePos50 - Math.floor( percentilePos50 );
			diff75 = percentilePos75 - Math.floor( percentilePos75 );
			diff90 = percentilePos90 - Math.floor( percentilePos90 );
		}

		Double value;
		double min = sortedValues.peek();
		if( sortedValues.size() > 1 )
		{
			int i = 0;
			while( ( value = sortedValues.poll() ) != null )
			{
				sumTotalSquare += Math.pow( value - average, 2 );
				if( i == upperPercPos25 )
					percentile25 = previousValue + diff25 * ( value - previousValue );
				if( i == upperPercPos50 )
					percentile50 = previousValue + diff50 * ( value - previousValue );
				if( i == upperPercPos75 )
					percentile75 = previousValue + diff75 * ( value - previousValue );
				if( i == upperPercPos90 )
					percentile90 = previousValue + diff90 * ( value - previousValue );
				previousValue = value;
				i++ ;
			}
		}
		else
		{
			previousValue = percentile25 = percentile50 = percentile75 = percentile90 = min;
		}
		double max = previousValue;

		stdDev = Math.sqrt( sumTotalSquare / count );
		// percentile = perc.evaluate( pValues );

		lastTimeFlushed = System.currentTimeMillis();

		Entry e = at( lastTimeFlushed ).put( SampleStats.AVERAGE.name(), average ).put( SampleStats.COUNT.name(), count )
				.put( SampleStats.SUM.name(), sum ).put( SampleStats.STD_DEV_SUM.name(), sumTotalSquare )
				.put( SampleStats.STD_DEV.name(), stdDev ).put( SampleStats.PERCENTILE_25TH.name(), percentile25 )
				.put( SampleStats.PERCENTILE_75TH.name(), percentile75 ).put( SampleStats.PERCENTILE_90TH.name(), percentile90 )
				.put( SampleStats.MEDIAN.name(), percentile50 ).put( SampleStats.MIN.name(), min ).put( SampleStats.MAX.name(), max ).build();

		// reset counters
		sum = 0;
		count = 0;
		sortedValues.clear();
		return e;
	}

	@Override
	public void reset()
	{
		super.reset();
		sortedValues.clear();
		sum = 0.0;
		count = 0L;
	}

	private static class Aggregator implements EntryAggregator
	{
		/**
		 * Aggregates a list of Entries.
		 * 
		 * Note that we are using Population based Standard deviation, as opposed
		 * to Sample based Standard deviation.
		 * 
		 * The percentile calculation assumes that the values are normally
		 * distributed. This assumption might be wrong, but has to be done to
		 * avoid storing and iterating through the whole set of actual values
		 * provided by the loadUI components.
		 * 
		 * @author henrik.olsson
		 */
		@Override
		public Entry aggregate( Set<Entry> entries, boolean parallel )
		{
			if( entries.size() <= 1 )
				return Iterables.getFirst( entries, null );

			long timestamp = -1;
			double totalSum = 0;
			long totalCount = 0;
			double min = Double.MAX_VALUE;
			double max = 0;
			double median = 0, percentile25 = 0, percentile75 = 0, percentile90 = 0, stddev = 0;

			for( Entry e : entries )
			{
				long count = e.getValue( SampleStats.COUNT.name() ).longValue();
				double average = e.getValue( SampleStats.AVERAGE.name() ).doubleValue();

				timestamp = Math.max( timestamp, e.getTimestamp() );

				// median - not really median of all subpopulations, rather a weighted
				// average of the subpopulations' medians (performance reasons).
				median += count * e.getValue( SampleStats.MEDIAN.name() ).doubleValue();
				percentile25 += count * e.getValue( SampleStats.PERCENTILE_25TH.name() ).doubleValue();
				percentile75 += count * e.getValue( SampleStats.PERCENTILE_75TH.name() ).doubleValue();
				percentile90 += count * e.getValue( SampleStats.PERCENTILE_90TH.name() ).doubleValue();
				stddev += count * e.getValue( SampleStats.STD_DEV.name() ).doubleValue();

				// average
				totalSum += count * average;
				totalCount += count;

				min = Math.min( min, e.getValue( SampleStats.MIN.name() ).doubleValue() );
				max = Math.max( max, e.getValue( SampleStats.MAX.name() ).doubleValue() );
			}

			median = median / totalCount;
			percentile25 = percentile25 / totalCount;
			percentile75 = percentile75 / totalCount;
			percentile90 = percentile90 / totalCount;
			stddev = stddev / totalCount;

			double totalAverage = totalSum / totalCount;

			return at( timestamp ).put( SampleStats.AVERAGE.name(), totalAverage ).put( SampleStats.COUNT.name(), totalCount )
					.put( SampleStats.STD_DEV.name(), stddev ).put( SampleStats.PERCENTILE_90TH.name(), percentile90 )
					.put( SampleStats.PERCENTILE_25TH.name(), percentile25 ).put( SampleStats.PERCENTILE_75TH.name(), percentile75 )
					.put( SampleStats.MEDIAN.name(), median ).put( SampleStats.MIN.name(), min ).put( SampleStats.MAX.name(), max ).build();
		}
	}

	/**
	 * Factory for instantiating AverageStatisticWriters.
	 * 
	 * @author dain.nilsson
	 * 
	 *         Define what Statistics this writer should write in Track.
	 * 
	 */
	public static class Factory implements StatisticsWriterFactory
	{
		@Override
		public String getType()
		{
			return TYPE;
		}

		@Override
		public StatisticsWriter createStatisticsWriter( StatisticsManager statisticsManager, StatisticVariable variable,
				Map<String, Object> config )
		{
			Map<String, Class<? extends Number>> trackStructure = new TreeMap<>();

			// init statistics
			trackStructure.put( SampleStats.AVERAGE.name(), Double.class );
			trackStructure.put( SampleStats.MIN.name(), Double.class );
			trackStructure.put( SampleStats.MAX.name(), Double.class );
			// trackStructure.put( Stats.COUNT.name(), Double.class );
			// trackStructure.put( Stats.SUM.name(), Double.class );
			trackStructure.put( SampleStats.STD_DEV.name(), Double.class );
			// trackStructure.put( Stats.STD_DEV_SUM.name(), Double.class );
			trackStructure.put( SampleStats.PERCENTILE_25TH.name(), Double.class );
			trackStructure.put( SampleStats.PERCENTILE_75TH.name(), Double.class );
			trackStructure.put( SampleStats.PERCENTILE_90TH.name(), Double.class );
			trackStructure.put( SampleStats.MEDIAN.name(), Double.class );

			return new SampleStatisticsWriter( statisticsManager, variable, trackStructure, config );
		}
	}

	@Override
	public String getDescriptionForMetric( String metricName )
	{
		for( SampleStats s : SampleStats.values() )
		{
			if( s.name().equals( metricName ) )
				return s.description;
		}
		return null;
	}

}
