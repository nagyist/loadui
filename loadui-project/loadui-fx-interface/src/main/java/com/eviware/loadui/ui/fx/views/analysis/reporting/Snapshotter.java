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
package com.eviware.loadui.ui.fx.views.analysis.reporting;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;

public class Snapshotter
{
	private WritableImage writableImage;
	private final Parent rootNode;
	private final Node node;
	private CountDownLatch latch;

	public Snapshotter( @Nonnull final Parent rootNode, @Nonnull final Node node )
	{
		this.rootNode = rootNode;
		this.node = node;
	}

	public void setWritableImage( WritableImage writableImage )
	{
		this.writableImage = writableImage;
	}

	public BufferedImage createSnapshot()
	{
		latch = new CountDownLatch( 1 );

		if( !Platform.isFxApplicationThread() )
		{
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					doSnapshot();
				}
			} );
		}
		else
		{
			doSnapshot();
		}

		try
		{
			latch.await();
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}

		return SwingFXUtils.fromFXImage( writableImage, null );
	}

	private void doSnapshot()
	{
		new Scene( rootNode );
		setWritableImage( node.snapshot( null, null ) );
		latch.countDown();
	}
}
