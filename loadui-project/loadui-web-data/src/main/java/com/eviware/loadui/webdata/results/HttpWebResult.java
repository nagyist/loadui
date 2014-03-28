package com.eviware.loadui.webdata.results;

import com.eviware.loadui.webdata.HttpWebResponse;
import org.apache.http.client.methods.CloseableHttpResponse;

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

	public static HttpWebResult of( CloseableHttpResponse response )
	{
		try
		{
			return new HttpWebResult( HttpWebResponse.of( response ) );
		}
		catch( Exception e )
		{
			return new HttpWebResult( e );
		}
	}

	public static HttpWebResult of( Exception e )
	{
		return new HttpWebResult( e );
	}

}
