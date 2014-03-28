package com.eviware.loadui.util.property;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.util.serialization.ListenableValueSupport;

public class UrlProperty
{
	public static final String URL = "url";
	public static final String HTTP = "http://";
	public static final String HTTPS = "https://";

	private ListenableValueSupport<String> listenableValueSupport = new ListenableValueSupport<>();

	private String url = "";
	private final Property<String> urlProperty;

	public UrlProperty( ComponentContext context )
	{
		this.urlProperty = context.createProperty( URL, String.class, "" );
		context.addEventListener( PropertyEvent.class, new EventHandler<PropertyEvent>()
		{
			@Override
			public void handleEvent( PropertyEvent event )
			{
				if( event.getProperty() == urlProperty )
				{
					url = tidyUrl( urlProperty.getStringValue() );
					listenableValueSupport.update( url );
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

	public void addUrlChangeListener( ListenableValue.ValueListener<String> listener )
	{
		listenableValueSupport.addListener( listener );
	}

	public Property<String> getUrlProperty()
	{
		return urlProperty;
	}

}
