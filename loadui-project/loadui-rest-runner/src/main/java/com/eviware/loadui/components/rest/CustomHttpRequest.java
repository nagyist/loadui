package com.eviware.loadui.components.rest;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

public class CustomHttpRequest extends HttpEntityEnclosingRequestBase
{
	private final String method;

	public CustomHttpRequest( String method, String url )
	{
		setURI( URI.create( url ) );
		this.method = method;
	}

	@Override
	public String getMethod()
	{
		return method;
	}

	public boolean canHaveBody()
	{
		return method.equals( "POST" ) || method.equals( "PUT" );
	}
}
