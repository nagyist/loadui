package com.eviware.loadui.launcher;

import javafx.application.Application;

public class FxAppStarter extends JavaFxStarter
{
	@Override
	protected Class<? extends Application> applicationClass()
	{
		return LoadUIFXLauncher.FXApplication.class;
	}
}
