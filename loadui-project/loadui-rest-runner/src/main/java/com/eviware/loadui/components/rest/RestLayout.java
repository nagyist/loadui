package com.eviware.loadui.components.rest;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.impl.layout.ActionLayoutComponentImpl;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.impl.layout.LayoutContainerImpl;
import com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl;
import com.google.common.collect.ImmutableMap;

import static com.eviware.loadui.api.component.ComponentContext.Scope.COMPONENT;
import static com.eviware.loadui.api.component.categories.RunnerCategory.SAMPLE_ACTION;
import static com.eviware.loadui.impl.layout.ActionLayoutComponentImpl.ACTION;
import static com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl.LABEL;
import static com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl.PROPERTY;

public class RestLayout extends LayoutContainerImpl
{
	public RestLayout( final ComponentContext context )
	{
		super( "gap 10 5", "", "align top", "" );

		add( buildRestFields( context ) );
	}

	private LayoutContainer buildRestFields( final ComponentContext context )
	{
		LayoutContainer box = new LayoutContainerImpl( "ins 0", "", "align top", "" );
		box.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object>builder()
				.put( PROPERTY, context.getProperty( RestRunner.METHOD ) )
				.put( LABEL, "HTTP Method" )
				.put( CONSTRAINTS, "w 300!, spanx 2" )
				.build() ) );
		box.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object>builder()
				.put( PROPERTY, context.getProperty( RestRunner.URL ) )
				.put( LABEL, "URL" )
				.put( CONSTRAINTS, "w 300!, spanx 2" )
				.put( "style", "-fx-font-size: 17pt" )
				.build() ) );
		box.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object>builder()
				.put( PROPERTY, context.getProperty( RestRunner.BODY ) )
				.put( LABEL, "Entity Body" )
				.put( CONSTRAINTS, "w 300!, spanx 2" )
				.build() ) );
		box.add( new ActionLayoutComponentImpl( ImmutableMap.<String, Object>builder() //
				.put( ActionLayoutComponentImpl.LABEL, "Run Once" ) //
				.put( ACTION, new Runnable()
				{
					@Override
					public void run()
					{
						context.triggerAction( SAMPLE_ACTION, COMPONENT );
					}
				} ).build() ) );
		return box;
	}
}
