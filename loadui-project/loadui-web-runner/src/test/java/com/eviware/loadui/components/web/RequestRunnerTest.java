package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.util.test.FakeHttpAsyncClient;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class RequestRunnerTest
{
	public static final String URL_1 = "http://example.org/url1";
	public static final String URL_2 = "http://example.org/url2";
	Clock clock;
	FakeHttpAsyncClient httpClient;
	List<URI> uris = new ArrayList<>();
	WebRunnerStatsSender mockStatsSender;
	ArgumentCaptor<List> resourcesCaptor;

	@Before
	public void setup() throws Exception
	{
		clock = mock( Clock.class );
		when( clock.millis() ).thenReturn( 0L, 10L, 20L, 30L, 40L, 50L );

		httpClient = new FakeHttpAsyncClient();

		CloseableHttpResponse mockResponse = mock( CloseableHttpResponse.class );
		StatusLine mockStatusLine = mock( StatusLine.class );
		when( mockStatusLine.getStatusCode() ).thenReturn( 200 );
		when( mockResponse.getStatusLine() ).thenReturn( mockStatusLine );

		HttpEntity mockEntity = mock( HttpEntity.class );
		when( mockResponse.getEntity() ).thenReturn( mockEntity );
		Header mockContentType = mock( Header.class );
		Header mockContentEncoding = mock( Header.class );
		when( mockEntity.getContentType() ).thenReturn( mockContentType );
		when( mockEntity.getContentEncoding() ).thenReturn( mockContentEncoding );

		final SettableFuture<HttpResponse> mockResponseFuture = SettableFuture.create();
		mockResponseFuture.set( mockResponse );

		mockStatsSender = mock( WebRunnerStatsSender.class );
		resourcesCaptor = ArgumentCaptor.forClass(List.class);
	}

	@Test
	public void allResourcesAreAdded()
	{
		List<URI> urisToTest = asList( URI.create( URL_1 ), URI.create( URL_2 ) );
		uris.addAll( urisToTest );
		RequestRunner runner = createRunner();

		runner.call();

		verify( mockStatsSender, times( 2 ) ).addResources( resourcesCaptor.capture() );

		List<URI> addedUris = new ArrayList<>();
		for( Iterable<URI> uris : resourcesCaptor.getAllValues() )
		{
			Iterables.addAll( addedUris, uris );
		}

		assertThat( addedUris, is( urisToTest ) );
	}

	@Test
	public void allRequestsArePerformed() throws Exception
	{
		uris.addAll( asList( URI.create( URL_1 ), URI.create( URL_2 ) ) );
		RequestRunner runner = createRunner();

		runner.call();

		httpClient.awaitRequests( 2 );
	}

	@Test
	public void resultsAreUpdatedOnRequest() throws Exception
	{
		uris.addAll( asList( URI.create( URL_1 ) ) );

		RequestRunner runner = createRunner();

		runner.call();

		httpClient.awaitRequests( 1 );

		verify( mockStatsSender ).updateRequestSent( URL_1 );
		verify( mockStatsSender ).updateLatency( URL_1, 10L );
		verify( mockStatsSender ).updateResponse( URL_1, 20, FakeHttpAsyncClient.DEFAULT_RESPONSE.length() );
	}

	private RequestRunner createRunner()
	{
		Iterator<URI> iter = uris.iterator();
		URI pageUri = iter.next();
		Iterable<URI> assets = Iterables.skip( uris, 1 );
		RequestRunner runner = new RequestRunner( clock, httpClient, pageUri, assets, mockStatsSender );
		return runner;
	}

}
