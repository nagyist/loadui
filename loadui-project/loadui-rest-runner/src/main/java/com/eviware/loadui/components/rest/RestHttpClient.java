package com.eviware.loadui.components.rest;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class RestHttpClient
{
	public static final int MAX_CONNECTIONS = 50_000;

	public static HttpClient create()
	{
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal( MAX_CONNECTIONS );
		connectionManager.setDefaultMaxPerRoute( MAX_CONNECTIONS );
		return HttpClientBuilder.create()
				.setConnectionManager( connectionManager )
				.setSslcontext( createAndInitSSLContext() )
				.build();
	}

	private static SSLContext createAndInitSSLContext()
	{
		SSLContext context;
		try
		{
			context = SSLContext.getInstance( "SSL" );
		}
		catch( Exception e )
		{
			try
			{
				context = SSLContext.getDefault();
			}
			catch( Exception e1 )
			{
				throw new RuntimeException( "Unable to create any SSLContext.", e1 );
			}
		}

		TrustManager[] tms = { new NaiveTrustManager() };

		try
		{
			context.init( new KeyManager[0], tms, new SecureRandom() );
			return context;
		}
		catch( Exception e )
		{
			throw new RuntimeException( "Unable to initialize the SSLContext.", e );
		}
	}

	//SSL support, trust all certificates and host names.
	private static class NaiveTrustManager implements X509TrustManager
	{

		@Override
		public void checkClientTrusted( X509Certificate[] cert, String authType ) throws CertificateException
		{
		}

		@Override
		public void checkServerTrusted( X509Certificate[] cert, String authType ) throws CertificateException
		{
		}

		@Override
		public X509Certificate[] getAcceptedIssuers()
		{
			return null;
		}
	}
}
