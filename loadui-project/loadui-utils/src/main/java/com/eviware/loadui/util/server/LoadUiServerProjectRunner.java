package com.eviware.loadui.util.server;

import com.eviware.loadui.api.command.GroovyCommand;
import com.eviware.loadui.util.command.ResourceGroovyCommand;
import com.google.common.base.Preconditions;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class LoadUiServerProjectRunner
{
	Logger log = LoggerFactory.getLogger( LoadUiServerProjectRunner.class );

	private final BundleContext context;

	public LoadUiServerProjectRunner( BundleContext context )
	{
		this.context = context;
	}

	public void runProjectAsController( Map<String, Object> attributes, boolean exitAfterRun )
	{
		Object projectName = attributes.get( "projectFile" );
		Preconditions.checkNotNull( projectName, "Project file not specified" );
		Preconditions.checkArgument( !projectName.toString().isEmpty(), "Project file cannot be empty" );
		runProjectAsController( Paths.get( projectName.toString() ), attributes, exitAfterRun );
	}

	public void runProjectAsController( Path projectPath, Map<String, Object> attributes, boolean exitAfterRun )
	{
		runProject( projectPath, attributes, exitAfterRun, "/RunTest.groovy" );
	}

	public void runProjectAsAgent( Path projectPath, Map<String, Object> attributes, boolean exitAfterRun )
	{
		runProject( projectPath, attributes, exitAfterRun, "/AgentRunTest.groovy" );
	}

	private void runProject( Path projectPath, Map<String, Object> attributes, boolean exitAfterRun, String script )
	{
		try
		{
			log.info( "Will start project as soon as the project file has been written" );
			Thread.sleep( 1_000 );
		}
		catch( InterruptedException e )
		{
			log.warn( "Interrupted while waiting for project file to be written" );
		}

		log.info( "Starting project " + projectPath );
		attributes.put( "projectFile", projectPath.toFile() );
		context.registerService( GroovyCommand.class.getName(), createCommand( script, attributes, exitAfterRun ), null );
	}

	private GroovyCommand createCommand( String runScript, Map<String, Object> attributes, boolean exitAfterRun )
	{
		ResourceGroovyCommand command = new ResourceGroovyCommand( runScript, attributes );
		command.setExit( exitAfterRun );
		return command;
	}

}
