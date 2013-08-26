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

import com.eviware.loadui.api.ui.dialog.FilePickerDialog;
import com.eviware.loadui.api.ui.dialog.FilePickerDialogFactory;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.StringUtils;
import com.eviware.loadui.util.soapui.CajoClient;
import com.google.common.collect.ImmutableMap;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MiscLayoutComponents
{
	private static final Logger log = LoggerFactory.getLogger( MiscLayoutComponents.class );

	public LayoutComponentImpl buildOpenInSoapUiButton( final String projectFileName,
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
					String path = cajo.getPathToSoapUIBat();

					File soapUIExecutable;
					if( StringUtils.isNullOrEmpty( path ) )
					{
						soapUIExecutable = new File( "" );
					}
					else
					{
						soapUIExecutable = new File( path );
					}

					if( StringUtils.isNullOrEmpty( soapUIExecutable.getAbsolutePath() )
							|| !soapUIExecutable.exists()
							|| !soapUIExecutable.canExecute() )
					{
						log.warn( "no or an invalid path to SoapUI has been set!" );
						showFilePickerDialog( openInSoapUIButton,
								"You are missing the path to the SoapUI executable",
								"Select a SoapUI executable",
								FilePickerDialog.ExtensionFilter.SOAPUI_EXECUTABLE );
					}
					else
					{
						log.info( "Opening TestCase In SoapUI" );
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
				new Thread( action ).start();
			}
		} );

		return new LayoutComponentImpl( ImmutableMap.<String, Object>builder()
				.put( "component", openInSoapUIButton )
				.build() );
	}

	private void showFilePickerDialog( final Button button, final String title, final String filePickerTitle, FilePickerDialog.ExtensionFilter soapuiExecutable )
	{
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				FilePickerDialogFactory filePickerDialogFactory = BeanInjector.getBean( FilePickerDialogFactory.class );

				final FilePickerDialog confirm = filePickerDialogFactory.createDialog( button.getText(), title, filePickerTitle, FilePickerDialog.ExtensionFilter.SOAPUI_EXECUTABLE );

				Runnable callback = new Runnable()
				{
					public void run()
					{
						CajoClient cajo = CajoClient.getInstance();

						String newPath = confirm.getFile().getAbsolutePath();
						cajo.setPathToSoapUIBat( newPath );

						button.fire();
					}
				};

				confirm.show();

				confirm.setOnConfirm( callback );
			}
		} );
	}
}
