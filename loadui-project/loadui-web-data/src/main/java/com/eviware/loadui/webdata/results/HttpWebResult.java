package com.eviware.loadui.webdata.results;

import com.eviware.loadui.webdata.HttpWebResponse;

public class HttpWebResult extends WebResult<HttpWebResponse>
{

	private HttpWebResult( HttpWebResponse response )
	{
		super( response );
	}

	private HttpWebResult( Exception e )
	{
		super( e );
	}

	public static HttpWebResult of( HttpWebResponse response )
	{
		return new HttpWebResult( response );
	}

	public static HttpWebResult of( Exception e )
	{
		return new HttpWebResult( e );
	}

}
