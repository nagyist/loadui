package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.statistics.StatisticVariable;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: maximilian.skog
 * Date: 2014-03-19
 * Time: 15:41
 */
public class WebRunnerStatsSender
{
	private final Map<String, StatisticVariable.Mutable> SentVariableMap = new HashMap<>();
	private final Map<String, StatisticVariable.Mutable> latencyVariableMap = new HashMap<>();
	private final Map<String, StatisticVariable.Mutable> timeTakenVariableMap = new HashMap<>();
	private final Map<String, StatisticVariable.Mutable> responseSizeVariableMap = new HashMap<>();

	private final Clock clock;
	private final ComponentContext context;

	public WebRunnerStatsSender( ComponentContext context, Clock clock)
	{

		this.clock = clock;
		this.context = context;
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
	}

	public void updateResponse( String resource, long timeTaken, long responseSize )
	{
		long currentTime = clock.millis();
	}

}
