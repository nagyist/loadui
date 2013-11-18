package com.eviware.loadui.components.soapui.utils;

import com.google.common.base.Preconditions;

import java.io.File;

/**
 * @author renato
 */
public class CompositeProjectUtils
{

	public File fromCompositeDirectory( File projectDirectory )
	{
		Preconditions.checkArgument( projectDirectory.isDirectory(), "File given must be a directory" );
		return SoapUiProjectUtils.makeNonCompositeCopy( projectDirectory );
	}
}
