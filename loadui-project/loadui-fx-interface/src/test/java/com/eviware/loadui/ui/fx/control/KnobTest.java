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
package com.eviware.loadui.ui.fx.control;

import javafx.scene.GroupBuilder;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBoxBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.categories.TestFX;

import static javafx.geometry.VerticalDirection.DOWN;
import static javafx.geometry.VerticalDirection.UP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.loadui.testfx.Assertions.verifyThat;

@Category( TestFX.class )
public class KnobTest extends GuiTest
{
	@BeforeClass
	public static void createStage() throws Throwable
	{
		showNodeInStage( setupRootNode(), "/com/eviware/loadui/ui/fx/loadui-style.css" );
	}

	private static Parent setupRootNode()
	{
		Knob boundedKnob = new Knob( "Bounded", 0, 10, 0 );
		boundedKnob.setId( "bounded" );
		Knob minKnob = new Knob( "Min" );
		minKnob.setMin( 0 );
		minKnob.setId( "min" );
		Knob maxKnob = new Knob( "Max" );
		maxKnob.setMax( 0 );
		maxKnob.setId( "max" );
		return GroupBuilder.create()
				.children( HBoxBuilder.create().children( boundedKnob, minKnob, maxKnob ).build() ).build();
	}

	@Before
	public void setup()
	{
		( ( Knob )find( "#bounded" ) ).setValue( 0.0 );
		( ( Knob )find( "#min" ) ).setValue( 0.0 );
		( ( Knob )find( "#max" ) ).setValue( 0.0 );
	}

	@Test
	public void onlyBoundedShouldHaveBoundedStyleClass()
	{
		assertThat( find( "#bounded" ).getStyleClass().contains( "bounded" ), is( true ) );
		assertThat( find( "#min" ).getStyleClass().contains( "bounded" ), is( false ) );
		assertThat( find( "#max" ).getStyleClass().contains( "bounded" ), is( false ) );
	}

	@Test
	public void shouldBeModifiableByDragging()
	{
		Knob bounded = find( "#bounded" );
		//Drag down first to initiate dragging.
		drag( bounded ).by( 0, 10 ).by( 0, -5 ).drop().sleep( 100 );
		verifyThat( bounded.getValue(), closeTo( 5.0, 0.01 ) );
	}

	@Test
	public void shouldBeModifiableByManualEntry()
	{
		Knob bounded = find( "#bounded" );
		doubleClick( bounded ).sleep( 100 ).type( "5" ).push( KeyCode.ENTER );
		assertThat( bounded.getValue(), closeTo( 5.0, 0.01 ) );
	}

	@Test
	public void shouldBeModifiableByScrolling()
	{
		Knob bounded = find( "#bounded" );
		move( bounded );
		scroll( 10, DOWN );
		assertThat( bounded.getValue(), closeTo( 0.0, 0.01 ) );

		scroll( 10, UP );
		assertThat( bounded.getValue(), closeTo( 10.0, 0.01 ) );

		Knob minKnob = find( "#min" );
		move( minKnob );
		scroll( 10, DOWN );
		assertThat( minKnob.getValue(), closeTo( 0.0, 0.01 ) );

		scroll( 11, UP );
		assertThat( minKnob.getValue(), greaterThan( 10.0 ) );

		Knob maxKnob = find( "#max" );
		move( maxKnob );
		scroll( 10, UP );
		assertThat( maxKnob.getValue(), closeTo( 0.0, 0.01 ) );

		scroll( 11, DOWN );
		assertThat( maxKnob.getValue(), lessThan( -10.0 ) );
	}
}
