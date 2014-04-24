package com.eviware.loadui.components.web;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.impl.component.RunnerCountersDisplay;
import com.eviware.loadui.impl.layout.*;
import com.eviware.loadui.util.property.UrlProperty;
import com.google.common.collect.ImmutableMap;

import static com.eviware.loadui.api.component.ComponentContext.Scope.COMPONENT;
import static com.eviware.loadui.api.component.categories.RunnerCategory.SAMPLE_ACTION;
import static com.eviware.loadui.impl.layout.ActionLayoutComponentImpl.ACTION;

public class WebRunnerLayout extends LayoutContainerImpl
{

	public WebRunnerLayout( UrlProperty webPageUrlProperty, final ComponentContext context )
	{
		super( "gap 10 5", "", "align top", "" );
		LayoutContainer box = new LayoutContainerImpl( "wrap 2, ins 0", "", "align top", "" );



		box.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object>builder()
				.put( PropertyLayoutComponentImpl.PROPERTY, webPageUrlProperty.getUrlProperty() )
				.put( PropertyLayoutComponentImpl.LABEL, "URL" )
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 300!, spanx 2" )
				.build() ) );
		box.add( new ActionLayoutComponentImpl( ImmutableMap.<String, Object>builder()
				.put( ActionLayoutComponentImpl.LABEL, "Run Once" )
				.put( ACTION, new Runnable()
				{
					@Override
					public void run()
					{
						String url = context.getProperty( UrlProperty.URL ).getStringValue();
						((WebRunner) context.getComponent().getBehavior()).updateWebPageUrl( url );
						context.triggerAction( SAMPLE_ACTION, COMPONENT );
					}
				} ).build() ) );


		//TODO: remove this when component is done
		add( new LabelLayoutComponentImpl( "NOTICE: This component is a preview.", "spanx" ) );
		//TODO: also this
		add( new SeparatorLayoutComponentImpl( false, "newline, growx, spanx" ) );


		add( box );
		add( new SeparatorLayoutComponentImpl( true, "grow y" ) );
		add( RunnerCountersDisplay.forRunner( context.getComponent() ) );
	}

}
