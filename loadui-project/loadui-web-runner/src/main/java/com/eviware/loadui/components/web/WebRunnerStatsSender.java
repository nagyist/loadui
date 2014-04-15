package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.impl.statistics.CounterStatisticsWriter;
import com.eviware.loadui.impl.statistics.SampleStatisticsWriter;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.eviware.loadui.webdata.StatisticConstants.*;

public class WebRunnerStatsSender
{
	private static final Logger log = LoggerFactory.getLogger( WebRunnerStatsSender.class );

	private volatile ImmutableMap<String, VariableGroup> resourceToVariableGroup = ImmutableMap.of();

	private final Clock clock;
	private final ComponentContext context;

	public WebRunnerStatsSender( ComponentContext context, Clock clock )
	{
		this.clock = clock;
		this.context = context;
	}

	public void addResources( Iterable<URI> uris )
	{
		ImmutableMap.Builder<String, VariableGroup> mapBuilder = ImmutableMap.builder();
		mapBuilder.putAll( resourceToVariableGroup );
		for( URI uri : uris )
		{
			String resource = uri.toASCIIString();
			mapBuilder.put( resource, new VariableGroup( resource ) );
		}
		resourceToVariableGroup = mapBuilder.build();
	}

	public void reset()
	{
		for( VariableGroup variables : resourceToVariableGroup.values() )
		{
			variables.resetCounters();
		}
	}

	private VariableGroup getVariablesFor( String resource )
	{
		return resourceToVariableGroup.get( resource );
	}

	public void updateRequestSent( String resource )
	{
		long timeStamp = clock.millis();

		VariableGroup resourceVariables = getVariablesFor( resource );

		if( resourceVariables != null )
		{
			resourceVariables.sentVariable.update( timeStamp, resourceVariables.sentCount.incrementAndGet() );
		}
		else
		{
			logResourceNotFoundError( resource );
		}
	}

	public void updateLatency( String resource, long latency )
	{
		long timeStamp = clock.millis();

		VariableGroup resourceVariables = getVariablesFor( resource );

		if( resourceVariables != null )
		{
			resourceVariables.latencyVariable.update( timeStamp, latency );
		}
		else
		{
			logResourceNotFoundError( resource );
		}
	}

	public void updateResponse( String resource, long timeTaken, long responseSize )
	{
		long timeStamp = clock.millis();

		VariableGroup resourceVariables = getVariablesFor( resource );
		if( resourceVariables != null )
		{
			resourceVariables.timeTakenVariable.update( timeStamp, timeTaken );
			resourceVariables.responseSizeVariable.update( timeStamp, responseSize );
		}
		else
		{
			logResourceNotFoundError( resource );
		}
	}

	public void updateRequestFailed( String resource )
	{
		long timeStamp = clock.millis();

		VariableGroup resorceVariables = getVariablesFor( resource );

		if( resorceVariables != null )
		{
			resorceVariables.failureVariable.update( timeStamp, resorceVariables.failureCount.incrementAndGet() );
		}
		else
		{
			logResourceNotFoundError( resource );
		}
	}

	private void logResourceNotFoundError( String resource )
	{
		log.error( "Could not find statistic variables for resource {} please add it using addResource", resource );
	}

	public class VariableGroup
	{
		private final StatisticVariable.Mutable timeTakenVariable;
		private final StatisticVariable.Mutable latencyVariable;
		private final StatisticVariable.Mutable responseSizeVariable;

		private final StatisticVariable.Mutable failureVariable;
		private final StatisticVariable.Mutable sentVariable;

		private final AtomicLong failureCount = new AtomicLong( 0L );
		private final AtomicLong sentCount = new AtomicLong( 0L );

		public VariableGroup( String identifier )
		{
			timeTakenVariable = context.addStatisticVariable(
					identifier + ":" + TIMETAKEN_IDENTIFIER, "elapsed time for the resource to complete", SampleStatisticsWriter.TYPE );

			latencyVariable = context.addStatisticVariable(
					identifier + ":" + LATENCY_IDENTIFIER, "Time to first byte", SampleStatisticsWriter.TYPE );

			responseSizeVariable = context.addStatisticVariable(
					identifier + ":" + RESPONSESIZE_IDENTIFIER, "Response Size of Response", SampleStatisticsWriter.TYPE );

			failureVariable = context.addStatisticVariable(
					identifier + ":" + FAILURE_IDENTIFIER, "Failed requests", CounterStatisticsWriter.TYPE );

			sentVariable = context.addStatisticVariable(
					identifier + ":" + SENT_IDENTIFIER, "Sent for the resource", CounterStatisticsWriter.TYPE );
		}

		public void removeAllVariablesFromContext()
		{
			context.removeStatisticVariable( timeTakenVariable.getLabel() );
			context.removeStatisticVariable( sentVariable.getLabel() );
			context.removeStatisticVariable( latencyVariable.getLabel() );
			context.removeStatisticVariable( responseSizeVariable.getLabel() );
			context.removeStatisticVariable( failureVariable.getLabel() );
		}

		public void resetCounters()
		{
			failureCount.set( 0L );
			sentCount.set( 0L );
		}
	}

}
