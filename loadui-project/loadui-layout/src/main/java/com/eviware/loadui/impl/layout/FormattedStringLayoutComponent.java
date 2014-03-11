package com.eviware.loadui.impl.layout;

import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.util.ReleasableUtils;

import java.util.Map;

public class FormattedStringLayoutComponent extends LayoutComponentImpl implements Releasable
{
	public FormattedStringLayoutComponent( Map<String, ?> args )
	{
		super( args );
	}

	@Override
	public void release()
	{
		ReleasableUtils.releaseAll( get( "fString" ) );
	}
}
