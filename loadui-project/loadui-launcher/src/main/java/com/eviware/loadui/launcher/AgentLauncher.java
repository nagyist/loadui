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
		// as of 2014/03/12 the only module still requiring JavaFX packages in Agents is the soapui-plugin
		JavaFxStarter.addJavaFxOsgiExtraPackages( configProps );
	}

	@Override
	protected String commandLineServiceOsgiFilter()
	{
		return null;
	}

	public static void main( String[] args )
	{
		setDefaultSystemProperty( LoadUI.INSTANCE, LoadUI.AGENT );
		AgentLauncher launcher = new AgentLauncher( args );
		launcher.init();
		launcher.start();
	}

}
