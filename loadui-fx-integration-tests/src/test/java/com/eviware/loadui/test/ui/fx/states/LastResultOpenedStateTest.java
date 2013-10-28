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
package com.eviware.loadui.test.ui.fx.states;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import javafx.scene.control.Label;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Calendar;

import static org.junit.Assert.assertTrue;
import static org.loadui.testfx.FXTestUtils.getOrFail;

@Category(IntegrationTest.class)
public class LastResultOpenedStateTest extends FxIntegrationTestBase
{

	@After
	public void cleanup()
	{
		ensureResultViewWindowIsClosed();
	}

	@Test
	public void shouldHaveAnalysisView() throws Exception
	{
		getOrFail( ".analysis-view" );
		Label lbl = getOrFail( "#current-execution-label" );
		int year = Calendar.getInstance().get( Calendar.YEAR );
		assertTrue( lbl.getText().contains( year + "" ) ); // default label text should contain current date

	}

	@Override
	public TestState getStartingState()
	{
		return LastResultOpenedState.STATE;
	}
}
