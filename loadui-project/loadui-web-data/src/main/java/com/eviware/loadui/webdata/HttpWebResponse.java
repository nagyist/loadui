package com.eviware.loadui.webdata;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;

import static com.google.common.io.ByteStreams.toByteArray;

public class HttpWebResponse extends WebResponse
{

	private final int responseCode;
	private final long contentLength;
	private final Header contentType;
	private final Header contentEncoding;
	private final long responseTime;

	public HttpWebResponse( int responseCode, long contentLength, Header contentType,
									Header contentEncoding, byte[] content, long responseTime )
	{
		super( content );
		this.responseCode = responseCode;
		this.contentLength = contentLength;
		this.contentType = contentType;
		this.contentEncoding = contentEncoding;
		this.responseTime = responseTime;
	}

	public int getResponseCode()
	{
		return responseCode;
	}

	public long getContentLength()
	{
		return contentLength;
	}

	public Header getContentType()
	{
		return contentType;
	}

	public Header getContentEncoding()
	{
		return contentEncoding;
	}

	public long getResponseTime()
	{
		return responseTime;
	}

	@Override
	public byte[] getResponseData()
	{
		return new byte[0];
	}

	public static HttpWebResponse of( CloseableHttpResponse response, long startTime )
	{
		int responseCode = response.getStatusLine().getStatusCode();
		Header contentType = response.getEntity().getContentType();
		Header contentEncoding = response.getEntity().getContentEncoding();

		byte[] content;
		try
		{
			content = toByteArray( response.getEntity().getContent() );
		}
		catch( IOException e )
		{
			throw new RuntimeException( "Could not download response body", e );
		}

		long contentLength = content.length;
		long responseTime = System.currentTimeMillis() - startTime;

		return new HttpWebResponse(
				responseCode, contentLength, contentType, contentEncoding, content, responseTime );
	}

	@Override
	public String toString()
	{
		String contentText;
		if( contentType != null && contentType.getValue().toLowerCase().contains( "text" ) )
			contentText = new String( getResponseData() );
		else
			contentText = "%BINARY_DATA%";

		return "ProcessedHttpResponse{" +
				"responseCode=" + responseCode +
				", contentLength=" + contentLength +
				", contentType=" + contentType +
				", contentEncoding=" + contentEncoding +
				", content=" + contentText +
				", responseTime=" + responseTime +
				'}';
	}

}
