package com.eviware.loadui.components.web;

import com.eviware.loadui.util.RealClock;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URI;

public class RequestRunnerProvider
{

	public RequestRunner provideRequestRunner( Iterable<URI> pageUris )
	{
		return new RequestRunner( new RealClock(), HttpClientBuilder.create().build(), pageUris );
	}

}
