package com.eviware.loadui.components.soapui.utils;

import com.eviware.x.dialogs.XFileDialogs;

import java.io.File;

/**
 * @author renato
 */
public class NoOpXFileDialogs implements XFileDialogs
{
	@Override
	public File saveAs( Object o, String s, String s2, String s3, File file )
	{
		return null;
	}

	@Override
	public File saveAs( Object o, String s )
	{
		return null;
	}

	@Override
	public File saveAsDirectory( Object o, String s, File file )
	{
		return null;
	}

	@Override
	public File open( Object o, String s, String s2, String s3, String s4 )
	{
		return null;
	}

	@Override
	public File openXML( Object o, String s )
	{
		return null;
	}

	@Override
	public File openDirectory( Object o, String s, File file )
	{
		return null;
	}

	@Override
	public File openFileOrDirectory( Object o, String s, File file )
	{
		return null;
	}
}
