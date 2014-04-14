package com.eviware.loadui.components.web.internal;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.components.web.RequestRunner;
import com.eviware.loadui.components.web.WebRunnerStatsSender;
import com.eviware.loadui.webdata.HttpWebResponse;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public class HttpWebResponseCallback implements FutureCallback<HttpWebResponse>
{
	static final Logger log = LoggerFactory.getLogger( HttpWebResponseCallback.class );

	private final String resource;
	private final Clock clock;
	private final RequestRunner.Request request;
	private final WebRunnerStatsSender statsSender;
	private final long startTime;
	private final SettableFuture<Boolean> result;

	public HttpWebResponseCallback( long startTime, String resource, WebRunnerStatsSender statsSender, Clock clock, RequestRunner.Request request, SettableFuture<Boolean> result )
	{
		this.resource = resource;
		this.clock = clock;
		this.request = request;
		this.statsSender = statsSender;
		this.startTime = startTime;
		this.result = result;
	}

	@Override
	public void completed( HttpWebResponse webResponse )
	{
		log.debug( "Completed request for {}", resource );
		boolean failed = request.isFailure( webResponse );
		if( failed )
		{
			failed( new RuntimeException( "Request reported webResponse constitutes a failure" ) );
		}
		else
		{
			long contentLength = webResponse.getContentLength();
			statsSender.updateResponse( resource, clock.millis() - startTime, contentLength );
			result.set( true );
		}
	}

	@Override
	public void failed( Exception error )
	{
		request.handleError( new RuntimeException( "Request to " + resource + " failed", error ) );
		result.set( false );
	}

	@Override
	public void cancelled()
	{
		result.set( false );
	}
}