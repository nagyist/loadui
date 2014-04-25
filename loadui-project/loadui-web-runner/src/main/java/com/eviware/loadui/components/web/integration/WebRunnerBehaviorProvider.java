package com.eviware.loadui.components.web.integration;

import com.eviware.loadui.api.component.*;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.components.web.DefaultRequestRunnerProvider;
import com.eviware.loadui.components.web.WebRunner;
import com.eviware.loadui.util.html.HtmlAssetScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebRunnerBehaviorProvider implements BehaviorProvider, TestExecutionTask
{
	private static final Logger log = LoggerFactory.getLogger( WebRunnerBehaviorProvider.class );

	private final List<WebRunner> runners = new ArrayList<>( 2 );

	public WebRunnerBehaviorProvider( TestRunner testRunner )
	{
		testRunner.registerTask( this, Phase.PRE_START, Phase.STOP );
	}

	@Override
	public ComponentBehavior createBehavior( ComponentDescriptor descriptor, ComponentContext context ) throws ComponentCreationException
	{
		return loadBehavior( descriptor.getType(), context );
	}

	@Override
	public ComponentBehavior loadBehavior( String componentType, ComponentContext context ) throws ComponentCreationException
	{
		final WebRunner webRunner = new WebRunner( context, HtmlAssetScraper.create(), new DefaultRequestRunnerProvider() );
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

	@Override
	public void invoke( TestExecution execution, Phase phase )
	{
		boolean testRunning = ( phase == Phase.PRE_START );
		for( WebRunner runner : runners )
		{
			runner.setLoadTestRunning( testRunning );
		}
	}

}
