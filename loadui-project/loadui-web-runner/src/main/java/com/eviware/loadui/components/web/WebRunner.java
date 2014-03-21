package com.eviware.loadui.components.web;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.components.web.api.RequestRunnerProvider;
import com.eviware.loadui.api.testevents.MessageLevel;
import com.eviware.loadui.api.testevents.TestEventManager;
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
	private TestEventManager testEventManager;
	private Runnable toRunOnRelease;

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

		context.setLayout( new WebRunnerLayout( webPageUrlProperty ) );
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
			log.debug( "WebRunner cannot accept the invalid URL: {}", url );
		}
		catch( IOException e )
		{
			log.debug( "An error occurred while scraping the provided URL: {}", url );
		}
	}

	private void notifyUser( String message )
	{
		if( testEventManager != null )
		{
			testEventManager.logMessage( MessageLevel.WARNING, message );
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
			throw new RuntimeException( "Cannot run, no URL set or URL is invalid" );
		requestRunner.run();
		return triggerMessage;
	}

	@Override
	protected int onCancel()
	{
		//TODO implement
		return 0;
	}

	@Override
	public void onRelease()
	{
		super.onRelease();
		if( toRunOnRelease != null )
		{
			toRunOnRelease.run();
		}
	}

	public void setTestEventManager( TestEventManager testEventManager )
	{
		this.testEventManager = testEventManager;
	}


	public void setOnRelease( Runnable onRelease )
	{
		this.toRunOnRelease = onRelease;
	}

}
