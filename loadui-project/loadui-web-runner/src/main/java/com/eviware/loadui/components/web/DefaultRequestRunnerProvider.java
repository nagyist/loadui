package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.components.web.api.RequestRunnerProvider;
import com.eviware.loadui.components.web.internal.SocketFactoryProvider;
import com.eviware.loadui.util.RealClock;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;

import java.io.IOException;
import java.net.URI;

public class DefaultRequestRunnerProvider implements RequestRunnerProvider
{
	private final Clock clock = new RealClock();
	private WebRunnerStatsSender statisticsSender;
	private SocketFactoryProvider socketFactoryProvider = new SocketFactoryProvider();

	public RequestRunner provideRequestRunner( ComponentContext context, URI pageUri, Iterable<URI> assetUris )
			throws IOException
	{
		try
		{
			IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
					.setIoThreadCount( Runtime.getRuntime().availableProcessors() )
					.setConnectTimeout( 30_000 )
					.setSoTimeout( 30_000 )
					.build();

			ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor( ioReactorConfig );

			Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder.<SchemeIOSessionStrategy>create()
					.register( "http", NoopIOSessionStrategy.INSTANCE )
					.register( "https", new SSLIOSessionStrategy(
							socketFactoryProvider.newSSLContext(),
							socketFactoryProvider.getHostnameVerifier() ) )
					.build();

			PoolingNHttpClientConnectionManager connectionManager =
					new PoolingNHttpClientConnectionManager( ioReactor, sessionStrategyRegistry );
			connectionManager.setDefaultMaxPerRoute( 1_000 );
			connectionManager.setMaxTotal( 5_000 );

			CloseableHttpAsyncClient client = HttpAsyncClients.custom()
					.setMaxConnTotal( 5_000 )
					.setMaxConnPerRoute( 1_000 )
					.setConnectionManager( connectionManager )
					.build();

			return new RequestRunner( clock, client, pageUri, assetUris, new WebRunnerStatsSender( context, clock ) );
		}
		catch( Exception e )
		{
			throw new IOException( "Could not create a request runner", e );
		}

	}
}
