package com.eviware.loadui.components.rest.integration;

import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.components.rest.RestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class RestRunnerActivator
{
	private Logger log = LoggerFactory.getLogger( RestRunnerActivator.class );

	private final ComponentRegistry registry;
	private final RestBehaviorProvider behaviorProvider;

	public RestRunnerActivator( ComponentRegistry registry, RestBehaviorProvider behaviorProvider )
	{
		this.registry = registry;
		this.behaviorProvider = behaviorProvider;
	}

	public void start() throws URISyntaxException
	{
		URI restIcon = getClass().getResource( "/images/restRunner.png" ).toURI();

		log.debug( "Activating the DejaClick Bundle" );
		ComponentDescriptor restDescriptor = new ComponentDescriptor(
				RestRunner.class.getName(),
				RunnerCategory.CATEGORY,
				"REST Runner",
				"A runner for REST requests",
				restIcon
		);

		registry.registerDescriptor( restDescriptor, behaviorProvider );
	}

	public void stop()
	{
		log.debug( "Stopping the REST Bundle" );
		registry.unregisterProvider( behaviorProvider );
	}

}
