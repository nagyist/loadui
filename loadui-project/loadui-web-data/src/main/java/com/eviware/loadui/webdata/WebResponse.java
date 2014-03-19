package com.eviware.loadui.webdata;

public abstract class WebResponse
{

	private final byte[] responseData;

	protected WebResponse( byte[] responseData )
	{
		this.responseData = responseData;
	}

	public abstract byte[] getResponseData();

}
