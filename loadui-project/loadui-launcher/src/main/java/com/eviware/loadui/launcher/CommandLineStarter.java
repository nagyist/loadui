package com.eviware.loadui.launcher;

import javafx.application.Application;

public class CommandLineStarter extends JavaFxStarter
{

	@Override
	protected Class<? extends Application> applicationClass()
	{
		return LoadUICommandLineLauncher.CommandApplication.class;
	}

}
