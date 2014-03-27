package com.eviware.loadui.components.web.internal;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketFactoryProvider
{

	static final Logger log = LoggerFactory.getLogger( SocketFactoryProvider.class );

	public SSLConnectionSocketFactory newSocketFactory()
	{
		log.debug( "Creating a new SSLConnectionSocketFactory" );
		try
		{
			return new SSLConnectionSocketFactory(
					new SSLContextBuilder()
							.loadTrustMaterial( null, new TrustSelfSignedStrategy() ).build(),
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
			);
		}
		catch( Exception e )
		{
			log.warn( "Could not build a SSLConnectionSocketFactory, will provide the default one", e );
			return SSLConnectionSocketFactory.getSocketFactory();
		}
	}


}
