package com.eviware.loadui.components.web.integration;

import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.components.web.WebRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class WebRunnerActivator
{
	static final Logger log = LoggerFactory.getLogger( WebRunnerActivator.class );

	private final ComponentRegistry registry;
	private final WebRunnerBehaviorProvider behaviorProvider;

	public WebRunnerActivator( ComponentRegistry registry, WebRunnerBehaviorProvider behaviorProvider )
	{
		this.registry = registry;
		this.behaviorProvider = behaviorProvider;
	}

	public void start() throws URISyntaxException
	{
		URI webRunnerIcon = getClass().getResource( "/images/WebRunner.png" ).toURI();

		log.debug( "Activating the Web Runner Bundle" );
		ComponentDescriptor webRunnerDescriptor = new ComponentDescriptor(
				WebRunner.class.getName(),
				RunnerCategory.CATEGORY,
				"Web Runner",
				"A runner for Web requests",
				webRunnerIcon
		);

		registry.registerDescriptor( webRunnerDescriptor, behaviorProvider );
	}

	public void stop()
	{
		log.debug( "Stopping the Web Runner Bundle" );
		registry.unregisterProvider( behaviorProvider );
	}

}
