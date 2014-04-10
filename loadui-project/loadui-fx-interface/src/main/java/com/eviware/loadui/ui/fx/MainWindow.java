/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.ui.fx.api.LoaduiFXConstants;
import com.eviware.loadui.ui.fx.api.intent.AbortableBlockingTask;
import com.eviware.loadui.ui.fx.api.intent.BlockingTask;
import com.eviware.loadui.ui.fx.api.intent.DeleteTask;
import com.eviware.loadui.ui.fx.views.analysis.FxExecutionsInfo;
import com.eviware.loadui.ui.fx.views.window.MainWindowView;
import com.eviware.loadui.ui.fx.views.workspace.GettingStartedDialog;
import com.eviware.loadui.ui.fx.views.workspace.NewVersionDialog;
import com.eviware.loadui.util.ShutdownWatchdog;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static com.eviware.loadui.ui.fx.views.workspace.GettingStartedDialog.SHOW_GETTING_STARTED;
import static com.eviware.loadui.util.NewVersionChecker.VersionInfo;
import static com.eviware.loadui.util.NewVersionChecker.checkForNewVersion;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MainWindow
{
	private static final String FULLSCREEN = MainWindow.class.getName() + "@fullscreen"; // This is the OS X definition of fullscreen.
	private static final String WINDOW_WIDTH = MainWindow.class.getName() + "@width";
	private static final String WINDOW_HEIGHT = MainWindow.class.getName() + "@height";

	private Stage stage;
	private TestEventManager tem;
	private final WorkspaceProvider workspaceProvider;
	private FxExecutionsInfo executionsInfo;

	public MainWindow( final WorkspaceProvider workspaceProvider )
	{
		this.workspaceProvider = workspaceProvider;
	}

	public MainWindow withStage( Stage stage )
	{
		if( this.stage != null )
			throw new IllegalStateException( "Stage has already been set" );
		this.stage = stage;
		stage.addEventHandler( WindowEvent.WINDOW_HIDING, new EventHandler<WindowEvent>()
		{
			@Override
			public void handle( WindowEvent event )
			{
				saveDimensions();
				ShutdownWatchdog.killJvmIn( 6, SECONDS );
			}
		} );

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				loadDimensions();
			}
		} );
		return this;
	}

	public MainWindow withTestEventManager( TestEventManager tem )
	{
		if( this.tem != null )
			throw new IllegalStateException( "TestEventManager has already been set" );
		this.tem = tem;
		return this;
	}

	public MainWindow provideInfoFor( FxExecutionsInfo executionsInfo )
	{
		this.executionsInfo = executionsInfo;
		return this;
	}

	public void show()
	{
		if( stage == null || tem == null )
			throw new IllegalStateException( "Stage or TestEventManager have not been set" );

		stage.setTitle( System.getProperty( LoadUI.NAME, "LoadUI" ) + " " + LoadUI.version() );

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				final MainWindowView mainView = new MainWindowView( workspaceProvider, executionsInfo, tem );

				stage.setScene( SceneBuilder.create().stylesheets( LoaduiFXConstants.getLoaduiStylesheets() )
						.root( mainView ).build() );

				installOnStartupPopupDialogs();

				stage.show();

				BlockingTask.install( stage.getScene() );
				AbortableBlockingTask.install( stage.getScene() );
				DeleteTask.install( stage.getScene() );
			}


		} );
	}

	private void installOnStartupPopupDialogs()
	{
		stage.showingProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable observable )
			{
				final WorkspaceItem workspace = workspaceProvider.getWorkspace();
				final Node rootNode = stage.getScene().getRoot();

				final Task<VersionInfo> checkVersionTask = new Task<VersionInfo>(){

					private VersionInfo newVersion;

					@Override
					protected VersionInfo call() throws Exception
					{
						newVersion = checkForNewVersion( workspace );
						return newVersion;
					}

					@Override
					protected void succeeded()
					{
						super.succeeded();

						if( newVersion != null )
							new NewVersionDialog( rootNode, newVersion).show();
						else if ( "true".equals( workspace.getAttribute( SHOW_GETTING_STARTED, "true" ) ) )
							new GettingStartedDialog( workspace, rootNode ).show();
					}
				};

				new Thread(checkVersionTask).start();



				stage.showingProperty().removeListener( this );
			}
		} );
	}

	private void loadDimensions()
	{
		if( !workspaceProvider.isWorkspaceLoaded() )
		{
			workspaceProvider.loadDefaultWorkspace();
		}

		WorkspaceItem workspace = workspaceProvider.getWorkspace();
		if( Boolean.valueOf( workspace.getAttribute( FULLSCREEN, "false" ) ) )
		{
			stage.setFullScreen( true );
		}
		else
		{
			stage.setWidth( Double.parseDouble( workspace.getAttribute( WINDOW_WIDTH, "1200" ) ) );
			stage.setHeight( Double.parseDouble( workspace.getAttribute( WINDOW_HEIGHT, "800" ) ) );
		}
	}

	private void saveDimensions()
	{
		WorkspaceItem workspace = workspaceProvider.getWorkspace();
		workspace.setAttribute( FULLSCREEN, String.valueOf( stage.isFullScreen() ) );
		workspace.setAttribute( WINDOW_WIDTH, String.valueOf( stage.getWidth() ) );
		workspace.setAttribute( WINDOW_HEIGHT, String.valueOf( stage.getHeight() ) );
		workspace.save();
	}
}
