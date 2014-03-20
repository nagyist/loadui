package com.eviware.loadui.components.rest;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.property.Property;

public class UrlProperty
{
	public static final String URL = "url";
	public static final String HTTP = "http://";
	public static final String HTTPS = "https://";

	private String url = "";

	public UrlProperty( ComponentContext context )
	{
		final Property<String> urlProperty = context.createProperty( URL, String.class, "" );
		context.addEventListener( PropertyEvent.class, new EventHandler<PropertyEvent>()
		{
			@Override
			public void handleEvent( PropertyEvent event )
			{
				if( event.getProperty() == urlProperty )
				{
					url = tidyUrl( urlProperty.getStringValue() );
				}
			}
		} );
		url = tidyUrl( urlProperty.getStringValue() );
	}

	private String tidyUrl( String url )
	{
		String lowerCaseUrl = url.toLowerCase();
		if( !lowerCaseUrl.startsWith( HTTP ) && !lowerCaseUrl.startsWith( HTTPS ) )
		{
			return HTTP + lowerCaseUrl;
		}
		return lowerCaseUrl;
	}

	public String getUrl()
	{
		return url;
	}
}
