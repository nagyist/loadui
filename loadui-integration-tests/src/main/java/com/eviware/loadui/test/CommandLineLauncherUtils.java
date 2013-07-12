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

package com.eviware.loadui.test;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Author: maximilian.skog
 * Date: 2013-07-04
 * Time: 11:08
 */
public class CommandLineLauncherUtils
{
	protected static final Logger log = LoggerFactory.getLogger( CommandLineLauncherUtils.class );
	public static final String LOAD_UI_CMD_BAT_NAME = "loadUI-cmd.bat";

	public static synchronized int launchCommandLineRunner( String pathToLauncher, String[] commands )
	{
		int exitValue = -1;

		final String[] pathToCommandLineBat = { pathToLauncher + File.separator + LOAD_UI_CMD_BAT_NAME };

		final String[] command = concat( pathToCommandLineBat, commands );

		Process proc = null;
		try
		{
			proc = Runtime.getRuntime().exec( command );

			AttachStreamPrinter( proc );

			exitValue = proc.waitFor();
		}
		catch( IOException e )
		{
			throw new RuntimeException( "Failed to launch command line runner with command " + Arrays.toString(command), e );
		}
		catch( InterruptedException e )
		{
			//  thrown if waitFor() gets interrupted
			throw new RuntimeException( "command line runner was interrupted unexpectedly", e );
		} finally
		{
			if( proc != null )
			{
				try
				{
					proc.destroy();
				}
				catch( Exception e )
				{
					log.debug( "Unable to destroy process for license-manager", e );
				}
			}
		}
		return exitValue;
	}

	private static void AttachStreamPrinter( Process proc )
	{
		StreamPrinter errorListener = new StreamPrinter( proc.getErrorStream(), "ERROR" );
		StreamPrinter inputListener = new StreamPrinter( proc.getInputStream(), "INPUT" );

		errorListener.start();
		inputListener.start();
	}

	public static Collection<File> getFilesAt( String path ) throws NullPointerException
	{
		File folder = new File( path );

		ArrayList<File> filesInPath = new ArrayList<>();

		if( folder.isDirectory() )
		{
			for( File file : folder.listFiles() )
			{
				if( file.isFile() )
				{
					filesInPath.add( file );
				}
			}
		}
		else
		{
			throw new RuntimeException( "The path " + path + " is not a directory" );
		}

		return filesInPath;
	}

	public static String getXMLFrom( File summaryFile )
	{
		try(FileInputStream inputStream = new FileInputStream( summaryFile ))
		{
			return IOUtils.toString( inputStream );

		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		return null;
	}

	static class StreamPrinter extends Thread
	{
		InputStream is;
		String type;

		StreamPrinter( InputStream is, String type )
		{
			this.is = is;
			this.type = type;
		}

		public void run()
		{
			try(BufferedReader br = new BufferedReader( new InputStreamReader( is ) ))
			{
				String line;
				while( ( line = br.readLine() ) != null )
					System.out.println( "StreamPrinter:" + type + " > " + line );
			}
			catch( IOException ioe )
			{
				System.out.println( "Problem logging command line output " + ioe );
			}
		}
	}

	public static String findPathToCommandLineBat()
	{
		Path pathA = Paths.get( "", "loadui-installers", "loadui-controller-installer", "target", "main", LOAD_UI_CMD_BAT_NAME );
		Path pathB = Paths.get( "", "..", "loadui-installers", "loadui-controller-installer", "target", "main", LOAD_UI_CMD_BAT_NAME );

		if( pathA.toFile().exists() )
		{
			return pathA.getParent().toFile().getAbsolutePath();
		}

		if( pathB.toFile().exists() )
		{
			return pathB.getParent().toFile().getAbsolutePath();
		}

		throw new RuntimeException( "Could not find path to the command line runner.  File at:" + pathA.toString() + " or " + pathB.toString() + " does not exist!" );
	}

	private static String[] concat( String[] first, String[] second )
	{
		List<String> both = new ArrayList<>( first.length + second.length );
		Collections.addAll( both, first );
		Collections.addAll( both, second );
		return both.toArray( new String[both.size()] );
	}


}
