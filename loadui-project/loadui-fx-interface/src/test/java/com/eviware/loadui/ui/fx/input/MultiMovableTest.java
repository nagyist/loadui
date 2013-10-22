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
package com.eviware.loadui.ui.fx.input;

import com.eviware.loadui.ui.fx.api.input.Selectable;
import com.google.common.util.concurrent.SettableFuture;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.loadui.testfx.FXScreenController;
import org.loadui.testfx.FXTestUtils;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.MouseMotion;
import org.loadui.testfx.categories.TestFX;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.loadui.testfx.GuiTest.offset;

@Category( TestFX.class )
public class MultiMovableTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static Selectable selectable1;
	private static Selectable selectable2;
	private static Selectable selectable3;
	private static Stage stage;
	private static GuiTest controller;
	private static Pane background;

	public static class SelectableTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			Rectangle rect1 = RectangleBuilder.create().id( "rect1" ).width( 25 ).height( 25 ).fill( Color.BLUE ).build();
			MovableImpl.install( rect1 );

			Rectangle rect2 = RectangleBuilder.create().id( "rect2" ).width( 50 ).height( 50 ).layoutX( 100 )
					.layoutY( 100 ).build();
			MovableImpl.install( rect2 );

			StackPane stack = StackPaneBuilder.create().id( "stack" ).minHeight( 25 ).minWidth( 25 ).layoutY( 180 )
					.build();
			Rectangle rect3 = RectangleBuilder.create().width( 25 ).height( 25 ).fill( Color.DARKSLATEBLUE ).build();
			VBox handle = VBoxBuilder.create().children( rect3 ).build();
			stack.getChildren().add( handle );
			MovableImpl.install( stack, handle );

			background = PaneBuilder.create().children( stack, rect2, rect1 ).build();

			SelectableImpl.installDragToSelectArea( background );
			selectable1 = SelectableImpl.installSelectable( rect1 );
			selectable2 = SelectableImpl.installSelectable( rect2 );
			selectable3 = SelectableImpl.installSelectable( stack );

			rect1.fillProperty().bind(
					Bindings.when( selectable1.selectedProperty() ).then( Color.GREEN ).otherwise( Color.GREY ) );

			rect2.fillProperty().bind(
					Bindings.when( selectable2.selectedProperty() ).then( Color.GREEN ).otherwise( Color.GREY ) );

			primaryStage.setScene( SceneBuilder.create().width( 300 ).height( 300 ).root( background ).build() );

			MultiMovable.install( background, rect1 );
			MultiMovable.install( background, rect2 );
			MultiMovable.install( background, stack );

			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		controller = GuiTest.wrap( new FXScreenController() );
		FXTestUtils.launchApp( SelectableTestApp.class );
		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		GuiTest.targetWindow( stage );
		FXTestUtils.bringToFront( stage );
	}

	@After
	public void restorePosition()
	{
		final Node rectangle1 = selectable1.getNode();
		final Node rectangle2 = selectable2.getNode();
		selectable1.deselect();
		selectable2.deselect();
		selectable3.deselect();
		rectangle1.setLayoutX( 0 );
		rectangle1.setLayoutY( 0 );
		rectangle2.setLayoutX( 100 );
		rectangle2.setLayoutY( 100 );
		FXTestUtils.awaitEvents();
	}

	@Test
	public void movingSelectedNode_should_moveAlongAllOtherSelectedNodes() throws Throwable
	{
		final Node rectangle1 = selectable1.getNode();
		final Node rectangle2 = selectable2.getNode();

		controller.drag( offset( background, 220, 170 ) ).to( offset( background, 5, 5 ) );
		assertThat( selectable1.isSelected(), is( true ) );
		assertThat( selectable2.isSelected(), is( true ) );

		MouseMotion motion = controller.drag( rectangle1 ).by( 100, 20 );
		assertThat( selectable1.isSelected(), is( true ) );
		assertThat( selectable2.isSelected(), is( true ) );

		motion.drop().click( background );
		assertThat( selectable1.isSelected(), is( false ) );
		assertThat( selectable2.isSelected(), is( false ) );
		assertThat( rectangle1.getLayoutX(), equalTo( 100.0 ) );
		assertThat( rectangle1.getLayoutY(), equalTo( 20.0 ) );
		assertThat( rectangle2.getLayoutX(), equalTo( 200.0 ) );
		assertThat( rectangle2.getLayoutY(), equalTo( 120.0 ) );
	}

	@Test
	public void movingUnselectedNode_shouldNot_moveAlongAllSelectedNodes() throws Throwable
	{
		final Node rectangle1 = selectable1.getNode();
		final Node rectangle2 = selectable2.getNode();

		controller.click( rectangle1 );
		assertThat( selectable1.isSelected(), is( true ) );
		assertThat( selectable2.isSelected(), is( false ) );

		MouseMotion motion = controller.drag( rectangle2 ).by( 100, 20 );
		assertThat( selectable1.isSelected(), is( false ) );
		assertThat( selectable2.isSelected(), is( true ) );

		motion.drop();
		assertThat( selectable1.isSelected(), is( false ) );
		assertThat( selectable2.isSelected(), is( true ) );
		assertThat( rectangle1.getLayoutX(), equalTo( 0.0 ) );
		assertThat( rectangle1.getLayoutY(), equalTo( 0.0 ) );
		assertThat( rectangle2.getLayoutX(), equalTo( 200.0 ) );
		assertThat( rectangle2.getLayoutY(), equalTo( 120.0 ) );
	}

	@Test
	public void clickingOnAMovablesHandle_should_selectThatNode() throws Throwable
	{
		final Node stack = selectable3.getNode();

		controller.click( stack );
		assertThat( selectable3.isSelected(), is( true ) );

	}

}
