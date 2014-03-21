package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.components.web.api.RequestRunnerProvider;
import com.eviware.loadui.util.RealClock;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URI;

public class DefaultRequestRunnerProvider implements RequestRunnerProvider
{
	public RequestRunner provideRequestRunner( ComponentContext context, Iterable<URI> pageUris )
	{
		Clock clock = new RealClock();
		return new RequestRunner( clock,
				HttpClientBuilder.create().build(),
				pageUris,
				new WebRunnerStatsSender( context, clock ) );
	}
}
