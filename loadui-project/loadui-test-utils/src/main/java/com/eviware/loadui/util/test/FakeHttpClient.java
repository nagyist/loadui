package com.eviware.loadui.util.test;

import com.google.common.base.Function;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FakeHttpClient extends CloseableHttpClient
{
	protected static final Logger log = LoggerFactory.getLogger( FakeHttpClient.class );

	private final BlockingQueue<HttpUriRequest> handledRequests = new LinkedBlockingQueue<>();

	public HttpUriRequest popRequest() throws InterruptedException
	{
		return checkNotNull( handledRequests.poll( 5, SECONDS ), "No more requests received." );
	}

	public Multiset<String> popAllRequests() throws InterruptedException
	{
		Set<HttpUriRequest> result = new HashSet<>();
		handledRequests.drainTo( result );
		return HashMultiset.create( Iterables.transform( result, new Function<HttpUriRequest, String>()
		{
			@Nullable
			@Override
			public String apply( @Nullable HttpUriRequest request )
			{
				return request.getMethod() + " " + request.getURI();
			}
		} ) );
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

		StatusLine statusLine = mock(StatusLine.class);

		int statusCode = 200;
		try
		{
			statusCode = Integer.parseInt( httpUriRequest.getURI().getHost() );
		}
		catch( NumberFormatException e )
		{
			// ignore
		}

		when( statusLine.getStatusCode() ).thenReturn( statusCode );
		when( response.getStatusLine() ).thenReturn( statusLine );

		return response;
	}

	@Override
	public CloseableHttpResponse execute( HttpUriRequest httpUriRequest, HttpContext httpContext ) throws IOException
	{
		return null;
	}

	@Override
	public CloseableHttpResponse execute( HttpHost httpHost, HttpRequest httpRequest ) throws IOException
	{
		return null;
	}

	@Override
	protected CloseableHttpResponse doExecute( HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext ) throws IOException
	{
		return null;
	}

	@Override
	public CloseableHttpResponse execute( HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext ) throws IOException
	{
		return null;
	}

	@Override
	public <T> T execute( HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler ) throws IOException
	{
		return null;
	}

	@Override
	public <T> T execute( HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler, HttpContext httpContext ) throws IOException
	{
		return null;
	}

	@Override
	public <T> T execute( HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<? extends T> responseHandler ) throws IOException
	{
		return null;
	}

	@Override
	public <T> T execute( HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<? extends T> responseHandler, HttpContext httpContext ) throws IOException
	{
		return null;
	}

	@Override
	public void close() throws IOException
	{

	}
}
