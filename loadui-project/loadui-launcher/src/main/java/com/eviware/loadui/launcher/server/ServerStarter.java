package com.eviware.loadui.launcher.server;

import com.eviware.loadui.launcher.HeadlessFxLauncherBase;
import com.eviware.loadui.launcher.JavaFxStarter;
import javafx.application.Application;

public class ServerStarter extends JavaFxStarter
{
	@Override
	protected Class<? extends Application> applicationClass()
	{
		return LoadUiServerApp.class;
	}

	public static class LoadUiServerApp extends HeadlessFxLauncherBase.HeadlessFxApp
	{

		@Override
		protected HeadlessFxLauncherBase createLauncher( String[] args )
		{
			return new LoadUiServerLauncher( args );
		}
	}
}
