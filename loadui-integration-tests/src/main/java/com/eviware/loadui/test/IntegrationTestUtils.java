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

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.List;

/**
 * A collection of static utility methods for simplifying some tasks.
 *
 * @author dain.nilsson
 */
public class IntegrationTestUtils
{
	public static boolean deleteRecursive( File target )
	{
		if( target.isDirectory() )
		{
			for( File file : target.listFiles() )
				if( !deleteRecursive( file ) )
					return false;
		}
		return target.delete();
	}

	@Nullable
	public static File newestDirectoryIn( @Nonnull File directory )
	{
		File[] files = directory.listFiles();
		if( files == null || files.length == 0 )
			return null;
		File newest = null;
		long newestTime = Long.MIN_VALUE;
		for( File file : files )
		{
			if( file.isDirectory() && file.lastModified() > newestTime )
			{
				newest = file;
				newestTime = file.lastModified();
			}
		}
		return newest;
	}

	public static void copyDirectory( File sourceLocation, File targetLocation ) throws IOException
	{
		if( !sourceLocation.exists() )
			throw new IOException( "File does not exist: " + sourceLocation );
		if( sourceLocation.isDirectory() )
		{
			if( !targetLocation.exists() && !targetLocation.mkdir() )
				throw new IOException( "Unable to create directory: " + targetLocation );

			for( String child : sourceLocation.list() )
				copyDirectory( new File( sourceLocation, child ), new File( targetLocation, child ) );
		}
		else
		{
			Files.copy( sourceLocation, targetLocation );
		}
	}

	public static int getAvailablePort()
	{

		try(ServerSocket ss = new ServerSocket( 0 ))
		{
			ss.setReuseAddress( true );
			return ss.getLocalPort();
		}
		catch( IOException e )
		{
		} finally
		{
			try
			{
				Thread.sleep( 1000 );
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}
		}

		return -1;
	}

	public static int getAvailablePort( int start )
	{
		while( !isPortAvailable( start ) )
			start++;

		return start;
	}

	public static boolean isPortAvailable( int port )
	{
		try(ServerSocket ss = new ServerSocket( port ))
		{
			ss.setReuseAddress( true );
			return true;
		}
		catch( IOException e )
		{
		}

		return false;
	}

	public static <T> T[] getTailAsArray( Collection<T> collection, Class<T> type )
	{
		List<T> list = Lists.newArrayList( collection );
		list.remove( 0 );
		return list.toArray( ( T[] )Array.newInstance( type, list.size() ) );
	}
}
