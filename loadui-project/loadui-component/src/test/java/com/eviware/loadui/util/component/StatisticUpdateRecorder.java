package com.eviware.loadui.util.component;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.store.TrackDescriptor;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkState;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class StatisticUpdateRecorder
{
	private Answer<StatisticVariable.Mutable> newStatisticVariableAnswer = new Answer<StatisticVariable.Mutable>()
	{
		@Override
		public StatisticVariable.Mutable answer( InvocationOnMock invocation ) throws Throwable
		{
			Object[] args = invocation.getArguments();
			return variables.get( ( String ) args[0] );
		}
	};
	private final StatisticHolder componentSpy;
	private final LoadingCache<String, RecordingStatisticVariable> variables = CacheBuilder.newBuilder()
			.build( new CacheLoader<String, RecordingStatisticVariable>()
			{
				@Override
				public RecordingStatisticVariable load( String key ) throws Exception
				{
					return new RecordingStatisticVariable();
				}
			} );

	public static StatisticUpdateRecorder newInstance( StatisticHolder componentSpy, ComponentContext contextSpy )
	{
		checkState( Mockito.mockingDetails( contextSpy ).isMock() );
		return new StatisticUpdateRecorder( componentSpy, contextSpy );
	}

	private StatisticUpdateRecorder( StatisticHolder componentSpy, ComponentContext contextSpy )
	{
		doAnswer( newStatisticVariableAnswer ).when( contextSpy ).addStatisticVariable( anyString(), anyString(),
				Matchers.<String>anyVararg() );
		doAnswer( newStatisticVariableAnswer ).when( contextSpy ).addListenableStatisticVariable( anyString(), anyString(),
				Matchers.<String>anyVararg() );
		doNothing().when( contextSpy ).removeStatisticVariable( anyString() );
		this.componentSpy = componentSpy;
	}

	public List<Number> getUpdatesToVariable( String variableName )
	{
		ConcurrentMap<String, RecordingStatisticVariable> variablesMap = variables.asMap();
		assertTrue( variablesMap.containsKey( variableName ) );
		return variablesMap.get( variableName ).updates;
	}

	private class RecordingStatisticVariable implements StatisticVariable.Mutable
	{
		final List<Number> updates = Collections.synchronizedList( new LinkedList<Number>() );

		@Override
		public StatisticHolder getStatisticHolder()
		{
			return componentSpy;
		}

		@Override
		public Set<String> getSources()
		{
			return ImmutableSet.of();
		}

		@Override
		public Set<String> getStatisticNames()
		{
			return ImmutableSet.of();
		}

		@Override
		public Statistic<?> getStatistic( String statisticName, String source )
		{
			Statistic statistic = mock( Statistic.class );
			when( statistic.getStatisticVariable() ).thenReturn( this );
			return statistic;
		}

		@Override
		public String getDescriptionForStatistic( @Nonnull String statisticName )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterable<TrackDescriptor> getTrackDescriptors()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String getLabel()
		{
			return "Label set by " + StatisticUpdateRecorder.class;
		}

		@Override
		public void setDescription( @Nonnull String description )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String getDescription()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void update( long timestamp, Number value )
		{
			updates.add( value );
		}
	}
}
