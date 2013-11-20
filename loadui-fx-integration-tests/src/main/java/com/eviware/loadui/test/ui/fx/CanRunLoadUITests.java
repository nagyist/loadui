package com.eviware.loadui.test.ui.fx;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.loadui.testfx.Matchers.visible;


/**
 * @author renato
 */
public class CanRunLoadUITests extends FxIntegrationBase
{

	public void abortRequestsIfPossible()
	{
		if( !findAll( "#abort-requests" ).isEmpty() )
		{
			click( "#abort-requests" );
			waitUntil( "#abort-requests", is( not( visible() ) ) );
		}
	}

	public void letGuiReactToProjectStopping()
	{
		sleep( 100 );
		waitForNodeToDisappear( "#abort-requests" );
	}

}
