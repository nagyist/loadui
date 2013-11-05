package com.eviware.loadui.test.ui.fx.tutorials;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.SimpleWebTestState;
import com.google.common.collect.Lists;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;
import java.util.Set;

import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.FIXED_RATE_GENERATOR;

/**
 * Created with IntelliJ IDEA.
 * User: Sara
 * Date: 2013-10-17
 * Time: 15:34
 * To change this template use File | Settings | File Templates.
 */
@Category( IntegrationTest.class )
public class Statistics extends FxIntegrationTestBase
{
	private Node segmentView;

	@Override
	public TestState getStartingState()
	{
		return SimpleWebTestState.STATE;

	}

	@Test
	public void Statistics()
	{
		robot.clickPlayStopButton();

		click( "#statsTab" );
		drag( ".item-holder" ).to( ".scroll-pane" );
		click( "Add" );
		sleep( 3000 );
		drag( "#global" ).by( 600, 150 ).drop();
		click( "Add" );
		sleep( 3000 );
		click( "Design" );
		turnKnobIn( FIXED_RATE_GENERATOR ).to( 15 );
		sleep( 3000 );
		click( "#statsTab" );
		sleep( 4000 );
		click( "#zoomMenuButton" );
		click( "Minutes" );
		sleep( 3000 );
		click( "#zoomMenuButton" );
		click( "Seconds" );

		Node TPSMenuButton = getMenuButtonOnLegend(2);
		click( TPSMenuButton );
		click( "Scale" );

		Node TPSScaleSlider = getSliderOnLegend( 2 );

		drag( TPSScaleSlider ).by( 100, 0 ).drop();

		click( find( "Done", find(".chart-segment-box") ) );
		sleep( 3000 );

		robot.clickPlayStopButton();
		sleep( 2000 );
		click( "#titleMenuButton" );
		click( "Export raw data" );


	}

	private Node getMenuButtonOnLegend(int number)
	{
		return getSegmentView( number ).lookup( ".menu-button" );
	}

	private Node getSliderOnLegend(int number)
	{
		return getSegmentView( number ).lookup( ".thumb" );
	}


	public Node getSegmentView(int number)
	{
		Set<Node> nodes = findAll( ".chart-segment-box .slim-icon" );

		List<Node> all = Lists.newArrayList(nodes);

		return all.get( number-1 );
	}
}


