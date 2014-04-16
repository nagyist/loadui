package com.eviware.loadui.components.rest;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.impl.component.RunnerCountersDisplay;
import com.eviware.loadui.impl.layout.LayoutContainerImpl;


public class RestCompactLayout extends LayoutContainerImpl
{
	public RestCompactLayout( ComponentContext context )
	{
		super( "gap 10 5", "", "align top", "" );
		LayoutContainer box = new LayoutContainerImpl( "wrap 2, ins 0", "", "align top", "" );
		add( box );
		add( RunnerCountersDisplay.forRunner( context.getComponent() ) );
	}
}
