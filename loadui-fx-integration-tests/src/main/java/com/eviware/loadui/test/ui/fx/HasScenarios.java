package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.test.ui.fx.states.ScenarioCreatedState;
import com.google.code.tempusfugit.temporal.Condition;
import com.google.common.collect.Lists;
import javafx.scene.Node;
import org.loadui.testfx.GuiTest;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
import static org.junit.Assert.fail;

/**
 * @author renato
 */
public class HasScenarios extends FxIntegrationBase
{
	/**
	 * If there are several scenarios, provide the indexes of each scenario to use.
	 *
	 * @param scenarioIndexes indexes of scenarios to use, starting from 1
	 */
	public void clickOnLinkScenarioButton( int... scenarioIndexes )
	{
		List<Node> linkButtons = Lists.newArrayList( GuiTest.findAll( "#link-scenario" ) );

		if( scenarioIndexes.length == 0 )
			scenarioIndexes = new int[] { 1 };


		for( int index : scenarioIndexes )
		{
			click( linkButtons.get( index - 1 ) ).sleep( 500 );
		}
	}

	public void enterScenario()
	{
		doubleClick( ".scenario-view" );
	}

	public void exitScenarioIfPossible()
	{
		if( !findAll( "#closeScenarioButton" ).isEmpty() )
			doubleClick( "#closeScenarioButton" );
	}

	public SceneItem ensureScenarioIsLinkedIs( final boolean follow )
	{
		final SceneItem scenario = ScenarioCreatedState.STATE.getScenario();
		if( scenario.isFollowProject() != follow )
		{
			clickOnLinkScenarioButton();
		}
		try
		{
			waitOrTimeout( new Condition()
			{
				@Override
				public boolean isSatisfied()
				{
					return scenario.isFollowProject() == follow;
				}
			}, timeout( seconds( 2 ) ) );
		}
		catch( InterruptedException | TimeoutException e )
		{
			e.printStackTrace();
			fail( "Problem while waiting for scenario to be in Linked Mode" );
		}
		return scenario;
	}

	public void createScenario( int x, int y )
	{
		waitForNode( "#newScenarioIcon" );
		drag( "#newScenarioIcon" ).by( x, y ).drop();
		waitForNode( ".scenario-view" );
	}

}
