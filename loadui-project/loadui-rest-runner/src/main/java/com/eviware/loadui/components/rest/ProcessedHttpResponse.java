package com.eviware.loadui.components.rest;

import org.apache.http.Header;

public class ProcessedHttpResponse
{
	private final int responseCode;
	private final long contentLength;
	private final Header contentType;
	private final Header contentEncoding;
	private final byte[] content;
	private final long responseTime;

	public ProcessedHttpResponse( int responseCode, long contentLength, Header contentType,
											Header contentEncoding, byte[] content, long responseTime )
	{
		this.responseCode = responseCode;
		this.contentLength = contentLength;
		this.contentType = contentType;
		this.contentEncoding = contentEncoding;
		this.content = content;
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

	public byte[] getContent()
	{
		return content;
	}

	public long getResponseTime()
	{
		return responseTime;
	}

	@Override
	public String toString()
	{
		String contentText;
		if( contentType != null && contentType.getValue().toLowerCase().contains( "text" ) )
			contentText = new String( content );
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
