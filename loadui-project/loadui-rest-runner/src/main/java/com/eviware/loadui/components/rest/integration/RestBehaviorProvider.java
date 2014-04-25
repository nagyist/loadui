package com.eviware.loadui.components.rest.integration;

import com.eviware.loadui.api.component.*;
import com.eviware.loadui.components.rest.RestRunner;

public class RestBehaviorProvider implements BehaviorProvider
{
	@Override
	public ComponentBehavior createBehavior( ComponentDescriptor descriptor, ComponentContext context ) throws ComponentCreationException
	{
		return loadBehavior( descriptor.getType(), context );
	}

	@Override
	public ComponentBehavior loadBehavior( String componentType, ComponentContext context ) throws ComponentCreationException
	{
		return new RestRunner( context );
	}

}
