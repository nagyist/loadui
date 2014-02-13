package com.eviware.loadui.launcher;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.launcher.api.GroovyCommand;
import com.eviware.loadui.launcher.impl.ResourceGroovyCommand;
import org.apache.commons.cli.CommandLine;

import java.util.HashMap;
import java.util.Map;

public class AgentLauncher extends LoadUILauncher
{

	public AgentLauncher( String[] args )
	{
		super( args );
	}

	@Override
	public void start()
	{
		super.start();

		Map<String, Object> attributes = new HashMap<>();

		attributes.put( "workspaceFile", null );
		attributes.put( "projectFile", null );
		attributes.put( "testCase", null );
		attributes.put( "localMode", true );
		attributes.put( "abort", "false" );

		publishService( GroovyCommand.class, new ResourceGroovyCommand( "/AgentRunTest.groovy", attributes ), null );
	}

	@Override
	protected void processOsgiExtraPackages()
	{
		//TODO see if we can stop this
		JavaFxStarter.addJavaFxOsgiExtraPackages( configProps );
	}

	@Override
	protected void processCommandLine( CommandLine cmdLine )
	{
		// agent requires no command line options
	}

	public static void main( String[] args )
	{
		setDefaultSystemProperty( LoadUI.INSTANCE, "agent" );
		AgentLauncher launcher = new AgentLauncher( args );
		launcher.init();
		launcher.start();
	}

}
