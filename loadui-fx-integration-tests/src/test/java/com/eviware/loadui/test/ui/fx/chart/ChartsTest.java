/*
 * Copyright 2011 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.test.ui.fx.chart;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.SimpleWebTestState;
import com.eviware.loadui.util.LoadUIComponents;
import com.eviware.loadui.util.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.eviware.loadui.test.ui.fx.chart.ChartTestSupport.allChartLines;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author henrik.olsson
 */

@Category(IntegrationTest.class)
public class ChartsTest extends FxIntegrationTestBase
{
	@Test
	public void shouldHaveTwoLines()
	{
		runTestFor( 5, SECONDS );

		click( "#statsTab" );
		drag( ".analysis-view " + getCssID() ).by( 150, 150 ).drop().sleep( 1000 );
		click( "#default" );

		assertThat( allChartLines().size(), is( 2 ) );
	}

	private String getCssID()
	{
		return "#" + StringUtils.toCssName( LoadUIComponents.HTTP_RUNNER.defaultComponentLabel() );
	}

	@Test
	public void statisticTree_should_acceptDoubleClicks()
	{
		runTestFor( 5, SECONDS );

		click( "#statsTab" );
		drag( ".analysis-view " + getCssID() ).by( 150, 150 ).drop().sleep( 1000 );
		doubleClick( "Max" );

		assertThat( allChartLines().size(), is( 1 ) );
	}

	@After
	public void removeChart()
	{
		click( "Chart 1" ).click( "Delete" ).click( "#default" );
	}

	@Override
	public TestState getStartingState()
	{
		return SimpleWebTestState.STATE;
	}

}
