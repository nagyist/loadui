package com.eviware.loadui.components.rest;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class HeaderManager
{
	public static final String HEADERS = "headers";

	private Multimap<String, String> headers;

	public HeaderManager( final ComponentContext context )
	{
		context.createProperty( HEADERS, String.class, "" );

		updateHeaders( context );

		context.addEventListener( PropertyEvent.class, new EventHandler<PropertyEvent>()
		{
			@Override
			public void handleEvent( PropertyEvent event )
			{
				if( HEADERS.equals( event.getProperty().getKey() ) )
				{
					updateHeaders( context );
				}
			}
		} );
	}

	private void updateHeaders( ComponentContext context )
	{
		headers = HeaderManager.extractHeaders( context.getProperty( HEADERS ).getStringValue() );
	}

	public Multimap<String, String> getHeaders()
	{
		return headers;
	}

	static Multimap<String, String> extractHeaders( String headersBlob )
	{
		Iterable<String> headersIterable = Splitter.on( System.lineSeparator() )
				.trimResults()
				.omitEmptyStrings()
				.split( headersBlob );

		Multimap<String, String> headers = LinkedHashMultimap.create();

		for( String header : headersIterable )
		{
			Iterable<String> keyValue = Splitter.on( ":" )
					.trimResults()
					.split( header );
			headers.put( Iterables.get( keyValue, 0 ), Iterables.get( keyValue, 1 ) );
		}
		return headers;
	}
}
