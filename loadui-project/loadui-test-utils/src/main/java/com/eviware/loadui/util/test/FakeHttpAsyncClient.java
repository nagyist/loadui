package com.eviware.loadui.util.test;

import com.google.common.base.Function;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import org.apache.http.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FakeHttpAsyncClient extends CloseableHttpAsyncClient
{
	protected static final Logger log = LoggerFactory.getLogger( FakeHttpAsyncClient.class );
	public static final String DEFAULT_RESPONSE = "Ok!";

	private final BlockingQueue<HttpRequest> handledRequests = new LinkedBlockingQueue<>();

	public HttpRequest popRequest() throws InterruptedException
	{
		return checkNotNull( handledRequests.poll( 5, SECONDS ), "No more requests received." );
	}

	public Multiset<String> awaitRequests( int expectedNumberOfRequests ) throws InterruptedException
	{
		Set<HttpRequest> polledRequests = new HashSet<>();
		while( expectedNumberOfRequests > polledRequests.size() )
		{
			polledRequests.add( handledRequests.poll( 5, SECONDS ) );
		}
		return HashMultiset.create( Iterables.transform( polledRequests, new Function<HttpRequest, String>()
		{
			@Nullable
			@Override
			public String apply( @Nullable HttpRequest request )
			{
				return request.getRequestLine().getMethod() + " " + request.getRequestLine().getUri();
			}
		} ) );
	}

	public boolean hasReceivedRequests()
	{
		return !handledRequests.isEmpty();
	}

	@Override
	public void close() throws IOException
	{

	}

	@Override
	public boolean isRunning()
	{
		return false;
	}

	@Override
	public void start()
	{

	}

	@Override
	public <T> Future<T> execute( HttpAsyncRequestProducer httpAsyncRequestProducer,
											final HttpAsyncResponseConsumer<T> tHttpAsyncResponseConsumer,
											final HttpContext httpContext,
											final FutureCallback<T> tFutureCallback )
	{
		final CloseableHttpResponse response;
		try
		{
			response = executeChecked( httpAsyncRequestProducer );
		}
		catch( IOException | HttpException e )
		{
			throw new RuntimeException( e );
		}

		Future<T> future = Executors.newSingleThreadExecutor().submit( new Callable<T>()
		{
			@Override
			public T call() throws Exception
			{
				Thread.sleep( 200 );

				ContentDecoder decoder = new ContentDecoder()
				{
					boolean isCompleted;

					@Override
					public int read( ByteBuffer dst ) throws IOException
					{
						byte[] bytes = DEFAULT_RESPONSE.getBytes();
						dst.put( bytes );
						isCompleted = true;
						return bytes.length;
					}

					@Override
					public boolean isCompleted()
					{
						return isCompleted;
					}
				};

				tHttpAsyncResponseConsumer.responseReceived( response );
				tHttpAsyncResponseConsumer.consumeContent( decoder, null );
				tHttpAsyncResponseConsumer.responseCompleted( httpContext );
				T result = tHttpAsyncResponseConsumer.getResult();
				tFutureCallback.completed( result );
				return result;
			}
		} );
		return future;
	}

	private CloseableHttpResponse executeChecked( HttpAsyncRequestProducer httpAsyncRequestProducer ) throws IOException, HttpException
	{
		HttpRequest request = httpAsyncRequestProducer.generateRequest();

		System.out.println( "Executing request: " + request );
		handledRequests.add( request );

		HttpEntity entity = mock( HttpEntity.class );
		when( entity.getContent() ).thenReturn( new ByteArrayInputStream( DEFAULT_RESPONSE.getBytes() ) );

		CloseableHttpResponse response = mock(CloseableHttpResponse.class);
		when( response.getEntity() ).thenReturn( entity );

		StatusLine statusLine = mock(StatusLine.class);

		int statusCode = 200;
		try
		{
			statusCode = Integer.parseInt( URI.create( request.getRequestLine().getUri() ).getHost() );
		}
		catch( NumberFormatException e )
		{
			// ignore
		}

		when( statusLine.getStatusCode() ).thenReturn( statusCode );
		when( response.getStatusLine() ).thenReturn( statusLine );

		return response;
	}
}
