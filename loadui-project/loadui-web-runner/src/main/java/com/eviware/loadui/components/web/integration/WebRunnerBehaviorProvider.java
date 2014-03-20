package com.eviware.loadui.components.web.integration;

import com.eviware.loadui.api.component.*;
import com.eviware.loadui.components.web.RequestRunnerProvider;
import com.eviware.loadui.components.web.WebRunner;
import com.eviware.loadui.util.html.HtmlAssetScraper;

public class WebRunnerBehaviorProvider implements BehaviorProvider
{

	@Override
	public ComponentBehavior createBehavior( ComponentDescriptor descriptor, ComponentContext context ) throws ComponentCreationException
	{
		return loadBehavior( descriptor.getType(), context );
	}

	@Override
	public ComponentBehavior loadBehavior( String componentType, ComponentContext context ) throws ComponentCreationException
	{
		return new WebRunner( context, HtmlAssetScraper.create(), new RequestRunnerProvider() );
	}

}
