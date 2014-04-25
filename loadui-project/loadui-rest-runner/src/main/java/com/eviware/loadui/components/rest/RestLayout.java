package com.eviware.loadui.components.rest;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.impl.component.RunnerCountersDisplay;
import com.eviware.loadui.impl.layout.*;
import com.google.common.collect.ImmutableMap;

import static com.eviware.loadui.api.component.ComponentContext.Scope.COMPONENT;
import static com.eviware.loadui.api.component.categories.RunnerCategory.SAMPLE_ACTION;
import static com.eviware.loadui.components.rest.RestRunner.BODY;
import static com.eviware.loadui.components.rest.RestRunner.METHOD;
import static com.eviware.loadui.util.property.UrlProperty.URL;
import static com.eviware.loadui.impl.layout.ActionLayoutComponentImpl.ACTION;
import static com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl.LABEL;
import static com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl.PROPERTY;

public class RestLayout extends LayoutContainerImpl
{
	private final ComponentContext context;

	public RestLayout( final ComponentContext context )
	{
		super( "gap 10 5", "", "align top", "" );
		this.context = context;
		add( buildRestFields() );
		add( new SeparatorLayoutComponentImpl( true, "grow y" ) );
		add( RunnerCountersDisplay.forRunner( context.getComponent() ) );
	}

	private LayoutContainer buildRestFields()
	{
		LayoutContainer box = new LayoutContainerImpl( "wrap 2, ins 0", "", "align top", "" );
		box.add( property( METHOD, "HTTP Method" ) );
		box.add( property( URL, "URL", "-fx-font-size: 17pt" ) );
		box.add( property( BODY, "Entity body" ) );
		box.add( new ActionLayoutComponentImpl( ImmutableMap.<String, Object>builder()
				.put( ActionLayoutComponentImpl.LABEL, "Run Once" )
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

	private PropertyLayoutComponentImpl<String> property( String name, String label, String style )
	{
		ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object>builder()
				.put( PROPERTY, context.getProperty( name ) )
				.put( LABEL, label )
				.put( CONSTRAINTS, "spanx 2" );
		if( style != null )
			builder.put( "style", style );
		return new PropertyLayoutComponentImpl<String>( builder.build() );
	}

	private PropertyLayoutComponentImpl<String> property( String name, String label )
	{
		return property( name, label, null );
	}
}
