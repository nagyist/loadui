package com.eviware.loadui.webdata;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import javax.annotation.concurrent.Immutable;
import java.util.concurrent.atomic.AtomicLong;

@Immutable
public class HttpWebResponse
{
	private final int responseCode;
	private final Header contentType;
	private final Header contentEncoding;
	private final long length;

	private HttpWebResponse( int responseCode, Header contentType, Header contentEncoding, long length )
	{
		this.responseCode = responseCode;
		this.contentType = contentType;
		this.contentEncoding = contentEncoding;
		this.length = length;
	}

	public int getResponseCode()
	{
		return responseCode;
	}

	public long getContentLength()
	{
		return length;
	}

	public static HttpWebResponse of( HttpResponse response, long length )
	{
		int responseCode = response.getStatusLine().getStatusCode();
		Header contentType = response.getEntity().getContentType();
		Header contentEncoding = response.getEntity().getContentEncoding();

		return new HttpWebResponse(
				responseCode, contentType, contentEncoding, length );
	}

	@Override
	public String toString()
	{
		return "ProcessedHttpResponse{" +
				"responseCode=" + responseCode +
				", contentType=" + contentType +
				", contentEncoding=" + contentEncoding +
				", length=" + length +
				'}';
	}

}
