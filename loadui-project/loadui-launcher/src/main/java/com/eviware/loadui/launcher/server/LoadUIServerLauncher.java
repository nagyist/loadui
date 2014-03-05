package com.eviware.loadui.launcher.server;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.launcher.JavaFxStarter;
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
		JavaFxStarter.addJavaFxOsgiExtraPackages( configProps );
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
