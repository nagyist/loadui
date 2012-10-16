package com.eviware.loadui.ui.fx.control;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.scene.SceneBuilder;
import javafx.stage.Stage;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.GUITest;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class OptionsSliderTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();

	private static OptionsSlider optionsSlider;
	private static Stage stage;
	private static TestFX controller;

	public static class OptionsSliderTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			optionsSlider = new TextOptionsSlider( ImmutableList.of( "one", "two", "three" ) );

			primaryStage.titleProperty().bind( optionsSlider.selectedProperty() );

			primaryStage.setScene( SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
					.width( 300 ).height( 200 ).root( optionsSlider ).build() );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = TestFX.wrap( new FXScreenController() );
		FXTestUtils.launchApp( OptionsSliderTestApp.class );
		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		FXTestUtils.bringToFront( stage );
	}

	@Test
	public void property_should_updateOnClick()
	{
		assertTrue( stage.getTitle().equals( "one" ) );
		controller.click( "#two" );
		assertTrue( stage.getTitle().equals( "two" ) );
		controller.click( "#three" );
		assertTrue( stage.getTitle().equals( "three" ) );
		controller.click( "#two" );
		assertTrue( stage.getTitle().equals( "two" ) );
		controller.click( "#one" );
		assertTrue( stage.getTitle().equals( "one" ) );
	}
}
