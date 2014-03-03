package com.eviware.loadui.launcher;

import com.eviware.loadui.LoadUI;

public class AgentLauncher extends LoadUILauncher
{

	public AgentLauncher( String[] args )
	{
		super( args );
	}

	@Override
	protected void processOsgiExtraPackages()
	{
		//TODO see if we can stop this
		JavaFxStarter.addJavaFxOsgiExtraPackages( configProps );
	}

	@Override
	protected String commandLineServiceOsgiFilter()
	{
		return null;
	}

	public static void main( String[] args )
	{
		setDefaultSystemProperty( LoadUI.INSTANCE, "agent" );
		AgentLauncher launcher = new AgentLauncher( args );
		launcher.init();
		launcher.start();
	}

}
