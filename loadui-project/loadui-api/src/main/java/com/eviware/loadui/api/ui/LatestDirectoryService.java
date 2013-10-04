package com.eviware.loadui.api.ui;

import com.eviware.loadui.api.model.AttributeHolder;

import java.io.File;

/**
 * LatestDirectoryService is a service for getting and setting latest directory.
 * Author: maximilian.skog
 * Date: 2013-10-01
 * Time: 14:14
 */
public interface LatestDirectoryService
{
	File getLatestDirectory( String attributeIdentifier, AttributeHolder attributeHolder );

	void setLatestDirectory( String attributeIdentifier, File directory, AttributeHolder attributeHolder );
}
