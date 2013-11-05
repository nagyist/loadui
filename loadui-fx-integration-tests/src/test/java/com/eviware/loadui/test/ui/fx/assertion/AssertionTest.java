package com.eviware.loadui.test.ui.fx.assertion;

import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.SimpleWebTestState;
import com.eviware.loadui.util.test.TestUtils;
import javafx.scene.Node;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.loadui.testfx.GuiTest;

import java.util.Set;
import java.util.concurrent.Callable;

/**
 * User: osten
 * Date: 8/22/13
 * Time: 12:55 PM
 */
@Category(IntegrationTest.class)

public class AssertionTest extends FxIntegrationTestBase
{
	private static GuiTest controller;
	private static final String ASSERTION_INSPECTOR = "Assertions";
	private static final String ASSERTABLE = ".assertion-inspector-view .items";
	private static final String DROPZONE = "#assertionList";
	private static final String DIALOG = ".dialog";
	private static final String CREATE = "#default";
	private static final String ASSERTION_VIEW = ".assertion-view";

	@Test
	public void shouldBeAbleToAddComponentAssertion()
	{
		//given
		doubleClick( ASSERTION_INSPECTOR );
		drag( ASSERTABLE ).to( DROPZONE );
		waitForDialogToAppear();

		//when
		click( "#sent" ).click( "#total" );
		click( "#min" ).type( "1" );
		click( "#max" ).type( "10000" );

		click( CREATE );

		//Then
		assert ( exists( ASSERTION_VIEW ) );
	}

	@Override
	public SimpleWebTestState getStartingState()
	{
		return SimpleWebTestState.STATE;
	}

	private void waitForDialogToAppear()
	{
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				Set<Node> nodes = GuiTest.findAll( ".dialog" );
				boolean dialogOpen = nodes.size() == 1;
				return dialogOpen;
			}
		}, 2 );
		assert ( exists( DIALOG ) );
	}


}
