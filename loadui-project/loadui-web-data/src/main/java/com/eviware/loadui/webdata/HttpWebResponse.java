package com.eviware.loadui.webdata;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import javax.annotation.Nullable;
import java.io.IOException;

public class HttpWebResponse implements WebResponse
{

	private final int responseCode;
	private final Header contentType;
	private final Header contentEncoding;
	private HttpEntity entity;
	private boolean responseConsumed = false;

	@Nullable
	private byte[] responseContents;

	private StreamConsumer consumer = new StreamConsumer();

	public HttpWebResponse( int responseCode, Header contentType, Header contentEncoding, HttpEntity entity )
	{
		this.responseCode = responseCode;
		this.contentType = contentType;
		this.contentEncoding = contentEncoding;
		this.entity = entity;
	}

	public void setConsumer( StreamConsumer consumer )
	{
		this.consumer = consumer;
	}

	private void ensureResponseConsumed()
	{
		if( responseConsumed ) return;

		try
		{
			responseContents = consumer.consume( entity.getContent() );
		}
		catch( IOException e )
		{
			throw new RuntimeException( "Could not download response body", e );
		} finally
		{
			responseConsumed = true;
		}
	}

	public int getResponseCode()
	{
		return responseCode;
	}

	public long getContentLength()
	{
		ensureResponseConsumed();
		return responseContents == null ? 0 : responseContents.length;
	}

	public Header getContentType()
	{
		return contentType;
	}

	public Header getContentEncoding()
	{
		return contentEncoding;
	}

	@Override
	public byte[] getResponseData()
	{
		ensureResponseConsumed();
		return responseContents == null ? new byte[0] : responseContents;
	}

	public static HttpWebResponse of( HttpResponse response )
	{
		int responseCode = response.getStatusLine().getStatusCode();
		Header contentType = response.getEntity().getContentType();
		Header contentEncoding = response.getEntity().getContentEncoding();

		return new HttpWebResponse(
				responseCode, contentType, contentEncoding, response.getEntity() );
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
				", contentType=" + contentType +
				", contentEncoding=" + contentEncoding +
				", content=" + contentText +
				", responseConsumed=" + responseConsumed +
				'}';
	}

}
