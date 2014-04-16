package com.eviware.loadui.components.web;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.api.component.ComponentContext;
import com.google.common.base.Optional;

public class WebRunnerStatsSenderFactory
{
	private static Optional<WebRunnerStatsSender> oldInstance = Optional.absent();

	public synchronized static WebRunnerStatsSender newInstance( ComponentContext context, Clock clock )
	{
		if( oldInstance.isPresent() )
		{
			oldInstance.get().release();
		}

		WebRunnerStatsSender newInstance = new WebRunnerStatsSender( context, clock );

		oldInstance = Optional.of( newInstance );

		return newInstance;
	}
}
