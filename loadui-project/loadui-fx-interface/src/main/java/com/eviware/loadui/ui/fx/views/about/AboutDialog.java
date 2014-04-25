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
package com.eviware.loadui.ui.fx.views.about;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.google.common.collect.ImmutableMap;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlurBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;

public class AboutDialog extends PopupControl
{
	@FXML
	private ImageView logo;

	@FXML
	private Label title;

	@FXML
	private Label buildVersion;

	@FXML
	private Label buildDate;

	private final Node owner;

	private static final Logger log = LoggerFactory.getLogger( AboutDialog.class );

	public AboutDialog( Node owner )
	{
		this.owner = owner;

		setAutoHide( true );

		//bridge.getChildren().setAll( this );

		FXMLLoader loader = new FXMLLoader( AboutDialog.class.getResource( AboutDialog.class.getSimpleName() + ".fxml" ) );
		loader.setClassLoader( FXMLUtils.class.getClassLoader() );
		loader.getNamespace().putAll(
				ImmutableMap.of( "name", System.getProperty( LoadUI.NAME ), "version", LoadUI.version(), "buildDate",
						System.getProperty( LoadUI.BUILD_DATE ), "buildVersion", System.getProperty( LoadUI.BUILD_NUMBER ) ) );
		loader.setController( this );

		try
		{
			bridge.getChildren().setAll( ( Parent )loader.load() );
		}
		catch( IOException exception )
		{
			throw new RuntimeException( exception );
		}

		Scene ownerScene = owner.getScene();
		Window parentWindow = ownerScene.getWindow();
		final double x = parentWindow.getX() + parentWindow.getWidth() / 2;
		final double y = parentWindow.getY() + parentWindow.getHeight() / 2;

		// Set a good estimated position before the dialog is shown to avoid flickering.
		setY( y - getScene().getRoot().prefHeight( -1 ) / 2 );
		setX( x - getScene().getRoot().prefWidth( -1 ) / 2 );

		addEventHandler( WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>()
		{
			@Override
			public void handle( WindowEvent arg0 )
			{
				blurParentWindow();

				setX( x - getWidth() / 2 );
				setY( y - getHeight() / 2 );
			}
		} );

		try
		{
			logo.setImage( new Image( LoadUI.relativeFile( "res/about-logo.png" ).toURI().toURL()
					.toExternalForm() ) );
		}
		catch( MalformedURLException e )
		{
			log.error( "Failed to load about-logo", e );
		}

		title.setText( String.format( "%s Version %s", System.getProperty( LoadUI.NAME, "LoadUI" ), LoadUI.version() ) );
		buildVersion
				.setText( String.format( "Build version: %s", System.getProperty( LoadUI.BUILD_NUMBER, "[internal]" ) ) );
		buildDate.setText( String.format( "Build version: %s", System.getProperty( LoadUI.BUILD_DATE, "unknown" ) ) );
	}

	private void blurParentWindow()
	{
		final Parent root = owner.getScene().getRoot();
		final Effect effect = root.getEffect();
		root.setEffect( GaussianBlurBuilder.create().radius( 8 ).build() );
		setOnHidden( new EventHandler<WindowEvent>()
		{
			@Override
			public void handle( WindowEvent arg0 )
			{
				root.setEffect( effect );
			}
		} );
	}

	public void loaduiSite()
	{
		UIUtils.openInExternalBrowser( "http://www.loadui.org" );
	}

	public void smartbearSite()
	{
		UIUtils.openInExternalBrowser( "http://www.smartbear.com" );
	}

}
