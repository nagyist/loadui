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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	public void shouldReportStatistic()
	{
		//given
		Entry entry1 = newEntry( 777l, VAR_STAT_NAME, 4 );
		int numberOfAgents = 1;

		//when
		aggregator.updateAndAggregate( entry1, "TrackID", numberOfAgents );

		//then
		verify( executionManagerMock ).writeEntry( trackIdCaptor.capture(), entryCaptor.capture(), sourceCaptor.capture() );

		assertThat( trackIdCaptor.getValue(), equalTo( "TrackID" ) );
		assertThat( sourceCaptor.getValue(), equalTo( StatisticVariable.MAIN_SOURCE ) );
		assertThat( trackIdCaptor.getValue(), equalTo( "TrackID" ) );

		Entry capturedEntry = entryCaptor.getValue();
		assertThat( capturedEntry.getValue( VAR_STAT_NAME ).intValue(), equalTo( 4 ) );
		assertThat( capturedEntry.getTimestamp(), equalTo( 777l ) );
	}

	@Test
	public void shouldNotReportStatistic()
	{
		//given
		Entry entry1 = newEntry( 777, VAR_STAT_NAME, 4 );
		int numberOfAgents = 2;

		//when
		aggregator.updateAndAggregate( entry1, "TrackID", numberOfAgents );

		//then
		verify( executionManagerMock, never() ).writeEntry( anyString(), ( Entry )anyObject(), anyString() );
	}

	@Test
	public void gettingUpdatesFrom2AgentsShouldAggregate()
	{
		//given
		int val1 = 4, val2 = 2;
		Entry entry1 = newEntry( 777, VAR_STAT_NAME, val1 );
		Entry entry2 = newEntry( 888, VAR_STAT_NAME, val2 );
		int numberOfAgents = 2;
		String trackId = "TrackID";

		//when
		aggregator.updateAndAggregate( entry1, trackId, numberOfAgents );
		aggregator.updateAndAggregate( entry2, trackId, numberOfAgents );

		//then
		verify( executionManagerMock ).writeEntry( trackIdCaptor.capture(), entryCaptor.capture(), sourceCaptor.capture() );

		assertThat( trackIdCaptor.getValue(), equalTo( "TrackID" ) );
		assertThat( sourceCaptor.getValue(), equalTo( StatisticVariable.MAIN_SOURCE ) );

		Entry capturedEntry = entryCaptor.getValue();
		assertThat( capturedEntry.getValue( VAR_STAT_NAME ).intValue(), equalTo( val1 + val2 ) );
		assertThat( capturedEntry.getTimestamp(), equalTo( 888l ) );
	}

	@Test
	public void multipleIntertwinedUpdatesWithDifferentTrackIdsTest()
	{
		//given
		Entry entry1 = newEntry( 777, VAR_STAT_NAME, 4 );
		Entry entry2 = newEntry( 666, VAR_STAT_NAME, 6 );
		Entry entry3 = newEntry( 1111, VAR_STAT_NAME, 2 );
		Entry entry4 = newEntry( 1222, VAR_STAT_NAME, 3 );
		Entry entry5 = newEntry( 2222, VAR_STAT_NAME, 1 );
		Entry entry6 = newEntry( 2333, VAR_STAT_NAME, 5 );
		int numberOfAgents = 2;
		String trackId1 = "TrackID1";
		String trackId2 = "TrackID2";

		//when
		aggregator.updateAndAggregate( entry1, trackId1, numberOfAgents );
		aggregator.updateAndAggregate( entry5, trackId2, numberOfAgents );
		aggregator.updateAndAggregate( entry6, trackId2, numberOfAgents );
		aggregator.updateAndAggregate( entry4, trackId1, numberOfAgents );
		aggregator.updateAndAggregate( entry3, trackId1, numberOfAgents );
		aggregator.updateAndAggregate( entry2, trackId1, numberOfAgents );

		//then
		verify( executionManagerMock, times( 3 ) ).writeEntry( trackIdCaptor.capture(), entryCaptor.capture(), sourceCaptor.capture() );

		assertThat( entryCaptor.getAllValues().size(), equalTo( 3 ) );

		List<String> trackIdCaptors = trackIdCaptor.getAllValues();
		List<Entry> capturedEntries = entryCaptor.getAllValues();
		assertThat( capturedEntries.get( 0 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 6 ) );
		assertThat( capturedEntries.get( 0 ).getTimestamp(), equalTo( 2333l ) );
		assertThat( trackIdCaptors.get( 0 ), equalTo( trackId2 ) );

		assertThat( capturedEntries.get( 1 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 5 ) );
		assertThat( capturedEntries.get( 1 ).getTimestamp(), equalTo( 1222l ) );
		assertThat( trackIdCaptors.get( 1 ), equalTo( trackId1 ) );

		assertThat( capturedEntries.get( 2 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 10 ) );
		assertThat( capturedEntries.get( 2 ).getTimestamp(), equalTo( 777l ) );
		assertThat( trackIdCaptors.get( 2 ), equalTo( trackId1 ) );
	}

	@Test
	public void updatesWithDifferentTrackIdsAndSimilarTimesStampTest()
	{
		//given
		int numberOfAgents = 2;
		Entry entry1 = newEntry( 777, VAR_STAT_NAME, 4 );
		Entry entry2 = newEntry( 666, VAR_STAT_NAME, 6 );
		String trackId1 = "TrackID1";
		String trackId2 = "TrackID2";

		//when
		aggregator.updateAndAggregate( entry1, trackId1, numberOfAgents );
		aggregator.updateAndAggregate( entry2, trackId2, numberOfAgents );

		//then
		verify( executionManagerMock, never() ).writeEntry( trackIdCaptor.capture(), entryCaptor.capture(), sourceCaptor.capture() );

	}

	@Test
	public void automaticFlushingTest()
	{
		//given
		int numberOfAgents = 2;
		String trackId1 = "TrackID1";

		//when
		aggregator.updateAndAggregate( newEntry( 1, VAR_STAT_NAME, 5 ), trackId1, numberOfAgents );
		aggregator.updateAndAggregate( newEntry( 1001, VAR_STAT_NAME, 5 ), trackId1, numberOfAgents );
		aggregator.updateAndAggregate( newEntry( 2002, VAR_STAT_NAME, 5 ), trackId1, numberOfAgents );
		aggregator.updateAndAggregate( newEntry( 3003, VAR_STAT_NAME, 5 ), trackId1, numberOfAgents );
		aggregator.updateAndAggregate( newEntry( 4004, VAR_STAT_NAME, 5 ), trackId1, numberOfAgents );

		aggregator.updateAndAggregate( newEntry( 2, VAR_STAT_NAME, 5 ), trackId1, numberOfAgents );

		aggregator.updateAndAggregate( newEntry( 5004, VAR_STAT_NAME, 5 ), trackId1, numberOfAgents );
		aggregator.updateAndAggregate( newEntry( 6004, VAR_STAT_NAME, 5 ), trackId1, numberOfAgents );
		aggregator.updateAndAggregate( newEntry( 1002, VAR_STAT_NAME, 5 ), trackId1, numberOfAgents );

		//then
		verify( executionManagerMock, times( 1 ) ).writeEntry( trackIdCaptor.capture(), entryCaptor.capture(), sourceCaptor.capture() );
		List<Entry> capturedEntries = entryCaptor.getAllValues();

		assertThat( capturedEntries.get( 0 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 10 ) );
		assertThat( capturedEntries.get( 0 ).getTimestamp(), equalTo( 2l ) );

//		assertThat( capturedEntries.get( 1 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 5 ) );
//		assertThat( capturedEntries.get( 1 ).getTimestamp(), equalTo( 1001l ) );
//
//		assertThat( capturedEntries.get( 2 ).getValue( VAR_STAT_NAME ).intValue(), equalTo( 5 ) );
//		assertThat( capturedEntries.get( 2 ).getTimestamp(), equalTo( 1002l ) );
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

