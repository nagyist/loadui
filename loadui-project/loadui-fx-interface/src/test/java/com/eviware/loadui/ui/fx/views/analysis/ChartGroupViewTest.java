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
package com.eviware.loadui.ui.fx.views.analysis;

import static org.loadui.testfx.GuiTest.targetWindow;
import static org.loadui.testfx.GuiTest.wrap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.loadui.testfx.categories.TestFX;
import org.loadui.testfx.FXScreenController;
import org.loadui.testfx.FXTestUtils;
import org.loadui.testfx.GuiTest;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.statistics.model.Chart.Owner;
import com.eviware.loadui.config.PropertyConfig;
import com.eviware.loadui.config.PropertyListConfig;
import com.eviware.loadui.config.impl.ChartConfigImpl;
import com.eviware.loadui.impl.statistics.model.ChartGroupImpl;
import com.eviware.loadui.util.test.BeanInjectorMocker;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.experimental.categories.Category;

@Ignore
@Category( TestFX.class )
public class ChartGroupViewTest
{

	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static GuiTest controller;

	public static class TestApp extends Application
	{

		@Override
		public void start( Stage stage ) throws Exception
		{
			//FIXME this test should be implemented some time but Mockito is playing up and getting into an infinite recursion
			System.out.println("Mocking owner");
			Owner owner = mock( Owner.class );

			AddressableRegistry mockRegistry = mock( AddressableRegistry.class );
			when( mockRegistry.lookup( "SH" ) ).thenReturn( owner );

			System.out.println("Mocking BeanInjector");
			BeanInjectorMocker injector = BeanInjectorMocker.newInstance();
			injector.put( AddressableRegistry.class, mockRegistry );

			List<PropertyConfig> propertyConfigs = Lists.newArrayList( mock( PropertyConfig.class ) );

			System.out.println("Mocking PropertyListConfig");
			PropertyListConfig attribs = mock( PropertyListConfig.class );
			when( attribs.getPropertyList() ).thenReturn( propertyConfigs );

			ChartGroupImpl parent = mock( ChartGroupImpl.class );
			
			ChartConfigImpl config = mock( ChartConfigImpl.class );
			//when( config.isSetStatisticHolder() ).thenReturn( true );
			//when( config.getStatisticHolder() ).thenReturn( "SH" );
			//when( config.getAttributes() ).thenReturn( attribs );

			System.out.println("Creating new Chart");
			
			//Chart chart = new ChartImpl( parent, config );
			//Collection<Chart> charts = Arrays.asList( chart );
			//ChartGroup chartGroup = mock( ChartGroup.class );
			//when( chartGroup.getChildren() ).thenReturn( charts );
			
			//ObservableValue<Execution> currentExecution = mock( ObservableValue.class );

			Observable poll = mock( Observable.class );
			System.out.println("Creating new ChartGroupView");
			//final ChartGroupView view = new ChartGroupView( chartGroup, currentExecution, poll );

			//stage.setScene( SceneBuilder.create().root( view ).build() );
			System.out.println("Showing stage");
			//stage.show();
			stageFuture.set( stage );
		}

	}

	@Test
	public void test() throws InterruptedException
	{
		//TODO implement tests
	}

	@Before
	public void createWindow() throws Throwable
	{

		controller = wrap( new FXScreenController() );
		FXTestUtils.launchApp( TestApp.class );
		Stage stage = targetWindow( stageFuture.get( 5, TimeUnit.SECONDS ) );
		FXTestUtils.bringToFront( stage );

	}

}
