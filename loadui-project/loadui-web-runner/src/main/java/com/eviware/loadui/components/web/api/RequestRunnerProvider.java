package com.eviware.loadui.components.web.api;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.components.web.RequestRunner;

import java.net.URI;

public interface RequestRunnerProvider
{
	RequestRunner provideRequestRunner( ComponentContext context, Iterable<URI> pageUris );
}
