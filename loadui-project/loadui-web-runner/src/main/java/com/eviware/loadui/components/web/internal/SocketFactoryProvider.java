package com.eviware.loadui.components.web.internal;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class SocketFactoryProvider
{

	static final Logger log = LoggerFactory.getLogger( SocketFactoryProvider.class );

	public SSLContext newSSLContext() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException
	{
		return new SSLContextBuilder()
				.loadTrustMaterial( null, new TrustSelfSignedStrategy() ).build();
	}

	public X509HostnameVerifier getHostnameVerifier()
	{
		return SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
	}

}
