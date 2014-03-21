package com.eviware.loadui.components.web;

import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.impl.layout.LayoutContainerImpl;
import com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl;
import com.google.common.collect.ImmutableMap;

public class WebRunnerLayout extends LayoutContainerImpl
{

	public WebRunnerLayout( Property<String> webPageUrlProperty )
	{
		super( "gap 10 5", "", "align top", "" );
		LayoutContainer box = new LayoutContainerImpl( "ins 0", "", "align top", "" );

		box.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object>builder()
				.put( PropertyLayoutComponentImpl.PROPERTY, webPageUrlProperty )
				.put( PropertyLayoutComponentImpl.LABEL, "URL" )
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 300!, spanx 2" )
				.build() ) );

		this.add( box );
	}

}
