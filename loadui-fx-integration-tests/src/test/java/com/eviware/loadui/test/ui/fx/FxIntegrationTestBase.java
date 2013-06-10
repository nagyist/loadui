package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.test.TestState;
import org.junit.After;
import org.junit.Before;

public abstract class FxIntegrationTestBase extends FxIntegrationBase
{
	public abstract TestState getStartingState();

	@Before
	public void setup() throws Exception
	{
		getStartingState().enter();
	}

	@After
	public void teardown() throws Exception
	{
		getStartingState().getParent().enter();
	}
}
