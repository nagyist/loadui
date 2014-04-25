package com.eviware.loadui.launcher.server;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.launcher.LoadUILauncher;

public class LoadUIServerLauncher extends LoadUILauncher
{


	public LoadUIServerLauncher( String[] args )
	{
		super( args );
	}

	@Override
	protected void processOsgiExtraPackages()
	{
		// server must not contain any JavaFX package so it can run in a clean Linux environment
	}

	@Override
	protected String commandLineServiceOsgiFilter()
	{
		return "(cliType=serverCli)";
	}

	public static void main( String[] args )
	{
		System.setProperty( LoadUI.INSTANCE, LoadUI.CONTROLLER );
		LoadUIServerLauncher launcher = new LoadUIServerLauncher( args );
		launcher.init();
		launcher.start();
	}

}
