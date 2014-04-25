package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.components.web.api.RequestRunnerProvider;
import com.eviware.loadui.util.test.FakeClock;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import java.net.URI;

public class FakeRequestRunnerProvider implements RequestRunnerProvider
{
	private final CloseableHttpAsyncClient httpClient;

	private FakeRequestRunnerProvider( CloseableHttpAsyncClient httpClient )
	{
		this.httpClient = httpClient;
	}

	public static RequestRunnerProvider usingHttpClient( CloseableHttpAsyncClient httpClient )
	{
		return new FakeRequestRunnerProvider( httpClient );
	}

	public RequestRunner provideRequestRunner( ComponentContext context, URI pageUri, Iterable<URI> assetUris )
	{
		Clock clock = new FakeClock();
		return new RequestRunner( clock,
				httpClient,
				pageUri, assetUris,
				new WebRunnerStatsSender( context, clock ) );
	}

}
