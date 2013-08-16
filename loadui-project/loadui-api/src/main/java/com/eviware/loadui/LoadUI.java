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
package com.eviware.loadui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LoadUI
{
	public static final String ARGUMENTS = "sun.java.command";
	/**
	 * The main version number of loadUI.
	 */
	private static String loadUiVersion;

	/**
	 * Internal version number used to determine controller/agent compatibility.
	 * Compatibility is only ensured when this version string is the same for
	 * both agent and controller.
	 */
	public static final String AGENT_VERSION = "13";

	public static final String INSTANCE = "loadui.instance";
	public static final String CONTROLLER = "controller";
	public static final String AGENT = "agent";

	public static final String HEADLESS = "loadui.headless";

	public static final String NAME = "loadui.name";
	public static final String BUILD_NUMBER = "loadui.build.number";
	public static final String BUILD_DATE = "loadui.build.date";

	public static final String LOADUI_HOME = "loadui.home";
	public static final String LOADUI_WORKING = "loadui.working";

	public static final String HTTPS_PORT = "loadui.https.port";

	public static final String DISABLE_STATISTICS = "loadui.statistics.disable";
	public static final String DISABLE_DISCOVERY = "loadui.discovery.disable";

	public static final String KEY_STORE = "loadui.ssl.keyStore";
	public static final String TRUST_STORE = "loadui.ssl.trustStore";
	public static final String KEY_STORE_PASSWORD = "loadui.ssl.keyStorePassword";
	public static final String TRUST_STORE_PASSWORD = "loadui.ssl.trustStorePassword";
	public static final String CLASSPATH = "java.class.path";


	public static synchronized String version()
	{
		if( loadUiVersion == null )
		{
			Properties systemProperties = new Properties();
			File sysPropsFile = Paths.get( getWorkingDir().getAbsolutePath(), "conf", "system.properties" ).toFile();
			try(FileInputStream fis = new FileInputStream( sysPropsFile ))
			{
				systemProperties.load( fis );
				loadUiVersion = systemProperties.getProperty( "loadui.version" );
				if( loadUiVersion == null )
				{
					throw new RuntimeException( "LoadUI version is unknown, it should be declared in " +
							sysPropsFile.getAbsolutePath() );
				}
			}
			catch( IOException e )
			{
				throw new RuntimeException( "LoadUI version is unknown, cannot read file " +
						sysPropsFile.getAbsolutePath(), e );
			}
		}
		return loadUiVersion;
	}

	public static boolean isController()
	{
		return CONTROLLER.equals( System.getProperty( INSTANCE ) );
	}

	public static boolean isHeadless()
	{
		return "true".equals( System.getProperty( HEADLESS, "true" ) );
	}

	public static boolean isPro()
	{
		return Boolean.parseBoolean( System.getProperty( "loadui.pro" ) );
	}

	/**
	 * Gets the directory from where all relative paths should be resolved.
	 *
	 * @return
	 */
	public static File getWorkingDir()
	{
		return new File( System.getProperty( LOADUI_WORKING, "." ) ).getAbsoluteFile();
	}

	public static File relativeFile( String path )
	{
		return new File( getWorkingDir(), path );
	}

	public static void restart()
	{
		String executable = "";
		File f = null;
		if( LoadUI.isRunningOnWindows() )
		{
			f = new File( "jre/bin/java.exe" );
		}
		else if( LoadUI.isRunningOnLinux() )
		{
			f = new File( "jre/bin/java" );
		}
		else
		{
			executable = "java";
		}

		if( f.exists() )
		{
			executable = f.getAbsolutePath();
		}

		try
		{
			List<String> commands = new ArrayList<>();
			commands.add( executable );
			commands.add( "-cp" );
			commands.add( System.getProperty( CLASSPATH ) );
			commands.add( "-Xms128m" );
			commands.add( "-Xmx1024m" );
			commands.add( "-XX:MaxPermSize=128m" );

			for( String arg : System.getProperty( ARGUMENTS ).split( " " ) ){
				if(arg.length() > 0){
					commands.add( arg );
				}
			}

			ProcessBuilder pb = new ProcessBuilder( commands );
			Process p = pb.inheritIO().start();
			p.waitFor();

			System.exit(0);
		}
		catch( IOException | InterruptedException e )
		{
			e.printStackTrace();
		}
	}

	private static final String OS = System.getProperty( "os.name" );

	public static boolean isRunningOnMac()
	{
		return OS.startsWith( "Mac" );
	}

	public static boolean isRunningOnLinux()
	{
		return OS.startsWith( "Linux" );
	}

	public static boolean isRunningOnWindows()
	{
		return OS.startsWith( "Windows" );
	}
}

