package com.eviware.loadui.components.soapui.utils;

import javax.annotation.Nonnull;
import java.io.File;

import static com.eviware.loadui.components.soapui.utils.SoapUiProjectUtils.makeNonCompositeCopy;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author renato
 */
public class CompositeProjectUtils
{

	@Nonnull
	public File fromCompositeDirectory( @Nonnull File projectDirectory )
	{
		checkArgument( projectDirectory.exists(), "File must exist" );
		checkArgument( projectDirectory.isDirectory(), "File must be a directory" );

		return makeNonCompositeCopy( projectDirectory );
	}
}
