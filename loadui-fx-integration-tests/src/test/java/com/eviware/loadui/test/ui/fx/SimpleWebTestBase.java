package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.states.SimpleWebTestState;
import org.junit.After;

import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.WEB_PAGE_RUNNER;

/**
 * @author renato
 */
public abstract class SimpleWebTestBase extends FxIntegrationTestBase
{
	public static final String VALID_URL_TO_HIT_ON_TESTS = "win-srvmontest";

	@Override
	@After
	public void teardown() throws Exception
	{
		ensureProjectIsNotRunning();
		robot.deleteAllComponentsFromProjectView();
		super.teardown();
	}

	public void setMaxConcurrentRequestsTo( int requests )
	{
		click( ".web-page-runner .menu-button" ).click( "Settings" ).click( "#max-concurrent-requests" )
				.doubleClick().type( Integer.toString( requests ) ).click( "#default" );
	}

	protected void setWebPageRunnerUrlTo( String text )
	{
		doubleClick( find( ".text-input", robot.getComponentNode( WEB_PAGE_RUNNER ) ) ).type( text );
	}

	@Override
	public TestState getStartingState()
	{
		return SimpleWebTestState.STATE;
	}

}
