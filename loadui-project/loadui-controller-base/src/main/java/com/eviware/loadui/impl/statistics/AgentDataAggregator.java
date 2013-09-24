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

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.statistics.EntryAggregator;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsAggregator;
import com.eviware.loadui.api.statistics.store.*;
import com.eviware.loadui.api.statistics.store.ExecutionManager.State;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AgentDataAggregator implements StatisticsAggregator
{
	public final static Logger log = LoggerFactory.getLogger( AgentDataAggregator.class );

	private final static int BUFFER_SIZE = 5;

	private final NavigableMap<Long, SetMultimap<String, Entry>> entriesIntoTrackIdBySecond = Maps.newTreeMap();
	private final TreeMultimap<Long, String> agentIdBySeconds = TreeMultimap.create();
	private final ExecutionManager executionManager;
	private final StatisticsInterpolator statisticsInterpolator;

	public AgentDataAggregator( ExecutionManager executionManager )
	{
		this.executionManager = executionManager;
		statisticsInterpolator = new StatisticsInterpolator( executionManager );
		executionManager.addExecutionListener( new FlushingExecutionListener() );
	}


	private SceneItem getScene( String trackId, AgentItem agent, ProjectItem project )
	{
		Collection<? extends SceneItem> assignedScenes = project.getScenesAssignedTo( agent );

		HashSet<StatisticHolder> assignedHolders = getStatisticHolders( assignedScenes );

		for( StatisticHolder holder : assignedHolders )
		{
			for( StatisticVariable variable : holder.getStatisticVariables() )
			{
				for( TrackDescriptor descriptor : variable.getTrackDescriptors() )
				{
					if( descriptor.getId().equalsIgnoreCase( trackId ) )
					{
						if( holder instanceof SceneItem )
						{
							return ( SceneItem )holder;
						}
						else
						{
							return ( SceneItem )holder.getCanvas();
						}

					}
				}
			}
		}

		throw new RuntimeException( "Could not find trackId during data aggregation" );
	}


	private HashSet<StatisticHolder> getStatisticHolders( Collection<? extends SceneItem> assignedScenes )
	{
		HashSet<StatisticHolder> assignedHolders = new HashSet<>();

		for( SceneItem scene : assignedScenes )
		{
			assignedHolders.add( scene );

			for( StatisticHolder component : scene.getComponents() )
			{
				assignedHolders.add( component );
			}
		}
		return assignedHolders;
	}

	public synchronized void update( Entry entry, String trackId, AgentItem agent )
	{
		// updates the unaggregated statistic
		statisticsInterpolator.update( entry, trackId, agent.getLabel() );

		Collection<String> assignedAgentsConnected = getAgentsSendingDataWithTrackId( trackId, agent );

		updateAndAggregate( entry, trackId, agent.getId(), assignedAgentsConnected );
	}

	private Collection<String> getAgentsSendingDataWithTrackId( String trackId, AgentItem agent )
	{
		ProjectItem project = agent.getWorkspace().getCurrentProject();
		SceneItem scene = getScene( trackId, agent, project );
		Collection<? extends AgentItem> assignedAgents = project.getAgentsAssignedTo( scene );
		return getIdsOfAgentsConnected( assignedAgents );
	}

	public void updateAndAggregate( final Entry entry, final String trackId, final String currentAgentId, final Collection<String> connectedAgentIds )
	{

		long time = entry.getTimestamp() / 1000;

		if( !entriesIntoTrackIdBySecond.isEmpty() && time < entriesIntoTrackIdBySecond.firstEntry().getKey() )
		{
			// old value
			log.warn( "received old message" );
			return;
		}

		agentIdBySeconds.put( time, currentAgentId );
		SetMultimap<String, Entry> currentEntryMap = entriesIntoTrackIdBySecond.get( time );

		if( currentEntryMap == null )
		{
			// no map for this time yet
			entriesIntoTrackIdBySecond.put( time, HashMultimap.<String, Entry>create() );
			currentEntryMap = entriesIntoTrackIdBySecond.get( time );
		}
		currentEntryMap.put( trackId, entry );

		Collection<Long> flushableTimes = getFlushableTimes( connectedAgentIds, agentIdBySeconds );
		if( !flushableTimes.isEmpty() )
		{
			flushTimes( flushableTimes );
		}

		flushOldTimes();
	}

	private void flushTimes( Collection<Long> flushableTimes )
	{
		// making copy to avoid concurrent modification
		Iterable<Long> copyOfTimes = new TreeSet<>( flushableTimes );
		for( Long flushableTime : copyOfTimes )
		{
			flushAndRemove( flushableTime.longValue() );
		}
	}

	private void flushOldTimes()
	{
		if( entriesIntoTrackIdBySecond.size() > BUFFER_SIZE )
		{
			flushAndRemove( entriesIntoTrackIdBySecond.firstEntry().getKey() );
		}
	}

	private void flushAndRemove( Long time )
	{
		SetMultimap<String, Entry> entriesByTrackId = entriesIntoTrackIdBySecond.get( time );

		for( String trackId : entriesByTrackId.keySet() )
		{
			flush( trackId, entriesByTrackId.get( trackId ) );
		}
		entriesIntoTrackIdBySecond.remove( time );
		agentIdBySeconds.removeAll( time );

	}

	private synchronized void flush( String trackId, Set<Entry> entries )
	{
		Track track = executionManager.getTrack( trackId );

		if( track == null )
		{
			// could be related to agents sending data from runs not started by this machine?
			log.warn( "Track does not exist, entries discarded" );
		}

		EntryAggregator aggregator = track.getEntryAggregator();

		if( aggregator != null )
		{
			Entry entry = aggregator.aggregate( entries, true );

			if( entry != null )
			{
				statisticsInterpolator.update( entry, trackId, StatisticVariable.MAIN_SOURCE );
			}

		}
		else
		{
			log.warn( "Could not get aggregator, entries discarded" );
		}

	}

	private void flushAll()
	{
		long flushTime = System.currentTimeMillis();

		for( Long time : Sets.newLinkedHashSet( entriesIntoTrackIdBySecond.keySet() ) )
		{
			flushAndRemove( time );
		}

		statisticsInterpolator.flush( flushTime );
	}

	@Override
	public void addEntry( String trackId, Entry entry )
	{
		statisticsInterpolator.update( entry, trackId, StatisticVariable.MAIN_SOURCE );
	}

	@Override
	public void addEntry( String trackId, Entry entry, String source )
	{
		statisticsInterpolator.update( entry, trackId, source );
	}

	public Collection<String> getIdsOfAgentsConnected( Collection<? extends AgentItem> agents )
	{
		HashSet<String> result = new HashSet<>();

		for( AgentItem agent : agents )
		{
			if( agent.isReady() )
			{
				result.add( agent.getId() );
			}
		}

		return result;
	}

	public Collection<Long> getFlushableTimes( final Collection<String> agents, final TreeMultimap<Long, String> reportedAgentsBySecond )
	{
		long lastFlushableTime = getLatestFlushableTime( agents, reportedAgentsBySecond );

		if( lastFlushableTime == -1 )
		{
			return new TreeSet<>();
		}

		NavigableSet<Long> timeSet = reportedAgentsBySecond.keySet();

		return timeSet.subSet( timeSet.first(), true, lastFlushableTime, false );

	}

	private long getLatestFlushableTime( Collection<String> agents, TreeMultimap<Long, String> reportedAgentsBySecond )
	{
		ArrayList<String> copy = new ArrayList<>( agents );
		for( Long time : reportedAgentsBySecond.keySet().descendingSet() )
		{

			for( String agentId : reportedAgentsBySecond.get( time ) )
			{
				copy.remove( agentId );
			}

			if( copy.isEmpty() )
			{
				return time;
			}

		}

		return -1;
	}

	private class FlushingExecutionListener implements ExecutionListener
	{
		@Override
		public void executionStarted( State oldState )
		{
			entriesIntoTrackIdBySecond.clear();
			agentIdBySeconds.clear();
			statisticsInterpolator.reset();
		}

		@Override
		public void executionPaused( State oldState )
		{
		}

		@Override
		public void executionStopped( State oldState )
		{
			log.debug( "Stop signal received" );
			flushAll();
		}

		@Override
		public void trackRegistered( TrackDescriptor trackDescriptor )
		{
		}

		@Override
		public void trackUnregistered( TrackDescriptor trackDescriptor )
		{
		}
	}
}
