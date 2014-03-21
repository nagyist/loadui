package com.eviware.loadui.util.test;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FakeHttpClient extends CloseableHttpClient
{
	private BlockingQueue<HttpUriRequest> handledRequests = new LinkedBlockingQueue<>();

	public HttpUriRequest lastRequest() throws InterruptedException
	{
		return checkNotNull( handledRequests.poll( 5, SECONDS ), "No more requests received." );
	}

	public boolean hasReceivedRequests()
	{
		return !handledRequests.isEmpty();
	}

	@Override
	public HttpParams getParams()
	{
		return null;
	}

	@Override
	public ClientConnectionManager getConnectionManager()
	{
		return null;
	}

	@Override
	public CloseableHttpResponse execute( HttpUriRequest httpUriRequest ) throws IOException
	{
		handledRequests.add( httpUriRequest );

		HttpEntity entity = mock( HttpEntity.class );
		when(entity.getContent()).thenReturn( new ByteArrayInputStream( "Ok!".getBytes() ) );

		CloseableHttpResponse response = mock(CloseableHttpResponse.class);
		when(response.getEntity()).thenReturn( entity );

		return response;
	}

	@Override
	public CloseableHttpResponse execute( HttpUriRequest httpUriRequest, HttpContext httpContext ) throws IOException, ClientProtocolException
	{
		return null;
	}

	@Override
	public CloseableHttpResponse execute( HttpHost httpHost, HttpRequest httpRequest ) throws IOException, ClientProtocolException
	{
		return null;
	}

	@Override
	protected CloseableHttpResponse doExecute( HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext ) throws IOException, ClientProtocolException
	{
		return null;
	}

	@Override
	public CloseableHttpResponse execute( HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext ) throws IOException, ClientProtocolException
	{
		return null;
	}

	@Override
	public <T> T execute( HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler ) throws IOException, ClientProtocolException
	{
		return null;
	}

	@Override
	public <T> T execute( HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler, HttpContext httpContext ) throws IOException, ClientProtocolException
	{
		return null;
	}

	@Override
	public <T> T execute( HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<? extends T> responseHandler ) throws IOException, ClientProtocolException
	{
		return null;
	}

	@Override
	public <T> T execute( HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<? extends T> responseHandler, HttpContext httpContext ) throws IOException, ClientProtocolException
	{
		return null;
	}

	@Override
	public void close() throws IOException
	{

	}
}
