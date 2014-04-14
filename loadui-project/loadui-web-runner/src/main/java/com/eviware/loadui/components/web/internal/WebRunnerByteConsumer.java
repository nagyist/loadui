package com.eviware.loadui.components.web.internal;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.components.web.WebRunnerStatsSender;
import com.eviware.loadui.webdata.HttpWebResponse;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncByteConsumer;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

class WebRunnerByteConsumer extends AsyncByteConsumer<HttpWebResponse>
{
	private final long startTime;
	private final String resource;
	private volatile HttpResponse response;
	private boolean hasFirstByte;
	private final AtomicLong length;
	private final WebRunnerStatsSender statsSender;
	private final Clock clock;

	public WebRunnerByteConsumer( long startTime, String resource, WebRunnerStatsSender statsSender, Clock clock )
	{
		this.startTime = startTime;
		this.resource = resource;
		this.statsSender = statsSender;
		this.clock = clock;
		hasFirstByte = false;
		length = new AtomicLong( 0 );
	}

	@Override
	protected void onByteReceived( ByteBuffer buf, IOControl ioctrl )
			throws IOException
	{
		if( !hasFirstByte )
		{
			long latency = clock.millis() - startTime;
			statsSender.updateLatency( resource, latency );
			RequestRunnerExecutor.log.debug( "It took {}ms to to get the first byte for {}", latency, resource );
		}
		hasFirstByte = true;

		long bytesRead = 0;
		while( buf.position() < buf.limit() )
		{
			buf.get();
			bytesRead++;
		}
		length.addAndGet( bytesRead );
	}

	@Override
	protected void onResponseReceived( HttpResponse response )
			throws HttpException, IOException
	{
		RequestRunnerExecutor.log.debug( "Called onResponseReceived for {}", resource );
		this.response = response;
	}

	@Override
	protected HttpWebResponse buildResult( HttpContext context )
			throws Exception
	{
		return HttpWebResponse.of( response, length.get() );
	}
}
