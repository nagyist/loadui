package com.eviware.loadui.webdata.results;

import com.eviware.loadui.webdata.WebResponse;

import static com.google.common.base.Preconditions.checkNotNull;

public class WebResult<T extends WebResponse>
{

	private final Exception exception;
	private final T response;

	public WebResult( T response )
	{
		checkNotNull( response );
		this.exception = null;
		this.response = response;
	}

	public WebResult( Exception e )
	{
		checkNotNull( e );
		this.exception = e;
		this.response = null;
	}

	public Exception getException()
	{
		return exception;
	}

	public T getResponse()
	{
		return response;
	}

	public boolean isFailure()
	{
		return exception != null;
	}

}
