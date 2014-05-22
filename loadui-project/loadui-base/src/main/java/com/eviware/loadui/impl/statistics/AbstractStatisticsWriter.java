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

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.statistics.*;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.statistics.StatisticVariableIdentifierImpl;
import com.eviware.loadui.util.statistics.store.EntryImpl;
import com.eviware.loadui.util.statistics.store.TrackDescriptorImpl;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class AbstractStatisticsWriter implements StatisticsWriter
{
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger( AbstractStatisticsWriter.class );

	public static final String DELAY = "delay";

	private final StatisticVariable variable;
	private final TrackDescriptor descriptor;

	private final StatisticsAggregator aggregator;

	protected long delay;
	protected long lastTimeFlushed = System.currentTimeMillis();

	private long lastFlushed;

	private final Map<String, Object> config;

	public AbstractStatisticsWriter( StatisticsManager manager, StatisticVariable variable,
												Map<String, Class<? extends Number>> values, Map<String, Object> config, EntryAggregator entryAggregator )
	{
		this.config = config;
		this.variable = variable;
		StatisticVariableIdentifier identifier = new StatisticVariableIdentifierImpl(
				variable.getStatisticHolder().getId(), variable.getLabel(), getType() );

		descriptor = new TrackDescriptorImpl( identifier, values, entryAggregator );

		delay = config.containsKey( DELAY ) ? ( ( Number )config.get( DELAY ) ).longValue() : manager
				.getMinimumWriteDelay();

		// FIXME this line may cause bug LOADUI-1447
		if( LoadUI.isController() )
			manager.getExecutionManager().registerTrackDescriptor( descriptor );

		aggregator = BeanInjector.getBean( StatisticsAggregator.class );
	}

	protected Map<String, Object> getConfig()
	{
		return config;
	}

	@Override
	public void reset()
	{
		lastTimeFlushed = System.currentTimeMillis();
	}

	@Override
	public String getId()
	{
		return descriptor.getId();
	}

	@Override
	public StatisticVariable getStatisticVariable()
	{
		return variable;
	}

	@Override
	public TrackDescriptor getTrackDescriptor()
	{
		return descriptor;
	}

	@Override
	public void flush()
	{
		Entry entry = output();
		if( entry != null )
		{
			if( entry.getTimestamp() == lastFlushed )
				return;

			lastFlushed = entry.getTimestamp();
			aggregator.addEntry( descriptor.getId(), entry );
		}
	}

	protected static EntryBuilder at( long timestamp )
	{
		return new EntryBuilder( timestamp );
	}

	/**
	 * Builder for use in at( int timestamp ) to make writing data to the proper
	 * Track easy.
	 *
	 * @author dain.nilsson
	 */
	protected static class EntryBuilder
	{
		private final long timestamp;
		private final ImmutableMap.Builder<String, Number> mapBuilder = ImmutableMap.builder();

		public EntryBuilder( long timestamp )
		{
			this.timestamp = timestamp;
		}

		public <T extends Number> EntryBuilder put( String name, T value )
		{
			mapBuilder.put( name, value );
			return this;
		}

		public long getTimestamp()
		{
			return timestamp;
		}

		public Entry build()
		{
			return new EntryImpl( timestamp, mapBuilder.build() );
		}
	}

}
