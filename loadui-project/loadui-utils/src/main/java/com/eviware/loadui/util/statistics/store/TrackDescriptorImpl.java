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
package com.eviware.loadui.util.statistics.store;

import com.eviware.loadui.api.statistics.EntryAggregator;
import com.eviware.loadui.api.statistics.StatisticVariableIdentifier;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import net.jcip.annotations.Immutable;

import java.util.Map;

/**
 * Immutable implementation of the TrackDescriptor, used to store the structure
 * of a Track.
 *
 * @author dain.nilsson
 */

@Immutable
public class TrackDescriptorImpl implements TrackDescriptor
{
	private final StatisticVariableIdentifier identifier;
	private final Map<String, Class<? extends Number>> structure;
	private final EntryAggregator aggregator;

	public TrackDescriptorImpl( StatisticVariableIdentifier identifier,
										 Map<String, Class<? extends Number>> structure,
										 EntryAggregator aggregator )
	{
		this.identifier = identifier;
		this.structure = ImmutableMap.copyOf( structure );
		this.aggregator = aggregator;
	}

	@Override
	public String getId()
	{
		return identifier.getHash();
	}

	@Override
	public Map<String, Class<? extends Number>> getValueNames()
	{
		return structure;
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper( this ).add( "structure", structure ).toString();
	}

	@Override
	public EntryAggregator getEntryAggregator()
	{
		return aggregator;
	}

	@Override
	public StatisticVariableIdentifier getStatisticIdentifier()
	{
		return identifier;
	}

}
