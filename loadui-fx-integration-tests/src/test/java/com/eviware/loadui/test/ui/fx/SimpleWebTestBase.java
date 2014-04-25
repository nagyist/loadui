package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.states.SimpleWebTestState;
import org.junit.After;

import static com.eviware.loadui.util.LoadUIComponents.HTTP_RUNNER;

/**
 * @author renato
 */
public abstract class SimpleWebTestBase extends FxIntegrationTestBase
{
	public static final String VALID_URL_TO_HIT_ON_TESTS = "win-srvmontest";

	@After
	public void cleanup() throws Exception
	{
		ensureProjectIsNotRunning();
		robot.deleteAllComponentsFromProjectView();
	}

	public void setMaxConcurrentRequestsTo( int requests )
	{
		click( HTTP_RUNNER.cssClass() + " .menu-button" ).click( "Settings" ).click( "#max-concurrent-requests" )
				.doubleClick().type( Integer.toString( requests ) ).click( "#default" );
	}

	protected void clickOnAbortButton()
	{
		click( "#abort-requests" ).sleep( 1_000 );
	}

	protected void setWebPageRunnerUrlTo( String text )
	{
		doubleClick( ".component-view .text-input" ).type( text );
	}

	@Override
	public TestState getStartingState()
	{
		return SimpleWebTestState.STATE;
	}

}
