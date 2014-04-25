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
package com.eviware.loadui.api.statistics.store;

import com.eviware.loadui.api.statistics.EntryAggregator;
import com.eviware.loadui.api.statistics.StatisticVariableIdentifier;

import java.util.Map;

/**
 * Defines the structure of a Track, and once registered, allows the creation of
 * the Track itself.
 * 
 * @author dain.nilsson
 * 
 */
public interface TrackDescriptor
{
	/**
	 * Gets the id of the given TrackDescriptor.
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * Gets a Map of the names of the Statistics that this Track provides, paired
	 * with the Number subclass of the Statistic.
	 * 
	 * @return
	 */
	public Map<String, Class<? extends Number>> getValueNames();

	/**
	 * Gets the EntryAggregator to be used for aggregating Entries for this
	 * TrackDescriptor.
	 * 
	 * @return
	 */
	public EntryAggregator getEntryAggregator();

	/**
	 * @return statistic identifier which can be used to retrieve details about a track's statistic.
	 */
	StatisticVariableIdentifier getStatisticIdentifier();

}
