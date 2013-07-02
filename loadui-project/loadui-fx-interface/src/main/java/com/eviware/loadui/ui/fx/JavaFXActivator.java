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

import javafx.application.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class JavaFXActivator implements BundleActivator
{
	@Override
	public void start( final BundleContext context ) throws Exception
	{
		System.out.println("JavaFX2 bundle started!");

		final ClassLoader bundleClassLoader = JavaFXActivator.class.getClassLoader();

		initJavaFXThreadWith(bundleClassLoader);

	}

	private void initJavaFXThreadWith( final ClassLoader bundleClassLoader )
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{

				Thread.currentThread().setContextClassLoader(bundleClassLoader);

			}
		});
	}

	@Override
	public void stop( BundleContext context ) throws Exception
	{
		System.out.println("JavaFX2 bundle stopped!");
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep(6000);
				} catch( InterruptedException e )
				{
					e.printStackTrace();
				}

				System.out.println("Shutdown timed out, forcing close...");
				System.exit(0);
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
}
