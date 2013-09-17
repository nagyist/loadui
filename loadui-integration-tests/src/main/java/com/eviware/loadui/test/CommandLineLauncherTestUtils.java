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

import com.google.common.collect.ObjectArrays;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.eviware.loadui.test.IntegrationTestUtils.getTailAsArray;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;

/**
 * Author: maximilian.skog
 * Date: 2013-07-04
 * Time: 11:08
 */
public class CommandLineLauncherTestUtils
{
	protected static final Logger log = LoggerFactory.getLogger( CommandLineLauncherTestUtils.class );
	public static final String CMD_RUNNER_NAME_WINDOWS = "loadUI-cmd.bat";
	public static final String CMD_RUNNER_NAME_OSX = "loadUI-cmd.command";
	public static final String CMD_RUNNER_NAME_UNIX = "loadUI-cmd.sh";

	private synchronized int launchCommandLineRunner( String[] commands )
	{
		int exitValue = -1;

		ProcessBuilder procBuilder = new ProcessBuilder( commands );

		Process proc = null;

		try
		{
			proc = procBuilder
					.inheritIO()
					.directory( new File( getMainFolderLocation() ) )
					.start();

			attachStreamPrinter( proc );

			exitValue = proc.waitFor();
		}
		catch( IOException e )
		{
			throw new RuntimeException( "Failed to launch command line runner with command: " + Arrays.toString( commands ), e );
		}
		catch( InterruptedException e )
		{
			//  thrown if waitFor() gets interrupted
			throw new RuntimeException( "command line runner was interrupted unexpectedly", e );
		}
		finally
		{
			if( proc != null )
			{
				try
				{
					proc.destroy();
				}
				catch( Exception e )
				{
					log.debug( "Unable to destroy process", e );
				}
			}
		}

		return exitValue;
	}

	private void attachStreamPrinter( Process proc )
	{
		StreamPrinter errorListener = new StreamPrinter( proc.getErrorStream(), "ERROR" );
		StreamPrinter inputListener = new StreamPrinter( proc.getInputStream(), "INPUT" );

		errorListener.start();
		inputListener.start();
	}

	public Collection<File> getFilesAt( String path ) throws NullPointerException
	{
		File folder = new File( path );

		checkArgument( folder.isDirectory(), "The path " + path + " is not a directory" );

		ArrayList<File> filesInPath = new ArrayList<>();

		for( File file : folder.listFiles() )
			if( file.isFile() )
				filesInPath.add( file );

		return filesInPath;
	}

	public String getXMLFrom( File summaryFile ) throws RuntimeException
	{
		try( FileInputStream inputStream = new FileInputStream( summaryFile ) )
		{
			return IOUtils.toString( inputStream );
		}
		catch( IOException e )
		{
			throw new RuntimeException( "Could not get text of file " + summaryFile.getAbsolutePath(), e );
		}
	}

	public String getCmdRunnerFileName()
	{
		if( SystemUtils.IS_OS_WINDOWS )
		{
			return CMD_RUNNER_NAME_WINDOWS;
		}
		else if( SystemUtils.IS_OS_MAC )
		{
			return CMD_RUNNER_NAME_OSX;
		}
		else // linux
		{
			return CMD_RUNNER_NAME_UNIX;
		}
	}

	public int launchCommandLineRunnerWithCommands( String... commands )
	{
		return launchCommandLineRunner( ObjectArrays.concat( getLaunchCommands(), commands, String.class ) );
	}

	private String[] getLaunchCommands()
	{

		if( SystemUtils.IS_OS_WINDOWS )
		{
			return new String[] { getLauncherPath() };
		}
		else // linux, mac, whatever
		{
			return new String[] { "sh", getLauncherPath() };
		}

	}

	private String getLauncherPath()
	{
		return Paths.get( getMainFolderLocation(), getCmdRunnerFileName() ).toFile().getAbsolutePath();
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
			try( BufferedReader br = new BufferedReader( new InputStreamReader( is ) ) )
			{
				String line;
				while( ( line = br.readLine() ) != null )
					System.out.println( "StreamPrinter:" + type + " > " + line );
			}
			catch( IOException ioe )
			{
				log.warn( "Problem logging command line output " + ioe );
			}
		}
	}

	public String getMainFolderLocation()
	{
		for( List<String> candidateMainFolder : getCandidateLocationsForMainFolder() )
		{
			Path path = Paths.get( candidateMainFolder.get( 0 ), getTailAsArray( candidateMainFolder, String.class ) );
			if( path.toFile().exists() )
			{
				return path.toFile().getAbsolutePath();
			}
		}
		throw new RuntimeException( "Could not find main folder, tried the following locations: " +
				getCandidateLocationsForMainFolder() );
	}

	protected List<List<String>> getCandidateLocationsForMainFolder()
	{
		return asList( asList( "loadui-installers", "loadui-controller-installer", "target", "main" ),
				asList( "..", "loadui-installers", "loadui-controller-installer", "target", "main" ) );
	}

}
