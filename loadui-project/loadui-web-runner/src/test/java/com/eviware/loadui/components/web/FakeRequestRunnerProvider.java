package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.components.web.api.RequestRunnerProvider;
import com.eviware.loadui.util.RealClock;
import com.eviware.loadui.util.test.FakeClock;
import com.eviware.loadui.util.test.FakeHttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URI;

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

	public RequestRunner provideRequestRunner( ComponentContext context, Iterable<URI> pageUris )
	{
		Clock clock = new FakeClock();
		return new RequestRunner( clock,
				httpClient,
				pageUris,
				new WebRunnerStatsSender( context, clock ) );
	}
}
