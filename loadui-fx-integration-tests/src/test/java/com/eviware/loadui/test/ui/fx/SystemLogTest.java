package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import org.loadui.testfx.GuiTest;
import javafx.scene.control.ListView;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.Matchers.hasLabel;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class SystemLogTest extends GuiTest
{
	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();
	}

	@Test
	public void should_displayLogMessages()
	{
		click( "System Log" ).drag(".inspector-view .tab-header-background").by(0, -400)
				.drop();

		click( "Clear").click( "Copy All" );

		assertNodeExists( hasLabel( containsString( "Copied all rows to system clipboard" ) ) );

		doubleClick( "System Log" );
	}

	@Test
	public void should_limitNumberOfRows()
	{
		doubleClick( "System Log" );

		generateALotOfMessages();

		ListView<?> systemLog = find( ".system-log" );
		assertThat( systemLog.getItems().size(), is(200) );

		doubleClick( "System Log" );
	}

	private void generateALotOfMessages()
	{
		for( int i=0; i<210; i++ )
		{
			click( "Copy All" );
		}
	}
}