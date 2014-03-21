package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.components.web.api.RequestRunnerProvider;
import com.eviware.loadui.util.RealClock;
import com.google.common.collect.Iterables;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URI;

import static java.util.Arrays.asList;

public class DefaultRequestRunnerProvider implements RequestRunnerProvider
{
	private final Clock clock = new RealClock();
	private WebRunnerStatsSender statisticsSender;

	public RequestRunner provideRequestRunner( ComponentContext context, URI pageUri, Iterable<URI> assetUris )
	{
		return new RequestRunner( clock,
				HttpClientBuilder.create().build(),
				Iterables.concat( asList( pageUri ), assetUris ),
				createStatsSenderIfNecessary( context ) );
	}

	private WebRunnerStatsSender createStatsSenderIfNecessary( ComponentContext context )
	{
		if( statisticsSender == null )
		{
			statisticsSender = new WebRunnerStatsSender( context, clock );
		}
		else
		{
			statisticsSender.clearStatisticVariables();
		}
		return statisticsSender;
	}

}
