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

import org.loadui.testfx.categories.TestFX;
import org.loadui.testfx.FXTestUtils;
import org.loadui.testfx.GuiTest;
import com.eviware.loadui.ui.fx.views.canvas.component.ComponentLayoutUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.SettableFuture;
import javafx.application.Application;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBoxBuilder;
import javafx.stage.Stage;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.TimeUnit;

import static org.loadui.testfx.Matchers.hasLabel;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Category( TestFX.class )
public class OptionsSliderTest extends GuiTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();

	private static OptionsSlider optionsSlider;
	private static OptionsSlider imageOptionsSlider;
	private static Stage stage;
	private static Label label;

	public static class OptionsSliderTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			optionsSlider = new OptionsSlider( ImmutableList.of( "one", "two", "three" ) );

			imageOptionsSlider = new OptionsSlider( ImmutableList.of( "gauss", "sine" ), ImmutableList.of(
					createImage( "gauss_shape.png" ), createImage( "variance2_shape.png" ) ) );

			primaryStage.titleProperty().bind( optionsSlider.selectedProperty() );

			label = new Label( "not set" );
			label.textProperty().bind( imageOptionsSlider.selectedProperty() );

			primaryStage.setScene( SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
					.width( 300 ).height( 200 )
					.root( HBoxBuilder.create().spacing( 25 ).children( optionsSlider, imageOptionsSlider, label ).build() )
					.build() );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		FXTestUtils.launchApp( OptionsSliderTestApp.class );
		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		FXTestUtils.bringToFront( stage );
	}

	@Test
	public void property_should_updateOnClick()
	{
		click( "#two" );
		assertTrue( "two".equals( stage.getTitle() ) );
		click( "#three" );
		assertTrue( "three".equals( stage.getTitle() ) );
		click( "#two" );
		assertTrue( "two".equals( stage.getTitle() ) );
		click( "#one" );
		assertTrue( "one".equals( stage.getTitle() ) );
	}

	@Test
	public void images_should_work()
	{
		click( "#gauss" );
		assertThat( label, hasLabel( "gauss" ) );

		click( "#sine" );
		assertThat( label, hasLabel( "sine" ) );
	}

	private static ImageView createImage( String imageName )
	{
		return new ImageView( new Image( ComponentLayoutUtils.class.getClassLoader()
				.getResource( "images/options/" + imageName ).toExternalForm() ) );
	}
}
