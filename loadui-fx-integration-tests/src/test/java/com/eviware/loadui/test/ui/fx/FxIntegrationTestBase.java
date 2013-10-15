package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.test.TestState;
import javafx.scene.Node;
import org.junit.After;
import org.junit.Before;

import static org.loadui.testfx.FXTestUtils.getOrFail;

public abstract class FxIntegrationTestBase extends FxIntegrationBase
{
	public abstract TestState getStartingState();

	@Before
	public void setup() throws Exception
	{
		getStartingState().enter();
		Node notificationPanel = getOrFail( ".notification-panel" );
		if( notificationPanel.isVisible() )
			click( "#hide-notification-panel" );
	}

	@After
	public void teardown() throws Exception
	{
		getStartingState().getParent().enter();
	}

}
