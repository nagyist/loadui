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
package com.eviware.loadui.test.ui.fx.chart;

import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import com.eviware.loadui.test.ui.fx.states.LastResultOpenedState;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static javafx.scene.input.KeyCode.ENTER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.FXTestUtils.getOrFail;
import static org.loadui.testfx.Matchers.hasLabel;
import static org.loadui.testfx.matchers.VisibleNodesMatcher.visible;

@Category(IntegrationTest.class)
public class StatisticTabsTest extends FxIntegrationTestBase
{
	private StatisticPages pages;

	@Test
	public void testTabs()
	{
		pages = getProjectItem().getStatisticPages();
		assertThat( pageCount(), is( 1 ) );

		click( "#plus-button" ).click( "Untitled Page 2" ).click( "#untitled-page-1" ).click( "#plus-button" );
		assertThat( pageCount(), is( 3 ) );

		click( "#untitled-page-2" ).click( "#untitled-page-2 .tab-close-button" ).click( "#plus-button" )
				.click( "#untitled-page-1" ).click( "#untitled-page-1 .tab-close-button" );
		assertThat( pageCount(), is( 2 ) );

		assertEquals( "Untitled Page 3", pages.getChildAt( 0 ).getLabel() );
		assertEquals( "Untitled Page 4", pages.getChildAt( 1 ).getLabel() );

		// the tests below are placed here to avoid having to find out the ID of the tabs currently being shown,
		// which could change depending on which tests have run first

		// test tab can be renamed
		rightClick( "#untitled-page-3" ).click( "#tab-rename" ).type( "tabnewname" )
				.type( ENTER ).sleep( 250 );

		// tab ID cannot be changed
		Node tabPaneHeaderSkin = getOrFail( "#untitled-page-3" );
		Label label = ( Label )tabPaneHeaderSkin.lookup( "Label" );
		assertThat( label, hasLabel( "tabnewname" ) );
		assertEquals( "tabnewname", pages.getChildAt( 0 ).getLabel() );

		// test tab can be closed through the menu
		rightClick( "#untitled-page-4" ).click( "#tab-delete" );
		waitUntil( "#untitled-page-4", is( not( visible() ) ) );

		assertNodeExists( "#untitled-page-3" );
		assertThat( pageCount(), is( 1 ) );

		assertEquals( "tabnewname", pages.getChildAt( 0 ).getLabel() );
	}

	private int pageCount()
	{
		return pages.getChildCount();
	}

	@Override
	public TestState getStartingState()
	{
		return LastResultOpenedState.STATE;
	}
}
