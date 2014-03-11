package com.eviware.loadui.components.rest;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.components.rest.statistics.LatencyCalculator;
import com.eviware.loadui.impl.component.categories.RunnerBase;
import com.eviware.loadui.impl.statistics.SampleStatisticsWriter;
import com.eviware.loadui.util.RealClock;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.xml.sax.SAXException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class RestRunner extends RunnerBase
{
	public static final String URL = "url";
	public static final String METHOD = "method";
	public static final String BODY = "body";

	private final HttpClient httpClient;
	private final Clock clock;
	private final LatencyCalculator latencyCalculator;
	private final StatisticVariable.Mutable latencyVariable;

	public RestRunner( ComponentContext context, HttpClient httpClient, Clock clock )
	{
		super( context );
		this.httpClient = httpClient;
		this.clock = clock;

		context.createProperty( URL, String.class );
		context.createProperty( METHOD, String.class, "GET" );
		context.createProperty( BODY, String.class );

		latencyCalculator = LatencyCalculator.usingClock( clock );
		latencyVariable = context.addStatisticVariable( "Latency", "", SampleStatisticsWriter.TYPE );

		context.setLayout( new RestLayout( context ) );
	}

	public RestRunner( ComponentContext context )
	{
		this( context, RestHttpClient.create(), new RealClock() );
	}

	@Override
	protected TerminalMessage sample( TerminalMessage triggerMessage, Object sampleId ) throws SampleCancelledException
	{
		HttpUriRequest request = generateRequest();
		HttpResponse response;
		try
		{
			response = httpClient.execute( request );
			long currentTime = clock.millis();
			long latency = latencyCalculator.calculate( response.getEntity().getContent(), ( Long )sampleId );
			latencyVariable.update( currentTime, latency );
		}
		catch( IOException e )
		{
			throw new RuntimeException( e );
		}

		return triggerMessage;
	}

	private HttpUriRequest generateRequest()
	{
		String method = getContext().getProperty( METHOD ).getStringValue();
		CustomHttpRequest request = new CustomHttpRequest( method, getPropertyValue( URL ) );
		if( request.canHaveBody() )
			request.setEntity( new StringEntity( getPropertyValue( BODY ), "UTF-8" ) );
		return request;
	}

	private String getPropertyValue( String propertyName )
	{
		return getContext().getProperty( propertyName ).getStringValue();
	}

	@Override
	protected int onCancel()
	{
		//TODO implement
		return 0;
	}
}
