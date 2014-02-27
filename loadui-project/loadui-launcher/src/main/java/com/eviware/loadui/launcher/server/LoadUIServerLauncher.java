package com.eviware.loadui.launcher.server;

import com.eviware.loadui.launcher.HeadlessFxLauncherBase;

public class LoadUiServerLauncher extends HeadlessFxLauncherBase
{


	public LoadUiServerLauncher( String[] args )
	{
		super( args );
	}

	@Override
	protected String commandLineServiceOsgiFilter()
	{
		return "(cliType=serverCli)";
	}

}
