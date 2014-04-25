package com.eviware.loadui.util.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemHelper
{
	Logger log = LoggerFactory.getLogger( FileSystemHelper.class );

	public boolean isDirectory( Path path )
	{
		return Files.isDirectory( path );
	}

	public boolean exists( Path path )
	{
		return Files.exists( path );
	}

	public boolean areSameLocation( Path p1, Path p2 )
	{
		log.info( "Checking if " + p1 + " and " + p2 + " are the same path" );
		return p1.equals( p2 );
	}

	public PathWatcher providePathWatcher()
	{
		return PathWatcher.singleThreadedPathWatcher();
	}

	public Path makeAbsolute( Path parent, Path path )
	{
		return parent.resolve( path );
	}
}