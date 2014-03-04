package com.eviware.loadui.launcher.server;

import com.eviware.loadui.launcher.HeadlessFxLauncherBase;

public class LoadUIServerLauncher extends HeadlessFxLauncherBase
{


	public LoadUIServerLauncher( String[] args )
	{
		super( args );
	}

	@Override
	protected String commandLineServiceOsgiFilter()
	{
		return "(cliType=serverCli)";
	}

}
