package com.eviware.loadui.components.soapui.utils;

import com.eviware.x.dialogs.XDialogs;
import com.eviware.x.dialogs.XProgressDialog;

import java.awt.*;

/**
 * @author renato
 */
public class NoOpXDialogs implements XDialogs
{
	@Override
	public void showErrorMessage( String s )
	{
		// do nothing
	}

	@Override
	public void showInfoMessage( String s )
	{
		// do nothing
	}

	@Override
	public void showInfoMessage( String s, String s2 )
	{
		// do nothing
	}

	@Override
	public void showExtendedInfo( String s, String s2, String s3, Dimension dimension )
	{
		// do nothing
	}

	@Override
	public boolean confirm( String s, String s2 )
	{
		return true;
	}

	@Override
	public Boolean confirmOrCancel( String s, String s2 )
	{
		return true;
	}

	@Override
	public int yesYesToAllOrNo( String s, String s2 )
	{
		return 0;
	}

	@Override
	public String prompt( String s, String s2, String s3 )
	{
		return null;
	}

	@Override
	public String prompt( String s, String s2 )
	{
		return null;
	}

	@Override
	public Object prompt( String s, String s2, Object[] objects )
	{
		return null;
	}

	@Override
	public Object prompt( String s, String s2, Object[] objects, String s3 )
	{
		return null;
	}

	@Override
	public char[] promptPassword( String s, String s2 )
	{
		return new char[0];
	}

	@Override
	public XProgressDialog createProgressDialog( String s, int i, String s2, boolean b )
	{
		return null;
	}

	@Override
	public boolean confirmExtendedInfo( String s, String s2, String s3, Dimension dimension )
	{
		return true;
	}

	@Override
	public Boolean confirmOrCancleExtendedInfo( String s, String s2, String s3, Dimension dimension )
	{
		return true;
	}

	@Override
	public String selectXPath( String s, String s2, String s3, String s4 )
	{
		return null;
	}
}
