package com.eviware.loadui.components.rest.result;

import com.eviware.loadui.components.rest.ProcessedHttpResponse;

import static com.google.common.base.Preconditions.checkNotNull;

public class DispatchResult
{
	private final ProcessedHttpResponse response;
	private final Exception exception;
	private final int expectedStatusCode;

	public DispatchResult( ProcessedHttpResponse response, int expectedStatusCode )
	{
		checkNotNull( response );
		this.response = response;
		this.exception = null;
		this.expectedStatusCode = expectedStatusCode;
	}

	public DispatchResult( Exception exception, int expectedStatusCode )
	{
		checkNotNull( exception );
		this.exception = exception;
		this.response = null;
		this.expectedStatusCode = expectedStatusCode;
	}

	public boolean isFailed()
	{
		return exception != null;
	}

	public ProcessedHttpResponse getResponse()
	{
		if( isFailed() )
		{
			throw new RuntimeException( "The result of this dispatch was an Exception. Ensure isFailed() returns false before calling this method" );
		}
		return response;
	}

	public int getExpectedStatusCode()
	{
		return expectedStatusCode;
	}

}
