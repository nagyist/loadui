package com.eviware.loadui.launcher;

import javafx.application.Application;

public class CommandLineStarter extends JavaFxStarter
{

	@Override
	protected Class<? extends Application> applicationClass()
	{
		return CommandLineApp.class;
	}

	public static class CommandLineApp extends HeadlessFxLauncherBase.HeadlessFxApp
	{

		@Override
		protected HeadlessFxLauncherBase createLauncher( String[] args )
		{
			return new LoadUICommandLineLauncher( args );
		}
	}

}
