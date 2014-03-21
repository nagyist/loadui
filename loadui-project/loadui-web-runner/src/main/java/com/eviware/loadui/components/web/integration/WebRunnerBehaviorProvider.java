package com.eviware.loadui.components.web.integration;

import com.eviware.loadui.api.component.*;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.components.web.RequestRunnerProvider;
import com.eviware.loadui.components.web.WebRunner;
import com.eviware.loadui.util.html.HtmlAssetScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebRunnerBehaviorProvider implements BehaviorProvider
{
	private static final Logger log = LoggerFactory.getLogger( WebRunnerBehaviorProvider.class );

	private final List<WebRunner> runners = new ArrayList<>( 2 );

	@Override
	public ComponentBehavior createBehavior( ComponentDescriptor descriptor, ComponentContext context ) throws ComponentCreationException
	{
		return loadBehavior( descriptor.getType(), context );
	}

	@Override
	public ComponentBehavior loadBehavior( String componentType, ComponentContext context ) throws ComponentCreationException
	{
		final WebRunner webRunner = new WebRunner( context, HtmlAssetScraper.create(), new RequestRunnerProvider() );
		webRunner.setOnRelease( new Runnable()
		{
			public void run()
			{
				runners.remove( webRunner );
			}
		} );
		runners.add( webRunner );
		return webRunner;
	}

	public void bindEventManager( TestEventManager testEventManager, Map serviceProperties )
	{
		log.debug( "Binding testEventManager {}", testEventManager );
		for( WebRunner runner : runners )
		{
			runner.setTestEventManager( testEventManager );
		}
	}

	public void removeEventManager( TestEventManager testEventManager, Map serviceProperties )
	{
		log.debug( "Removing testEventManager {}", testEventManager );
		for( WebRunner runner : runners )
		{
			runner.setTestEventManager( null );
		}
	}

}
