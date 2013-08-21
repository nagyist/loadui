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
package com.eviware.loadui.ui.fx.views.result;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import org.loadui.testfx.categories.TestFX;
import org.loadui.testfx.FXTestUtils;
import org.loadui.testfx.GuiTest;
import com.google.common.base.Predicate;
import com.google.common.util.concurrent.SettableFuture;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.SceneBuilder;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.Closeable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.eviware.loadui.ui.fx.util.ObservableLists.*;
import static org.loadui.testfx.matchers.ContainsNodesMatcher.contains;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@Category( TestFX.class )
public class ResultViewTest extends GuiTest
{
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static Stage stage;
	private static ResultView view;

	static Execution res0;
	static Execution res1;
	static Execution arch0;
	static Execution curr;
	static ExecutionManager firer;

	public static class ResultViewTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{

			System.out.println( "Setting up app" );
			primaryStage.setWidth( 1000 );
			firer = mock( ExecutionManager.class );

			res0 = mock( Execution.class );
			when( res0.getId() ).thenReturn( "0" );
			when( res0.getLabel() ).thenReturn( "Ex 1" );
			when( res0.isArchived() ).thenReturn( false );

			res1 = mock( Execution.class );
			when( res1.getId() ).thenReturn( "1" );
			when( res1.getLabel() ).thenReturn( "Ex 2" );
			when( res1.isArchived() ).thenReturn( false );

			arch0 = mock( Execution.class );
			when( arch0.getId() ).thenReturn( "2" );
			when( arch0.getLabel() ).thenReturn( "Ex 3" );
			when( arch0.isArchived() ).thenReturn( true );

			List<Execution> executions = Arrays.asList( res0, res1, arch0 );

			curr = mock( Execution.class );
			when( curr.getId() ).thenReturn( "3" );
			when( curr.getLabel() ).thenReturn( "Current" );
			when( curr.isArchived() ).thenReturn( false );

			ObservableList<Execution> recentExecutions = fx( filter(
					ofCollection( firer, ExecutionManager.RECENT_EXECUTIONS, Execution.class, executions ),
					new Predicate<Execution>()
					{
						@Override
						public boolean apply( Execution input )
						{
							return !input.isArchived();
						}
					} ) );
			ObservableList<Execution> archivedExecutions = fx( filter(
					ofCollection( firer, ExecutionManager.RECENT_EXECUTIONS, Execution.class, executions ),
					new Predicate<Execution>()
					{
						@Override
						public boolean apply( Execution input )
						{
							return input.isArchived();
						}
					} ) );

			Closeable closeable = mock( Closeable.class );

			view = new ResultView( recentExecutions, archivedExecutions, closeable );

			primaryStage.setScene( SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" )
					.width( 600 ).height( 600 ).root( view ).build() );
			primaryStage.show();
			stageFuture.set( primaryStage );
		}

	}

	@Before
	public void createWindow() throws Throwable
	{
		FXTestUtils.launchApp( ResultViewTestApp.class );
		stage = targetWindow( stageFuture.get( 5, TimeUnit.SECONDS ) );
		FXTestUtils.bringToFront( stage );

	}

	@Test
	public void ensureExecutionsInRightPlaceAndCanDragToArchive() throws Exception
	{
		assertThat( "#result-node-list", contains( 2, ".execution-view" ) );
		assertThat( "#result-node-list", contains( "#result-0" ) );
		assertThat( "#result-node-list", contains( "#result-1" ) );

		assertThat( "#archive-node-list", contains( 1, ".execution-view" ) );
		assertThat( "#archive-node-list", contains( "#archive-0" ) );

		drag( "#result-0" ).to( "#archive-0" );

		FXTestUtils.awaitEvents();

		verify( res0 ).archive();
	}

}
