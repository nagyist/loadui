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
import com.eviware.loadui.api.statistics.*;
import com.eviware.loadui.api.statistics.store.*;
import com.eviware.loadui.api.statistics.store.ExecutionManager.State;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AgentDataAggregator implements StatisticsAggregator
{
	public final static Logger log = LoggerFactory.getLogger( AgentDataAggregator.class );

	private final static int BUFFER_SIZE = 5;
	public static final Comparator<Object> NO_ORDER_COMPARATOR = new Comparator<Object>()
	{
		@Override
		public int compare( Object o1, Object o2 )
		{
			return -1;
		}
	};

	private final HashMap<String, TreeMultimap<Long, Entry>> entriesIntoSecondsByTrackId = new HashMap<>();
	private final ExecutionManager executionManager;
	private final StatisticsInterpolator statisticsInterpolator;

	public AgentDataAggregator( ExecutionManager executionManager )
	{
		this.executionManager = executionManager;
		statisticsInterpolator = new StatisticsInterpolator( executionManager );

		executionManager.addExecutionListener( new FlushingExecutionListener() );
	}


	private SceneItem getSceneWithTrackId( String trackId )
	{
		StatisticsManager statisticsManager = BeanInjector.getBean( StatisticsManager.class );

		for( StatisticHolder holder : statisticsManager.getStatisticHolders() )
		{
			for( StatisticVariable variable : holder.getStatisticVariables() )
			{
				for( TrackDescriptor descriptor : variable.getTrackDescriptors() )
				{
					if( descriptor.getId().equalsIgnoreCase( trackId ) )
					{
						return ( SceneItem )holder.getCanvas();
					}
				}
			}
		}

		return null;
	}

	private SceneItem getScene( String trackId, AgentItem agent, ProjectItem project )
	{

		Collection<? extends SceneItem> assignedScenes = project.getScenesAssignedTo( agent );


		HashSet<StatisticHolder> assignedHolders = new HashSet<>();

		for( SceneItem scene : assignedScenes )
		{
			assignedHolders.add( scene );

			for( StatisticHolder component : scene.getComponents() )
			{
				assignedHolders.add( component );
			}
		}

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

	public synchronized void update( Entry entry, String trackId, AgentItem agent )
	{
		// updates the unaggregated statistic
		statisticsInterpolator.update( entry, trackId, agent.getLabel() );

		int assignedAgentsConnected = getNumberOfAgentsConnected( trackId, agent );

		updateAndAggregate( entry, trackId, assignedAgentsConnected );
	}

	private int getNumberOfAgentsConnected( String trackId, AgentItem agent )
	{
		ProjectItem project = agent.getWorkspace().getCurrentProject();
		SceneItem scene = getScene( trackId, agent, project );
		Collection<? extends AgentItem> assignedAgents = project.getAgentsAssignedTo( scene );
		return getNumberOfAgentsConnected( assignedAgents );
	}

	public void updateAndAggregate( Entry entry, String trackId, final int assignedAgentsConnected )
	{
		long time = entry.getTimestamp() / 1000;

		TreeMultimap<Long, Entry> map = entriesIntoSecondsByTrackId.get( trackId );
		if( map == null )
		{
			map = TreeMultimap.create( Ordering.natural(), NO_ORDER_COMPARATOR );
			entriesIntoSecondsByTrackId.put( trackId, map );
		}

		map.put( time, entry );

		if( map.get( time ).size() == assignedAgentsConnected )
		{
			flushAndRemove( trackId, time );
		}

		removeOldMessages();
	}

	private void removeOldMessages()
	{
		for( String trackId : entriesIntoSecondsByTrackId.keySet() )
		{
			NavigableSet<Long> timeSet = entriesIntoSecondsByTrackId.get( trackId ).keySet();

			if( timeSet.size() > BUFFER_SIZE )
			{
				Long oldestTime = timeSet.first();
				log.debug( "Removing old incomplete messages: " + oldestTime + " : " + trackId );
				//flushAndRemove( trackId, oldestTime );
				entriesIntoSecondsByTrackId.get( trackId ).removeAll( oldestTime );
			}
		}

	}

	private void flushAndRemove( String trackId, Long time )
	{
		flush( trackId, entriesIntoSecondsByTrackId.get( trackId ).get( time ) );
		entriesIntoSecondsByTrackId.get( trackId ).removeAll( time );
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
			else
			{
				//this was ignored before.
				//log.warn("Could not get aggregated entry, entries {} discarded", entries);
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

		for( String trackId : entriesIntoSecondsByTrackId.keySet() )
		{
			NavigableSet<Long> timeSet = entriesIntoSecondsByTrackId.get( trackId ).keySet();
			for( Long time : timeSet )
			{
				flushAndRemove( trackId, time );
			}
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

	public int getNumberOfAgentsConnected( Collection<? extends AgentItem> agents )
	{
		int result = 0;

		for( AgentItem agent : agents )
		{
			if( agent.isReady() )
			{
				result++;
			}
		}

		return result;
	}

	private class FlushingExecutionListener implements ExecutionListener
	{
		@Override
		public void executionStarted( State oldState )
		{
			entriesIntoSecondsByTrackId.clear();
			statisticsInterpolator.reset();
		}

		@Override
		public void executionPaused( State oldState )
		{
		}

		@Override
		public void executionStopped( State oldState )
		{
			//flushAll();
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
