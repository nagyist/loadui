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

import com.eviware.loadui.test.categories.IntegrationTest;
import com.google.common.base.Predicate;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.loadui.testfx.GuiTest;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.WEB_PAGE_RUNNER;
import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
import static com.google.common.collect.Collections2.filter;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author renato
 */
@Category( IntegrationTest.class )
public class ExecutionTest extends SimpleWebTestBase
{

	static final String NON_RESPONDING_VALID_IP_ADDRESS = "111.111.111.1";

	@Test
	public void canRunWebRunnerAndAbortExecution() throws Exception
	{

		setWebPageRunnerUrlTo( NON_RESPONDING_VALID_IP_ADDRESS );

		// WHEN
		runTestFor( 2, SECONDS );

		// THEN
		assertEquals( 1, extraStages().size() );

		// WHEN
		clickOnAbortButton();

		// THEN
		waitOrTimeout( new IsCanvasRunning( getProjectItem(), false ), timeout( seconds( 2 ) ) );
		assertThat( numberOfAbortedRequests(), greaterThan( 1 ) );

	}

	private List<Stage> extraStages()
	{
		return ( List<Stage> )GuiTest.find( ".canvas-object-view" ).getScene().getRoot().getProperties()
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
