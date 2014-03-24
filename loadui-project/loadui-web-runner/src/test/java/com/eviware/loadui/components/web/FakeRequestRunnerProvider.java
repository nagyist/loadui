package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.components.web.api.RequestRunnerProvider;
import com.eviware.loadui.util.test.FakeClock;
import com.google.common.collect.Iterables;
import org.apache.http.impl.client.CloseableHttpClient;

import java.net.URI;
import java.util.Arrays;

public class FakeRequestRunnerProvider implements RequestRunnerProvider
{
	private final CloseableHttpClient httpClient;

	private FakeRequestRunnerProvider( CloseableHttpClient httpClient )
	{
		this.httpClient = httpClient;
	}

	public static RequestRunnerProvider usingHttpClient( CloseableHttpClient httpClient )
	{
		return new FakeRequestRunnerProvider( httpClient );
	}

	public RequestRunner provideRequestRunner( ComponentContext context, URI pageUri, Iterable<URI> assetUris )
	{
		Clock clock = new FakeClock();
		return new RequestRunner( clock,
				httpClient,
				Iterables.concat( Arrays.asList( pageUri ), assetUris ),
				new WebRunnerStatsSender( context, clock ) );
	}

}
