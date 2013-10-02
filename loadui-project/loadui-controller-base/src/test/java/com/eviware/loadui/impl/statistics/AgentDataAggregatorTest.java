package com.eviware.loadui.impl.statistics;

import com.eviware.loadui.api.statistics.EntryAggregator;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.Track;
import com.eviware.loadui.util.statistics.store.EntryImpl;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Author: maximilian.skog
 * Date: 2013-08-26
 * Time: 14:15
 */
public class AgentDataAggregatorTest
{

	private AgentDataAggregator aggregator;
	private ExecutionManager executionManagerMock;
	private final String VAR_STAT_NAME = "a very special statistic";

	private ArgumentCaptor<String> trackIdCaptor = ArgumentCaptor.forClass( String.class );
	private ArgumentCaptor<String> sourceCaptor = ArgumentCaptor.forClass( String.class );
	private ArgumentCaptor<Entry> entryCaptor = ArgumentCaptor.forClass( Entry.class );


	@Before
	public void setup()
	{
		EntryAggregator entryAggregator = new EntryAggregator()
		{
			@Override
			public Entry aggregate( Set<Entry> entries, boolean parallel )
			{
				if( entries.size() <= 1 )
					return Iterables.getFirst( entries, null );

				long maxTime = -1;
				double value = 0;
				for( Entry entry : entries )
				{
					maxTime = Math.max( maxTime, entry.getTimestamp() );
					value += entry.getValue( VAR_STAT_NAME ).doubleValue();
				}

				if( !parallel )
					value /= entries.size();

				return new AbstractStatisticsWriter.EntryBuilder( maxTime ).put( VAR_STAT_NAME, value ).build();
			}
		};

		Track trackMock = Mockito.mock( Track.class );
		when( trackMock.getEntryAggregator() ).thenReturn( entryAggregator );


		executionManagerMock = Mockito.mock( ExecutionManager.class );
		when( executionManagerMock.getState() ).thenReturn( ExecutionManager.State.STARTED );
		when( executionManagerMock.getTrack( anyString() ) ).thenReturn( trackMock );

		aggregator = new AgentDataAggregator( executionManagerMock );
	}

	@Test
	public void aggregationOfSingleStatisticAndSingleAgent()
	{
		//given
		String agentId = "Single Agent";
		List<String> agentsConnected = Arrays.asList( agentId );
		Entry entry1 = newEntry( 777l, VAR_STAT_NAME, 4 );
		Entry entry2 = newEntry( 1222l, VAR_STAT_NAME, 3 );

		//when
		aggregator.updateAndAggregate( entry1, "TrackID", agentId, agentsConnected );
		aggregator.updateAndAggregate( entry2, "TrackID", agentId, agentsConnected );

		//then
		verify( executionManagerMock, times( 1 ) ).writeEntry( trackIdCaptor.capture(), entryCaptor.capture(), sourceCaptor.capture() );

		assertThat( trackIdCaptor.getValue(), equalTo( "TrackID" ) );
		assertThat( sourceCaptor.getValue(), equalTo( StatisticVariable.MAIN_SOURCE ) );
		assertThat( trackIdCaptor.getValue(), equalTo( "TrackID" ) );

		Entry capturedEntry = entryCaptor.getValue();
		assertThat( capturedEntry.getValue( VAR_STAT_NAME ).intValue(), equalTo( 4 ) );
		assertThat( capturedEntry.getTimestamp(), equalTo( 777l ) );
	}

	@Test
	public void shouldNotReportStatisticWhenAllAgentsIsNotDoneMessaging()
	{
		//given
		String singleTrackId = "trackid1";

		String agentId1 = "coolAgent2008";
		String agentId2 = "max is the best programmer";
		List<String> agentsConnected = Arrays.asList( agentId1, agentId2 );

		Entry entry1 = newEntry( 777, VAR_STAT_NAME, 1 );
		Entry entry2 = newEntry( 555, VAR_STAT_NAME, 2 );
		Entry entry3 = newEntry( 1777, VAR_STAT_NAME, 3 );


		//when
		aggregator.updateAndAggregate( entry1, singleTrackId, agentId1, agentsConnected );
		aggregator.updateAndAggregate( entry2, singleTrackId, agentId2, agentsConnected );
		aggregator.updateAndAggregate( entry3, singleTrackId, agentId2, agentsConnected );

		//then
		verify( executionManagerMock, never() ).writeEntry( anyString(), ( Entry )anyObject(), anyString() );
	}

	@Test
	public void gettingUpdatesFrom2AgentsShouldAggregate()
	{
		//given
		String singleTrackId = "trackid1";

		String agentId1 = "coolAgent2008";
		String agentId2 = "max is the best programmer";
		List<String> agentsConnected = Arrays.asList( agentId1, agentId2 );

		int val1 = 3;
		int val2 = 4;

		Entry entry1 = newEntry( 777, VAR_STAT_NAME, val1 );
		Entry entry2 = newEntry( 555, VAR_STAT_NAME, val2 );
		Entry entry3 = newEntry( 1777, VAR_STAT_NAME, 3 );
		Entry entry4 = newEntry( 1555, VAR_STAT_NAME, 4 );

		//when
		aggregator.updateAndAggregate( entry1, singleTrackId, agentId1, agentsConnected );
		aggregator.updateAndAggregate( entry2, singleTrackId, agentId2, agentsConnected );
		aggregator.updateAndAggregate( entry3, singleTrackId, agentId1, agentsConnected );
		aggregator.updateAndAggregate( entry4, singleTrackId, agentId2, agentsConnected );

		//then
		verify( executionManagerMock, times( 1 ) ).writeEntry( trackIdCaptor.capture(), entryCaptor.capture(), sourceCaptor.capture() );

		assertThat( trackIdCaptor.getValue(), equalTo( "trackid1" ) );
		assertThat( sourceCaptor.getValue(), equalTo( StatisticVariable.MAIN_SOURCE ) );

		Entry capturedEntry = entryCaptor.getValue();
		assertThat( capturedEntry.getValue( VAR_STAT_NAME ).intValue(), equalTo( val1 + val2 ) );
		assertThat( capturedEntry.getTimestamp(), equalTo( 777l ) );
	}

	@Test
	public void multipleUpdatesWithDifferentTrackIdsTest()
	{
		//given
		String trackId1 = "TrackID1";
		String trackId2 = "TrackID2";

		String agentId1 = "coolAgent2008";
		String agentId2 = "agent2";
		List<String> agentsConnected = Arrays.asList( agentId1, agentId2 );

		Entry entry1 = newEntry( 777, VAR_STAT_NAME, 4 );
		Entry entry2 = newEntry( 666, VAR_STAT_NAME, 6 );
		Entry entry3 = newEntry( 1111, VAR_STAT_NAME, 2 );
		Entry entry4 = newEntry( 1222, VAR_STAT_NAME, 3 );
		Entry entry5 = newEntry( 2222, VAR_STAT_NAME, 1 );
		Entry entry6 = newEntry( 2333, VAR_STAT_NAME, 5 );
		Entry entry7 = newEntry( 3333, VAR_STAT_NAME, 1 );
		Entry entry8 = newEntry( 5554, VAR_STAT_NAME, 5 );


		//when
		aggregator.updateAndAggregate( entry1, trackId1, agentId1, agentsConnected );
		aggregator.updateAndAggregate( entry2, trackId1, agentId2, agentsConnected );
		aggregator.updateAndAggregate( entry3, trackId2, agentId2, agentsConnected );
		aggregator.updateAndAggregate( entry4, trackId2, agentId1, agentsConnected );
		aggregator.updateAndAggregate( entry5, trackId1, agentId2, agentsConnected );
		aggregator.updateAndAggregate( entry6, trackId1, agentId1, agentsConnected );
		aggregator.updateAndAggregate( entry7, trackId1, agentId1, agentsConnected );
		aggregator.updateAndAggregate( entry8, trackId1, agentId2, agentsConnected );

		//then
		verify( executionManagerMock, times( 3 ) ).writeEntry( trackIdCaptor.capture(), entryCaptor.capture(), sourceCaptor.capture() );

		List<String> trackIdCaptors = trackIdCaptor.getAllValues();
		List<Entry> capturedEntries = entryCaptor.getAllValues();

		assertThat( capturedEntries.get( 0 ).getTimestamp(), equalTo( 777l ) );
		assertThat( trackIdCaptors.get( 0 ), equalTo( trackId1 ) );

		assertThat( capturedEntries.get( 1 ).getTimestamp(), equalTo( 1222l ) );
		assertThat( trackIdCaptors.get( 1 ), equalTo( trackId2 ) );

		assertThat( capturedEntries.get( 2 ).getTimestamp(), equalTo( 2333l ) );
		assertThat( trackIdCaptors.get( 2 ), equalTo( trackId1 ) );

	}

	@Test
	public void whenOneAgentIsDelayedMultipleTimesShouldFlush()
	{
		//given
		String trackId1 = "TrackID1";

		String agentId1 = "coolAgent2008";
		String agentId2 = "agent2";
		List<String> agentsConnected = Arrays.asList( agentId1, agentId2 );

		Entry entry1 = newEntry( 777, VAR_STAT_NAME, 4 );
		Entry entry2 = newEntry( 1111, VAR_STAT_NAME, 6 );
		Entry entry3 = newEntry( 2222, VAR_STAT_NAME, 2 );
		Entry entry4 = newEntry( 3333, VAR_STAT_NAME, 3 );

		//when
		aggregator.updateAndAggregate( entry1, trackId1, agentId1, agentsConnected );
		aggregator.updateAndAggregate( entry2, trackId1, agentId1, agentsConnected );
		aggregator.updateAndAggregate( entry3, trackId1, agentId1, agentsConnected );
		aggregator.updateAndAggregate( entry4, trackId1, agentId2, agentsConnected );


		//then
		verify( executionManagerMock, times( 2 ) ).writeEntry( trackIdCaptor.capture(), entryCaptor.capture(), sourceCaptor.capture() );

		List<Entry> capturedEntries = entryCaptor.getAllValues();

		assertThat( capturedEntries.get( 0 ).getTimestamp(), equalTo( 777l ) );
		assertThat( capturedEntries.get( 0 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 4 ) );

		assertThat( capturedEntries.get( 1 ).getTimestamp(), equalTo( 1111l ) );
		assertThat( capturedEntries.get( 1 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 6 ) );

	}

	@Test
	public void oneAgentDisconnectsShouldMakeItAggregateOldMessages()
	{
		//given
		String trackId1 = "TrackID1";

		String agentId1 = "coolAgent2008";
		String agentId2 = "agent2";
		List<String> twoAgents = Arrays.asList( agentId1, agentId2 );
		List<String> oneAgent = Arrays.asList( agentId1 );

		Entry entry1 = newEntry( 777, VAR_STAT_NAME, 4 );
		Entry entry2 = newEntry( 555, VAR_STAT_NAME, 6 );
		Entry entry3 = newEntry( 2222, VAR_STAT_NAME, 2 );
		Entry entry4 = newEntry( 3333, VAR_STAT_NAME, 3 );
		Entry entry5 = newEntry( 3444, VAR_STAT_NAME, 5 );
		Entry entry6 = newEntry( 4444, VAR_STAT_NAME, 7 );

		//when
		aggregator.updateAndAggregate( entry1, trackId1, agentId1, twoAgents );
		aggregator.updateAndAggregate( entry2, trackId1, agentId2, twoAgents );
		aggregator.updateAndAggregate( entry3, trackId1, agentId1, twoAgents );
		aggregator.updateAndAggregate( entry4, trackId1, agentId1, twoAgents );
		aggregator.updateAndAggregate( entry5, trackId1, agentId1, twoAgents );
		aggregator.updateAndAggregate( entry6, trackId1, agentId1, oneAgent );


		//then
		verify( executionManagerMock, times( 3 ) ).writeEntry( trackIdCaptor.capture(), entryCaptor.capture(), sourceCaptor.capture() );

		List<Entry> capturedEntries = entryCaptor.getAllValues();

		assertThat( capturedEntries.get( 0 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 10 ) );
		assertThat( capturedEntries.get( 0 ).getTimestamp(), equalTo( 777l ) );

		assertThat( capturedEntries.get( 1 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 2 ) );
		assertThat( capturedEntries.get( 1 ).getTimestamp(), equalTo( 2222l ) );

		assertThat( capturedEntries.get( 2 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 8 ) );
		assertThat( capturedEntries.get( 2 ).getTimestamp(), equalTo( 3444l ) );
	}

	@Test
	public void shouldThrowAwayOldValue()
	{
		//given
		String trackId1 = "TrackID1";

		String agentId1 = "coolAgent2008";
		String agentId2 = "agent2";
		List<String> twoAgents = Arrays.asList( agentId1, agentId2 );

		Entry entry1 = newEntry( 1777, VAR_STAT_NAME, 4 );
		Entry entry2 = newEntry( 1555, VAR_STAT_NAME, 6 );
		Entry entry3 = newEntry( 3222, VAR_STAT_NAME, 2 );
		Entry entry4 = newEntry( 3333, VAR_STAT_NAME, 3 );
		Entry entry5 = newEntry( 777, VAR_STAT_NAME, 7 );
		Entry entry6 = newEntry( 888, VAR_STAT_NAME, 7 );
		Entry entry7 = newEntry( 4444, VAR_STAT_NAME, 7 );
		Entry entry8 = newEntry( 4555, VAR_STAT_NAME, 7 );

		//when
		aggregator.updateAndAggregate( entry1, trackId1, agentId1, twoAgents );
		aggregator.updateAndAggregate( entry2, trackId1, agentId2, twoAgents );
		aggregator.updateAndAggregate( entry3, trackId1, agentId1, twoAgents );
		aggregator.updateAndAggregate( entry4, trackId1, agentId2, twoAgents );

		aggregator.updateAndAggregate( entry5, trackId1, agentId1, twoAgents );
		aggregator.updateAndAggregate( entry6, trackId1, agentId2, twoAgents );

		aggregator.updateAndAggregate( entry7, trackId1, agentId1, twoAgents );
		aggregator.updateAndAggregate( entry8, trackId1, agentId2, twoAgents );


		//then
		verify( executionManagerMock, times( 2 ) ).writeEntry( trackIdCaptor.capture(), entryCaptor.capture(), sourceCaptor.capture() );

		List<Entry> capturedEntries = entryCaptor.getAllValues();

		assertThat( capturedEntries.get( 0 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 10 ) );
		assertThat( capturedEntries.get( 0 ).getTimestamp(), equalTo( 1777l ) );

		assertThat( capturedEntries.get( 1 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 5 ) );
		assertThat( capturedEntries.get( 1 ).getTimestamp(), equalTo( 3333l ) );


	}

	@Test
	public void shouldFlushAfter5reportedSeconds()
	{
		//given
		String trackId1 = "TrackID1";

		String agentId1 = "coolAgent2008";
		String agentId2 = "not used agent";
		List<String> twoAgents = Arrays.asList( agentId1, agentId2 );

		Entry entry1 = newEntry( 444, VAR_STAT_NAME, 4 );
		Entry entry2 = newEntry( 1555, VAR_STAT_NAME, 6 );
		Entry entry3 = newEntry( 2222, VAR_STAT_NAME, 2 );
		Entry entry4 = newEntry( 3333, VAR_STAT_NAME, 3 );
		Entry entry5 = newEntry( 4444, VAR_STAT_NAME, 7 );
		Entry entry6 = newEntry( 5555, VAR_STAT_NAME, 7 );

		//when
		aggregator.updateAndAggregate( entry1, trackId1, agentId1, twoAgents );
		aggregator.updateAndAggregate( entry2, trackId1, agentId1, twoAgents );
		aggregator.updateAndAggregate( entry3, trackId1, agentId1, twoAgents );
		aggregator.updateAndAggregate( entry4, trackId1, agentId1, twoAgents );
		aggregator.updateAndAggregate( entry5, trackId1, agentId1, twoAgents );

		aggregator.updateAndAggregate( entry6, trackId1, agentId1, twoAgents );


		//then
		verify( executionManagerMock, times( 1 ) ).writeEntry( trackIdCaptor.capture(), entryCaptor.capture(), sourceCaptor.capture() );

		List<Entry> capturedEntries = entryCaptor.getAllValues();

		assertThat( capturedEntries.get( 0 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 4 ) );
		assertThat( capturedEntries.get( 0 ).getTimestamp(), equalTo( 444l ) );

	}

	private EntryImpl newEntry( long timeStamp, String description, long value )
	{
		return new EntryImpl( timeStamp, createValueMap( description, value ) );
	}

	private Map<String, Number> createValueMap( String key, long value )
	{
		HashMap<String, Number> map = new HashMap<>();
		map.put( key, value );
		return map;
	}

}

