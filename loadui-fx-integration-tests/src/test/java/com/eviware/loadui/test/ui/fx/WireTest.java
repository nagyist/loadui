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
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.GuiTest;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Set;
import java.util.concurrent.Callable;

import static com.eviware.loadui.ui.fx.util.test.GuiTest.findAll;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class WireTest extends GuiTest
{
	private static final Predicate<Node> CONDITION_COMPONENT = new Predicate<Node>()
	{
		@Override
		public boolean apply( Node input )
		{
			if( input.getClass().getSimpleName().equals( "ComponentDescriptorView" ) )
			{
				return input.toString().equals( "Condition" );
			}
			return false;
		}
	};

	private static GuiTest controller;

	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();

		controller = GUI.getController();

		controller.drag( "#Assertions" ).by( 0, 250 ).drop();

		System.out.println( "Create Component 1" );
		controller.click( "#flow.category .expander-button" ).drag( CONDITION_COMPONENT ).by( 100, -400 ).drop();
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return findAll( ".canvas-object-view" ).size() == 1;
			}
		} );

		System.gc();
		System.gc();
		System.gc();

		System.out.println( "Create Component 2" );
		controller.click( "#flow.category .expander-button" ).drag( CONDITION_COMPONENT ).by( 250, -100 ).drop();
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return findAll( ".canvas-object-view" ).size() == 2;
			}
		} );
	}

	@AfterClass
	public static void cleanState()
	{
		int MAX_DELETE_TRIES = 10;
		while( !findAll( ".component-view" ).isEmpty() && MAX_DELETE_TRIES-- > 0 )
		{
			controller.click( ".component-view #menu" ).click( "#delete-item" ).click( ".confirmation-dialog #default" );
		}
	}

	@Test
	public void shouldDeleteSelectedWiresByClickingOnDelete()
	{
		Set<Node> outputs = findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> inputs = findAll( ".canvas-object-view .terminal-view.input-terminal" );

		drag( Iterables.get( inputs, 0 ) ).to( Iterables.get( outputs, 2 ) );

		move( Iterables.get( inputs, 0 ) ).moveBy( 0, -25 ).click();
		assertThat( findAll( ".connection-view" ).size(), is( 1 ) );

		type( KeyCode.DELETE );
		assertThat( findAll( ".connection-view" ).size(), is( 0 ) );
	}

	@Test
	@Ignore( "LOADUI-64 - Skipped this for release 2.5 as implementation would be too time-consuming" )
	public void shouldDeleteSelectedWiresByRightClickMenu()
	{
		Set<Node> outputs = findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> inputs = findAll( ".canvas-object-view .terminal-view.input-terminal" );

		drag( Iterables.get( inputs, 0 ) ).to( Iterables.get( outputs, 2 ) );

		assertThat( findAll( ".connection-view" ).size(), is( 1 ) );

		move( Iterables.get( inputs, 0 ) ).moveBy( 0, -25 ).click(MouseButton.SECONDARY)
				.click( "#delete-wire" );

		assertThat( findAll( ".connection-view" ).size(), is( 0 ) );
	}

	@Test
	public void shouldCreateWires()
	{
		Set<Node> outputs = findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> inputs = findAll( ".canvas-object-view .terminal-view.input-terminal" );

		drag( Iterables.get( outputs, 0 ) ).to( Iterables.get( inputs, 1 ) );
		drag( Iterables.get( inputs, 0 ) ).to( Iterables.get( outputs, 2 ) );

		assertThat( findAll( ".connection-view" ).size(), is( 2 ) );
		drag( Iterables.get( outputs, 0 ) ).to( Iterables.get( outputs, 1 ) );
		assertThat( findAll( ".connection-view" ).size(), is( 2 ) );

		Node terminal = Iterables.get( inputs, 0 );
		move( terminal ).moveBy( 0, -25 ).click().drag( terminal ).by( 0, -30 ).drop();
		assertThat( findAll( ".connection-view" ).size(), is( 1 ) );

		drag( Iterables.get( outputs, 1 ) ).by( 0, 20 ).drop();
		assertThat( findAll( ".connection-view" ).size(), is( 0 ) );
	}

	@Test
	public void preventMultipleConnectionsToASingleOutputTerminal()
	{
		Set<Node> outputs = findAll( ".canvas-object-view .terminal-view.output-terminal" );
		Set<Node> inputs = findAll( ".canvas-object-view .terminal-view.input-terminal" );

		drag( Iterables.get( inputs, 0 ) ).to( Iterables.get( outputs, 0 ) );
		drag( Iterables.get( inputs, 1 ) ).to( Iterables.get( outputs, 0 ) );

		assertThat( findAll( ".connection-view" ).size(), is( 1 ) );

		drag( Iterables.get( outputs, 0 ) ).by( 0, 20 ).drop();
		assertThat( findAll( ".connection-view" ).size(), is( 0 ) );
	}
}
