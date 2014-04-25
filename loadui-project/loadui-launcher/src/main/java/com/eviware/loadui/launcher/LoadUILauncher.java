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
import com.eviware.loadui.api.cli.CommandLineParser;
import com.eviware.loadui.launcher.util.BndUtils;
import com.eviware.loadui.launcher.util.ErrorHandler;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.apache.felix.main.Main;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;

import java.io.*;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Dictionary;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Starts an embedded OSGi Runtime (Felix) with all the required JavaFX packages
 * exposed, enabling JavaFX bundles to run.
 *
 * @author dain.nilsson
 */
public abstract class LoadUILauncher
{
	protected static final String LOADUI_HOME = "loadui.home";
	protected static final String LOADUI_NAME = "loadui.name";
	protected static final String LOADUI_BUILD_DATE = "loadui.build.date";
	protected static final String LOADUI_BUILD_NUMBER = "loadui.build.number";

	public static final String ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA = "org.osgi.framework.system.packages.extra";
	protected static final String NOFX_OPTION = "nofx";
	protected static final String SYSTEM_PROPERTY_OPTION = "D";
	protected static final String HELP_OPTION = "h";
	protected static final String IGNORE_CURRENTLY_RUNNING_OPTION = "nolock";

	private final static Logger log = Logger.getLogger( LoadUILauncher.class.getName() );

	protected Framework framework;
	protected final Properties configProps;
	protected final String[] argv;

	/**
	 * Initiates and starts the OSGi runtime.
	 */
	public LoadUILauncher( String[] args )
	{
		argv = args;

		setLoadUiWorkingDirectory();

		//Fix for Protection!
		//FIXME this fix is probably not needed after the license-manager was created,
		// this is also present in the license manager ticket to remove this: LOADUI-1029
		String username = System.getProperty( "user.name" );
		System.setProperty( "user.name.original", username );
		System.setProperty( "user.name", username.toLowerCase() );
		File buildInfoFile = new File( LoadUI.getWorkingDir(), "res/buildinfo.txt" );

		applyJava6sslIssueWorkaround();

		if( buildInfoFile.exists() )
		{
			try(InputStream is = new FileInputStream( buildInfoFile ))
			{
				Properties buildinfo = new Properties();
				buildinfo.load( is );
				System.setProperty( LOADUI_BUILD_NUMBER, buildinfo.getProperty( "build.number" ) );
				System.setProperty( LOADUI_BUILD_DATE, buildinfo.getProperty( "build.date" ) );
				System.setProperty( LOADUI_NAME, buildinfo.getProperty( LOADUI_NAME, "loadUI" ) );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.setProperty( LOADUI_BUILD_NUMBER, "unknown" );
			System.setProperty( LOADUI_BUILD_DATE, "unknown" );
			System.setProperty( LOADUI_NAME, "loadUI" );
		}


		initSystemProperties();

		String sysOutFilePath = System.getProperty( "system.out.file" );
		if( sysOutFilePath != null )
		{
			File sysOutFile = new File( sysOutFilePath );
			if( !sysOutFile.exists() )
			{
				try
				{
					sysOutFile.createNewFile();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}

			try
			{
				System.err.println( "Writing stdout and stderr to file:" + sysOutFile.getAbsolutePath() );

				final PrintStream outStream = new PrintStream( sysOutFile );
				System.setOut( outStream );
				System.setErr( outStream );

				Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
				{
					@Override
					public void run()
					{
						outStream.close();
					}
				} ) );
			}
			catch( FileNotFoundException e )
			{
				throw new RuntimeException( e );
			}
		}

		System.out.println( "Launching " + System.getProperty( LOADUI_NAME ) + " Build: "
				+ System.getProperty( LOADUI_BUILD_NUMBER, "[internal]" ) + " "
				+ System.getProperty( LOADUI_BUILD_DATE, "" ) );
		Main.loadSystemProperties();
		configProps = Main.loadConfigProperties();
		if( configProps == null )
		{
			System.err.println( "There was an error loading the OSGi configuration!" );
			exitInError();
		}
		Main.copySystemProperties( configProps );
	}

	private void setLoadUiWorkingDirectory()
	{
		String workingDir = System.getenv( "LOADUI_WORKING" );
		if( workingDir != null )
		{
			setDefaultSystemProperty( LoadUI.LOADUI_WORKING, workingDir );
		}
		else
		{
			setDefaultSystemProperty( LoadUI.LOADUI_WORKING, new File( "" ).getAbsolutePath() );
		}

		System.out.println( "LoadUI working directory: " + LoadUI.getWorkingDir() );
	}

	private void applyJava6sslIssueWorkaround()
	{
		//Workaround for some versions of Java 6 which have a known SSL issue
		String versionString = System.getProperty( "java.version", "0.0.0_00" );
		try
		{
			if( versionString.startsWith( "1.6" ) && versionString.contains( "_" ) )
			{
				int updateVersion = Integer.parseInt( versionString.split( "_", 2 )[1] );
				if( updateVersion > 27 )
				{
					log.info( "Detected Java version " + versionString + ", disabling CBC Protection." );
					System.setProperty( "jsse.enableCBCProtection", "false" );
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	private boolean hasCommandLineOption( String option )
	{
		for( String arg : argv )
		{
			if( arg.contains( option ) )
				return true;
		}
		return false;
	}

	public void init()
	{
		if( !hasCommandLineOption( IGNORE_CURRENTLY_RUNNING_OPTION ) )
		{
			ensureNoOtherInstance();
		}

		processOsgiExtraPackages();

		framework = new FrameworkFactory().newFramework( configProps );

		try
		{
			framework.init();
			AutoProcessor.process( configProps, framework.getBundleContext() );
			beforeBundlesStart( framework.getBundleContext().getBundles() );
			loadExternalJarsAsBundles();
		}
		catch( BundleException ex )
		{
			ex.printStackTrace();
		}
	}


	private void loadExternalJarsAsBundles()
	{
		File source = new File( LoadUI.getWorkingDir(), "ext" );
		if( source.isDirectory() )
		{
			for( File ext : source.listFiles( new FilenameFilter()
			{
				@Override
				public boolean accept( File dir, String name )
				{
					return name.toLowerCase().endsWith( ".jar" );
				}
			} ) )
			{
				try
				{
					File tmpFile = File.createTempFile( ext.getName(), ".jar" );
					BndUtils.wrap( ext, tmpFile );
					framework.getBundleContext().installBundle( tmpFile.toURI().toString() ).start();
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
	}

	private void ensureNoOtherInstance()
	{
		try
		{
			File bundleCache = new File( configProps.getProperty( "org.osgi.framework.storage" ) );
			if( !bundleCache.isDirectory() )
				if( !bundleCache.mkdirs() )
					throw new RuntimeException( "Unable to create directory: " + bundleCache.getAbsolutePath() );

			File lockFile = new File( bundleCache, "loadui.lock" );
			if( !lockFile.exists() )
				if( !lockFile.createNewFile() )
					throw new RuntimeException( "Unable to create file: " + lockFile.getAbsolutePath() );

			try
			{
				@SuppressWarnings( "resource" )
				RandomAccessFile randomAccessFile = new RandomAccessFile( lockFile, "rw" );
				FileLock lock = randomAccessFile.getChannel().tryLock();
				if( lock == null )
				{
					ErrorHandler.promptRestart( "An instance of LoadUI is already running!" );
					exitInError();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		catch( OverlappingFileLockException e )
		{
			System.err.println( "An instance of loadUI is already running!" );
			exitInError();
		}
		catch( IOException e )
		{
			e.printStackTrace();
			exitInError();
		}
	}

	protected abstract void processOsgiExtraPackages();

	protected final static void exitInError()
	{
		try
		{
			System.err.println( "Exiting..." );
			Thread.sleep( 5000 );
		}
		catch( InterruptedException e )
		{
		} finally
		{
			System.exit( -1 );
		}

	}


	public void start()
	{
		try
		{
			framework.start();
			System.out.println( "Framework started!" );
			afterStart();
		}
		catch( BundleException e )
		{
			e.printStackTrace();
		}
	}

	protected abstract String commandLineServiceOsgiFilter();

	protected void afterStart()
	{
		String filter = commandLineServiceOsgiFilter();
		if( filter != null )
		{
			try
			{
				Object cliService = getService( CommandLineParser.class, filter );

				// cannot cast to CommandLineParser as the this instance was loaded with a different classloader
				Method parse = cliService.getClass().getMethod( "parse", new String[0].getClass() );
				parse.invoke( cliService, ( Object )argv ); // casting to Object is necessary so varargs won't split up the array
			}
			catch( Exception e )
			{
				e.printStackTrace();
				exitInError();
			}
		}

	}

	public void stop() throws Exception
	{
		framework.getBundleContext().getBundle( 0 ).stop();
	}

	public <K> void publishService( Class<K> serviceClass,
											  K service,
											  Dictionary<String, ?> properties )
	{
		if( service != null )
			framework.getBundleContext().registerService( serviceClass, service, properties );
	}

	public Object getService( Class<?> serviceClass, String osgiFilter ) throws InvalidSyntaxException
	{
		int tries = 20;
		ServiceReference[] references;
		do
		{
			references = framework.getBundleContext().getServiceReferences( serviceClass.getName(), osgiFilter );
			sleep( 500 );
		} while( ( references == null || references.length == 0 ) && --tries > 0 );

		if( references == null || references.length == 0 )
		{
			throw new RuntimeException( "Service with class " + serviceClass.getName()
					+ " cannot be found. Osgi filter: " + osgiFilter );
		}
		else
		{
			ServiceReference serviceRef = references[0];
			return framework.getBundleContext().getService( serviceRef );
		}
	}

	private void sleep( long time )
	{
		try
		{
			Thread.sleep( time );
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * Allows sub-classes to check the installed bundles before they are started.
	 * Default action does nothing.
	 *
	 * @param bundles
	 */
	protected void beforeBundlesStart( Bundle[] bundles )
	{
		// no action
	}

	private static void setDefaultHomeToEnvironmentHomeIfAvailable()
	{
		String userHome = System.getenv( "USER_HOME" );
		if( userHome != null )
		{
			System.setProperty( "user.home", userHome );
		}
	}

	private static void setOsgiConfigurationProperties()
	{
		setDefaultSystemProperty( Main.CONFIG_PROPERTIES_PROP, Paths
				.get( LoadUI.getWorkingDir().getAbsolutePath(), Main.CONFIG_DIRECTORY, Main.CONFIG_PROPERTIES_FILE_VALUE )
				.toUri()
				.toString() );
		setDefaultSystemProperty( Main.SYSTEM_PROPERTIES_PROP, Paths
				.get( LoadUI.getWorkingDir().getAbsolutePath(), Main.CONFIG_DIRECTORY, Main.SYSTEM_PROPERTIES_FILE_VALUE )
				.toUri()
				.toString() );
	}

	public static void initSystemProperties()
	{
		setDefaultHomeToEnvironmentHomeIfAvailable();

		setOsgiConfigurationProperties();

		setDefaultSystemProperty( LOADUI_HOME, System.getProperty( "user.home" ) + File.separator + ".loadui" );

		setDefaultSystemProperty( "groovy.root", System.getProperty( LOADUI_HOME ) + File.separator + ".groovy" );

		setDefaultSystemProperty( "loadui.ssl.keyStore", System.getProperty( LOADUI_HOME ) + File.separator
				+ "keystore.jks" );
		setDefaultSystemProperty( "loadui.ssl.trustStore", System.getProperty( LOADUI_HOME ) + File.separator
				+ "certificate.pem" );
		setDefaultSystemProperty( "loadui.ssl.keyStorePassword", "password" );
		setDefaultSystemProperty( "loadui.ssl.trustStorePassword", "password" );

		setDefaultSystemProperty( LoadUI.INSTANCE, LoadUI.CONTROLLER );

		File loaduiHome = new File( System.getProperty( LOADUI_HOME ) );
		System.out.println( "LoadUI Home: " + loaduiHome.getAbsolutePath() );
		if( !loaduiHome.isDirectory() )
			if( !loaduiHome.mkdirs() )
				throw new RuntimeException( "Unable to create directory: " + loaduiHome.getAbsolutePath() );

		File keystore = new File( System.getProperty( "loadui.ssl.keyStore" ) );

		//Remove the old expired keystore, if it exists
		if( keystore.exists() )
		{
			try(FileInputStream kis = new FileInputStream( keystore ))
			{
				MessageDigest digest = MessageDigest.getInstance( "MD5" );
				byte[] buffer = new byte[8192];
				int read = 0;
				while( ( read = kis.read( buffer ) ) > 0 )
				{
					digest.update( buffer, 0, read );
				}
				String hash = new BigInteger( 1, digest.digest() ).toString( 16 );
				if( "10801d8ea0f0562aa3ae22dcea258339".equals( hash ) )
				{
					if( !keystore.delete() )
						System.err.println( "Could not delete old keystore: " + keystore.getAbsolutePath() );
				}
			}
			catch( NoSuchAlgorithmException | IOException e )
			{
				e.printStackTrace();
			}
		}

		if( !keystore.exists() )
		{
			createKeyStore( keystore );
		}

		File truststore = new File( System.getProperty( "loadui.ssl.trustStore" ) );
		if( !truststore.exists() )
		{
			createTrustStore( truststore );
		}
	}

	private static void createKeyStore( File keystore )
	{
		try(FileOutputStream fos = new FileOutputStream( keystore );
			 InputStream is = LoadUILauncher.class.getResourceAsStream( "/keystore.jks" ))
		{
			byte buf[] = new byte[1024];
			int len;
			while( ( len = is.read( buf ) ) > 0 )
				fos.write( buf, 0, len );

		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	private static void createTrustStore( File truststore )
	{
		try(FileOutputStream fos = new FileOutputStream( truststore );
			 InputStream is = LoadUILauncher.class.getResourceAsStream( "/certificate.pem" ))
		{
			byte buf[] = new byte[1024];
			int len;
			while( ( len = is.read( buf ) ) > 0 )
				fos.write( buf, 0, len );

		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	protected static void setDefaultSystemProperty( String property, String value )
	{
		if( System.getProperty( property ) == null )
		{
			System.setProperty( property, value );
		}
	}
}
