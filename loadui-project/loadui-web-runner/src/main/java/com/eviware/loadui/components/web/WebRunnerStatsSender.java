package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.impl.statistics.CounterStatisticsWriter;
import com.eviware.loadui.impl.statistics.SampleStatisticsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


public class WebRunnerStatsSender
{
	private final Map<String, VariableGroup> resourceToVariableGroup = new HashMap<>();

	private static final Logger log = LoggerFactory.getLogger( WebRunnerStatsSender.class );

	private final Clock clock;
	private final ComponentContext context;

	public WebRunnerStatsSender( ComponentContext context, Clock clock)
	{
		this.clock = clock;
		this.context = context;
	}

	public void addResource( String resource )
	{
		resourceToVariableGroup.put( resource, new VariableGroup( resource ) );
	}

	public void clearStatisticVariables()
	{
		for( VariableGroup variables : resourceToVariableGroup.values() )
		{
			variables.removeAllVariablesFromContext();
		}

		resourceToVariableGroup.clear();
	}

	public void reset()
	{
		for( VariableGroup variables : resourceToVariableGroup.values() )
		{
			variables.resetCounters();
		}

	}

	public void updateRequestSent( String resource )
	{
		long timeStamp = clock.millis();

		VariableGroup resorceVariables = resourceToVariableGroup.get( resource );

		if(resorceVariables != null)
		{
			resorceVariables.sentVariable.update( timeStamp, resorceVariables.sentCount.incrementAndGet() );
		}
		else
		{
			logResourceNotFoundError( resource );
		}
	}

	public void updateLatency( String resource, long latency )
	{
		long timeStamp = clock.millis();

		VariableGroup resorceVariables = resourceToVariableGroup.get( resource );

		if(resorceVariables != null)
		{
			resorceVariables.latencyVariable.update( timeStamp, latency );
		}
		else
		{
			logResourceNotFoundError( resource );
		}
	}

	public void updateResponse( String resource, long timeTaken, long responseSize )
	{
		long TimeTaken = clock.millis();

		VariableGroup resorceVariables = resourceToVariableGroup.get( resource );
		if(resorceVariables != null)
		{
			resorceVariables.timeTakenVariable.update( TimeTaken, timeTaken );
			resorceVariables.responseSizeVariable.update( TimeTaken, responseSize );
		}
		else
		{
			logResourceNotFoundError(resource);
		}
	}

	public void updateRequestFailed( String resource )
	{
		long timeStamp = clock.millis();

		VariableGroup resorceVariables = resourceToVariableGroup.get( resource );

		if(resorceVariables != null)
		{
			resorceVariables.failureVariable.update( timeStamp, resorceVariables.failureCount.incrementAndGet() );
		}
		else
		{
			logResourceNotFoundError( resource );
		}

	}

	private void logResourceNotFoundError(String resource)
	{
		log.error( "Could not find statistic variables for resource {} please add it using addResource", resource );
	}

	public class VariableGroup
	{
		private final  StatisticVariable.Mutable timeTakenVariable;
		private final  StatisticVariable.Mutable latencyVariable;
		private final  StatisticVariable.Mutable responseSizeVariable;

		private final  StatisticVariable.Mutable failureVariable;
		private final  StatisticVariable.Mutable sentVariable;

		private final AtomicLong failureCount = new AtomicLong( 0l );
		private final AtomicLong sentCount = new AtomicLong( 0l );

		public VariableGroup(String resource)
		{
			timeTakenVariable = context.addStatisticVariable(
					resource + ": TimeTaken", "elapsed time for the resource to complete", SampleStatisticsWriter.TYPE );

			latencyVariable = context.addStatisticVariable(
					resource + ": Latency", "Time to first byte", SampleStatisticsWriter.TYPE );

			responseSizeVariable = context.addStatisticVariable(
					resource + ": ResponseSize", "Response Size of Response", SampleStatisticsWriter.TYPE );

			failureVariable = context.addStatisticVariable(
					resource + ": Failure", "Failed requests", CounterStatisticsWriter.TYPE );

			sentVariable = context.addStatisticVariable(
					resource + ": Sent", "Sent for the resource", CounterStatisticsWriter.TYPE );
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
			failureCount.set( 0l );
			sentCount.set( 0l );
		}
	}

}
