package com.eviware.loadui.components.web;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.components.web.api.RequestRunnerProvider;
import com.eviware.loadui.impl.component.categories.RunnerBase;
import com.eviware.loadui.util.html.HtmlAssetScraper;

import java.io.IOException;
import java.net.URI;

public class WebRunner extends RunnerBase
{

	private final Property<String> webPageUrlProperty;
	private final HtmlAssetScraper scraper;
	private final RequestRunnerProvider requestRunnerProvider;
	private RequestRunner requestRunner;
	public static final String WEB_PAGE_URL_PROP = "webPageUrl";

	public WebRunner( ComponentContext context, HtmlAssetScraper scraper, RequestRunnerProvider requestRunnerProvider )
	{
		super( context );
		this.scraper = scraper;
		this.requestRunnerProvider = requestRunnerProvider;
		this.webPageUrlProperty = context.createProperty( WEB_PAGE_URL_PROP, String.class );

		context.addEventListener( PropertyEvent.class, new EventHandler<PropertyEvent>()
		{
			@Override
			public void handleEvent( PropertyEvent event )
			{
				if( event.getEvent() == PropertyEvent.Event.VALUE &&
						event.getProperty() == webPageUrlProperty )
				{
					updateWebPageUrl( webPageUrlProperty.getValue() );
				}
			}
		} );

		context.setLayout( new WebRunnerLayout() );
	}

	private void updateWebPageUrl( String url )
	{
		try
		{
			validateUrl( url );
			requestRunner = requestRunnerProvider.provideRequestRunner( getContext(), scraper.scrapeUrl( url ) );
		}
		catch( IllegalArgumentException e )
		{
			//TODO invalid URL, do something

		}
		catch( IOException e )
		{
			//TODO could not scrap!
		}
	}

	private void validateUrl( String url ) throws IllegalArgumentException
	{
		if( url == null )
		{
			throw new IllegalArgumentException( "URL cannot be null" );
		}
		URI.create( url );
	}

	@Override
	protected TerminalMessage sample( TerminalMessage triggerMessage, Object sampleId ) throws SampleCancelledException
	{
		if( requestRunner == null )
			throw new SampleCancelledException();
		requestRunner.run();
		return triggerMessage;
	}

	@Override
	protected int onCancel()
	{
		//TODO implement
		return 0;
	}


}
