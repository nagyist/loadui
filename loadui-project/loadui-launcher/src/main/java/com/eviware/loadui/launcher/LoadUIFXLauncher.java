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
package com.eviware.loadui.launcher;

import com.eviware.loadui.LoadUI;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.LabelBuilder;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.*;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;

public class LoadUIFXLauncher extends LoadUILauncher
{
	private final static Logger log = Logger.getLogger( LoadUIFXLauncher.class.getName() );

	public static void main( String[] args )
	{
		Application.launch( FXApplication.class, args );
	}

	public LoadUIFXLauncher( String[] args )
	{
		super( args );
	}

	@Override
	protected void processCommandLine( CommandLine cmdLine )
	{

	}

	public static class FXApplication extends Application
	{
		private LoadUILauncher launcher;

		@Override
		public void start( final Stage stage ) throws Exception
		{

			log.info( "Starting FXApplication, LoadUI Version: " + LoadUI.version() );
			File workingDir = LoadUI.getWorkingDir();
			log.info( "LoadUI Working directory: " + workingDir );
			Scene splashScene;

			final String noFx = getParameters().getNamed().get( NOFX_OPTION );
			final String agent = getParameters().getNamed().get( "agent" );

			if( "true".equals( agent ) )
				setDefaultSystemProperty( "loadui.instance", "agent" );

			if( "false".equals( noFx ) )
			{
				try
				{
					splashScene = FXMLLoader.load( new File( workingDir, "res/loadui-splash.fxml" ).toURI().toURL() );
				}
				catch( IOException e )
				{
					splashScene = SceneBuilder.create().width( 600 ).height( 320 ).fill( Color.DARKGRAY )
							.root( LabelBuilder.create().text( System.getProperty( LOADUI_NAME, "loadUI" ) ).build() ).build();
				}

				Image[] icons = new Image[0];
				try
				{
					icons = new Image[] {
							new Image( new File( workingDir, "res/icon_64x64.png" ).toURI().toURL().toString() ),
							new Image( new File( workingDir, "res/icon_32x32.png" ).toURI().toURL().toString() ) };
				}
				catch( Exception e )
				{
					log.warning( e.toString() );
				}

				setDefaultSystemProperty( "loadui.headless", "false" );
				final Stage splash = StageBuilder.create().style( StageStyle.TRANSPARENT ).scene( splashScene )
						.icons( icons ).build();
				splash.initModality( Modality.APPLICATION_MODAL );
				splash.centerOnScreen();
				splash.show();
				splash.toFront();

				stage.getIcons().addAll( icons );
				stage.setOnShown( new EventHandler<WindowEvent>()
				{
					@Override
					public void handle( WindowEvent event )
					{
						splash.close();
					}
				} );
			}

			Task<Void> task = new Task<Void>()
			{
				@Override
				protected Void call() throws Exception
				{
					System.setSecurityManager( null );

					launcher = createLauncher( getParameters().getRaw().toArray( new String[0] ) );
					launcher.init();
					launcher.start();

					if( "false".equals( noFx ) )
					{
						launcher.publishService( Stage.class, stage,
								new Hashtable<String, Object>() );
					}
					return null;
				}
			};

			new Thread( task ).start();
		}

		protected LoadUILauncher createLauncher( String[] args )
		{
			return new LoadUIFXLauncher( args );
		}

		@Override
		public void stop() throws Exception
		{
			launcher.stop();
		}
	}
}
