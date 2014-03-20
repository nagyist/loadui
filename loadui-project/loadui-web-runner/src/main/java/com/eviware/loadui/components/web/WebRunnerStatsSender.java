package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.impl.statistics.SampleStatisticsWriter;

/**
 * Author: maximilian.skog
 * Date: 2014-03-19
 * Time: 15:41
 */
public class WebRunnerStatsSender
{
	private final StatisticVariable.Mutable latencyVariable;
	private final StatisticVariable.Mutable sentVariable;
	private final StatisticVariable.Mutable timeTakenVariable;

	private final Clock clock;

	public WebRunnerStatsSender( ComponentContext context, Clock clock )
	{
		this.latencyVariable = context.addStatisticVariable( "Latency", "", SampleStatisticsWriter.TYPE );
		this.sentVariable = context.addStatisticVariable( "Sent", "", SampleStatisticsWriter.TYPE );
		this.timeTakenVariable = context.addStatisticVariable( "TimeTaken", "", SampleStatisticsWriter.TYPE );

		this.clock = clock;
	}

	public void addResource( String resource )
	{

	}


	public void updateRequestSent( String resource )
	{

	}

	public void updateLatency( String resource, long value )
	{
		long currentTime = clock.millis();
		latencyVariable.update( currentTime, value );
	}

	public void updateTimeTaken( String resource, long value )
	{
		long currentTime = clock.millis();
		latencyVariable.update( currentTime, value );
	}

}
