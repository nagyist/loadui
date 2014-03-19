package com.eviware.loadui.webdata.results;

import com.eviware.loadui.webdata.HttpWebResponse;
import org.apache.http.client.methods.CloseableHttpResponse;

public class HttpWebResult extends WebResult<HttpWebResponse>
{

	public HttpWebResult( HttpWebResponse response )
	{
		super( response );
	}

	public HttpWebResult( Exception e )
	{
		super( e );
	}

	public static HttpWebResult of( CloseableHttpResponse response, long startTime )
	{
		try
		{
			return new HttpWebResult( HttpWebResponse.of( response, startTime ) );
		}
		catch( Exception e )
		{
			return new HttpWebResult( e );
		}
	}

}
