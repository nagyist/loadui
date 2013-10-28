package com.eviware.loadui.impl;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.ui.LatestDirectoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Author: maximilian.skog
 * Date: 2013-09-27
 * Time: 14:47
 */
public class LatestDirectoryServiceImpl implements LatestDirectoryService
{
	protected static final Logger log = LoggerFactory.getLogger( LatestDirectoryServiceImpl.class );

	@Override
	public File getLatestDirectory( String attributeIdentifier, AttributeHolder attributeHolder )
	{
		String dirStr = attributeHolder.getAttribute( attributeIdentifier, System.getProperty( LoadUI.LOADUI_HOME ) );

		File directory = new File( dirStr );

		if( !directory.isDirectory() )
		{
			directory = new File( System.getProperty( "user.home" ) );
		}

		if( !directory.isDirectory() )
		{
			throw new RuntimeException( "Could not find a valid directory. Both " + dirStr + " and " + directory.getAbsolutePath() + " is not valid" );
		}

		return directory;
	}

	@Override
	public void setLatestDirectory( String attributeIdentifier, File directory, AttributeHolder attributeHolder )
	{
		if( directory.isDirectory() )
		{
			attributeHolder.setAttribute( attributeIdentifier, directory.getAbsolutePath() );
		}
		else
		{
			log.warn( "Directory " + directory.getAbsolutePath() + " is not valid, did not set latest directory" );
		}
	}

}
