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
package com.eviware.loadui.ui.fx.util;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.api.ImageResolver;
import com.eviware.loadui.ui.fx.views.window.Overlay;
import com.eviware.loadui.ui.fx.views.window.OverlayHolder;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Preconditions;
import com.sun.javafx.PlatformUtil;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class UIUtils
{
	protected static final Logger log = LoggerFactory.getLogger( UIUtils.class );

	private final static String TOOLBOX_IMAGES_PATH = "/com/eviware/loadui/ui/fx/toolboxIcons/";

	public static final ExtensionFilter XML_EXTENSION_FILTER = new FileChooser.ExtensionFilter( "loadUI project file",
			"*.xml" );

	private static List<ImageResolver> imageResolvers;

	// Used by the OSGi Framework to set imageResolver services. DO NOT REMOVE!
	@SuppressWarnings( "unused" )
	public void setImageResolvers( List<ImageResolver> imageResolvers )
	{
		UIUtils.imageResolvers = imageResolvers;
	}

	public static Window getParentWindow( Window window )
	{
		if( window instanceof PopupWindow )
		{
			window = ( ( PopupWindow )window ).getOwnerWindow();
		}
		else if( window instanceof Stage )
		{
			window = ( ( Stage )window ).getOwner();
		}
		else
		{
			window = null;
		}
		return window;
	}

	public static Overlay getOverlayFor( Scene scene )
	{
		Preconditions.checkNotNull( scene );
		if( scene.getRoot() instanceof OverlayHolder )
			return ( ( OverlayHolder )scene.getRoot() ).getOverlay();
		else
			return new Overlay.NoOpOverlay();
	}

	@Nonnull
	public static Image getImageFor( Object object )
	{
		try
		{
			return doGetImage( object );
		}
		catch( Exception e )
		{
			log.warn( "Could not get image for " + object, e );
			return getDefaultComponentImage();
		}
	}

	public static Image getDefaultComponentImage() {
		return new Image( root( "default-component-icon.png" ) );
	}

	private static Image doGetImage( Object object )
	{
		for( ImageResolver resolver : imageResolvers )
		{
			Image image = resolver.getImageFor( object );
			if( image != null )
				return image;
		}

		if( object instanceof AgentItem || AgentItem.class.equals( object ) )
		{
			return new Image( root( "agent-icon.png" ) );
		}
		else if( object instanceof ProjectItem || ProjectItem.class.equals( object ) )
		{
			return new Image( root( "project-icon.png" ) );
		}
		else if( object instanceof SceneItem || SceneItem.class.equals( object ) )
		{
			return new Image( root( "testcase-icon.png" ) );
		}
		else if( object instanceof ComponentItem )
		{
			return new Image( BeanInjector.getBean( ComponentRegistry.class )
					.findDescriptor( ( ( ComponentItem )object ).getType() ).getIcon().toString() );
		}
		else if( object instanceof AssertionItem )
		{
			return new Image( root( "assertion_icon_toolbar.png" ) );
		}

		throw new RuntimeException( "No image found for resource "
				+ ( object instanceof Class ? object : "of class " + object.getClass().getName() ) );
	}

	private static String root( String fileName )
	{
		return UIUtils.class.getResource( TOOLBOX_IMAGES_PATH + fileName ).toExternalForm();
	}

	public static BufferedImage scaleImage( BufferedImage image, int maxWidth, int maxHeight )
	{
		double imageWidth = image.getWidth();
		double imageHeight = image.getHeight();
		double scale = Math.min( maxWidth / imageWidth, maxHeight / imageHeight );

		int width = ( int )( imageWidth * scale );
		int height = ( int )( imageHeight * scale );

		BufferedImage scaledImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );

		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
		graphics2D.drawImage( image, 0, 0, width, height, null );
		graphics2D.dispose();

		return scaledImage;
	}

	public static void openInExternalBrowser( final String url )
	{
		if( !PlatformUtil.isMac() )
		{
			try
			{
				Desktop.getDesktop().browse( new java.net.URI( url ) );
			}
			catch( IOException | URISyntaxException e )
			{
				log.error( "Unable to launch browser with url in external browser!", e );
			}
			return;
		}

		try
		{
			Thread t = new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						Runtime.getRuntime().exec( "open " + url );
					}
					catch( IOException e )
					{
						log.error( "Unable to fork native browser with url in external browser!", e );
					}
				}
			} );
			t.start();
		}
		catch( Exception e )
		{
			log.error( "unable to display url!", e );
		}
	}

}
