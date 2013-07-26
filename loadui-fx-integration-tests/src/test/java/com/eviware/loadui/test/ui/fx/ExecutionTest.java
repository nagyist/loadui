/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.GuiTest;
import com.google.common.base.Predicate;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.FIXED_RATE_GENERATOR;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.WEB_PAGE_RUNNER;
import static com.eviware.loadui.ui.fx.util.test.GuiTest.find;
import static com.eviware.loadui.ui.fx.util.test.GuiTest.findAll;
import static com.google.common.collect.Collections2.filter;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

/**
 * @author renato
 */
@Category(IntegrationTest.class)
public class ExecutionTest extends FxIntegrationTestBase
{

	static final String NON_RESPONDING_VALID_IP_ADDRESS = "111.111.111.1";

	@Override
	public TestState getStartingState()
	{
		return ProjectLoadedWithoutAgentsState.STATE;
	}

	@Test
	public void canRunWebRunnerAndAbortExecution() throws Exception
	{

		// GIVEN
		ProjectItem project = ProjectLoadedWithoutAgentsState.STATE.getProject();

		connect( FIXED_RATE_GENERATOR ).to( WEB_PAGE_RUNNER );

		controller.click( webPageRunnerInput() ).type( NON_RESPONDING_VALID_IP_ADDRESS );

		// WHEN
		runTestFor( 2, SECONDS );

		// THEN
		assertTrue( project.isRunning() );
		assertEquals( 1, extraStages().size() );

		// WHEN
		clickOnAbortButton();

		// THEN
		assertFalse( project.isRunning() );
		assertThat( numberOfAbortedRequests(), greaterThan( 1 ) );

	}

	private Node webPageRunnerInput()
	{
		return find( ".text-input", robot.getComponentNode( WEB_PAGE_RUNNER ) );
	}

	private void clickOnAbortButton()
	{
		Node abortButton = extraStages().get( 0 ).getScene().lookup( "#abort-requests" );
		controller.click( abortButton ).sleep( 500 );
	}

	private List<Stage> extraStages()
	{
		return ( List<Stage> ) GuiTest.find( ".canvas-object-view" ).getScene().getRoot().getProperties()
				.get( "OTHER_STAGES" );
	}

	private int numberOfAbortedRequests()
	{
		Set<Node> allVBoxes = findAll( "VBox", robot.getComponentNode( WEB_PAGE_RUNNER ) );
		Collection<Node> discardedBoxes = filter( allVBoxes, new Predicate<Node>()
		{
			@Override
			public boolean apply( @Nullable Node input )
			{
				if( input != null && input instanceof VBox && ( ( VBox )input ).getChildren().size() == 2 )
				{
					Node firstChild = ( ( VBox )input ).getChildren().get( 0 );
					return ( firstChild instanceof Label ) &&
							( ( Label )firstChild ).getText().equals( "Discarded" );
				}
				return false;
			}
		} );

		if( discardedBoxes.size() != 1 )
			throw new RuntimeException( "Could not find the Discarded box in the Web Page Runner" );
		else
		{
			Node discardedTextNode = ( ( VBox )discardedBoxes.iterator().next() ).getChildren().get( 1 );
			return Integer.parseInt( ( ( Label )discardedTextNode ).getText() );
		}
	}

}
