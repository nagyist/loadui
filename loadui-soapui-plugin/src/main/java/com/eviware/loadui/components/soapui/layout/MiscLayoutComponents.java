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
package com.eviware.loadui.components.soapui.layout;

import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.FilePickerDialog;
import com.eviware.loadui.util.StringUtils;
import com.eviware.loadui.util.soapui.CajoClient;
import com.google.common.collect.ImmutableMap;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MiscLayoutComponents
{
	private static final Logger log = LoggerFactory.getLogger( MiscLayoutComponents.class );

	public static LayoutComponentImpl buildOpenInSoapUiButton( final String projectFileName,
																				  final String testSuiteName,
																				  final String testCaseName )
	{
		final Button openInSoapUIButton = ButtonBuilder.create()
				.text( "Open in SoapUI" )
				.build();

		final Runnable action = new Runnable()
		{
			public void run()
			{
				try
				{
					final CajoClient cajo = CajoClient.getInstance();
					File soapUIExecutable = new File( cajo.getPathToSoapUIBat() );

					if( StringUtils.isNullOrEmpty( soapUIExecutable.getAbsolutePath() )
							|| !soapUIExecutable.exists()
							|| !soapUIExecutable.canExecute()
							)
					{
						log.warn( "no or invalid path to SoapUI has been set!" );
						showFilePickerDialog( openInSoapUIButton,
								"You are missing the path to the SoapUI executable",
								"Select a SoapUI executable",
								FilePickerDialog.getExtensionFilterForSoapUIExecutableByPlatform() );
					}
					else
					{
						cajo.openTestCase( projectFileName, testSuiteName, testCaseName );
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		};

		openInSoapUIButton.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent actionEvent )
			{
				openInSoapUIButton.fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, action ) );
			}
		} );

		return new LayoutComponentImpl( ImmutableMap.<String, Object>builder()
				.put( "component", openInSoapUIButton )
				.build() );
	}

	private static void showFilePickerDialog( final Button button, final String title, final String filePickerTitle, final FileChooser.ExtensionFilter extensionFilter )
	{
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				final FilePickerDialog confirm = new FilePickerDialog( button,
						title,
						filePickerTitle,
						extensionFilter );

				confirm.show();

				confirm.setOnConfirm( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent actionEvent )
					{
						CajoClient cajo = CajoClient.getInstance();
						confirm.hide();

						String newPath = confirm.SelectedFileProperty().get().getAbsolutePath();
						cajo.setPathToSoapUIBat( newPath );

						button.fire();
					}
				} );
			}
		} );
	}
}
