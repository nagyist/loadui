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
package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.test.IntegrationTestUtils;
import com.google.code.tempusfugit.temporal.Condition;
import javafx.stage.Stage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;

/**
 * An loadUI Controller which can be used for testing.
 *
 * @author dain.nilsson
 */
public class ControllerFXWrapper
{
	private static final Logger log = LoggerFactory.getLogger( ControllerFXWrapper.class );

	static final File baseDir = new File( "target/controllerTest" );
	static final File bundleDir = new File( baseDir, "bundle" );
	static final File homeDir = new File( baseDir, ".loadui" );
	private final OSGiFXLauncher launcher;
	private final BundleContext context;

	public ControllerFXWrapper() throws Exception
	{
		if( baseDir.exists() && !IntegrationTestUtils.deleteRecursive( baseDir ) )
			throw new RuntimeException( "Test directory already exists and cannot be deleted! " + baseDir.getAbsolutePath() );

		log.info( "Test Basedir: " + baseDir.getAbsolutePath() );
		if( !baseDir.mkdir() )
			throw new RuntimeException( "Could not create test directory!" );

		if( !homeDir.mkdir() )
			throw new RuntimeException( "Could not create home directory!" );

		System.setProperty( LoadUI.LOADUI_HOME, homeDir.getAbsolutePath() );
		System.setProperty( LoadUI.LOADUI_WORKING, baseDir.getAbsolutePath() );

		copyRuntimeDirectories( baseDir );

		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				callLauncherMainMethod();
			}
		} ).start();

		waitOrTimeout( new Condition()
		{
			@Override
			public boolean isSatisfied()
			{
				return getLauncherInstance() != null;
			}

		}, timeout( seconds( 45 ) ) );

		launcher = getLauncherInstance();

		context = launcher.getBundleContext();
	}

	protected void callLauncherMainMethod()
	{
		OSGiFXLauncher.main( new String[] { "-nolock", "--nofx=false" } );
	}

	protected OSGiFXLauncher getLauncherInstance()
	{
		return OSGiFXLauncher.getInstance();
	}

	protected void copyRuntimeDirectories( File baseDir )
	{
		try
		{
			IntegrationTestUtils.copyDirectory( new File(
					"../loadui-installers/loadui-controller-installer/target/main" ), baseDir );
			IntegrationTestUtils.copyDirectory( new File( "target/bundle" ), bundleDir );
		}
		catch( IOException e1 )
		{
			throw new RuntimeException( e1 );
		}
	}

	public Stage getStage()
	{
		try
		{
			return OSGiFXLauncher.getStageFuture().get( 10, TimeUnit.SECONDS );
		}
		catch( Exception e )
		{
			throw new RuntimeException( "Could not get the Stage", e );
		}
	}

	public void stop() throws BundleException
	{
		try
		{
			launcher.stop();
		}
		finally
		{
			IntegrationTestUtils.deleteRecursive( baseDir );
		}
	}

	public BundleContext getBundleContext()
	{
		return context;
	}
}
