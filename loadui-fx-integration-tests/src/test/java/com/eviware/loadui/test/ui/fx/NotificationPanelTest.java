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
package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.testevents.MessageLevel;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.OpenSourceFxLoadedState;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.TestUtils;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.loadui.testfx.GuiTest;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.FXTestUtils.getOrFail;
import static org.loadui.testfx.Matchers.hasLabel;

@Category( IntegrationTest.class )
public class NotificationPanelTest extends FxIntegrationTestBase
{
	@Override
	public TestState getStartingState()
	{
		return OpenSourceFxLoadedState.STATE;
	}

	@Before
	public void start() throws Exception
	{
		ensureNotificationPanelIsNotVisible();
		ensureInspectorViewIsClosed();
	}

	@After
	public void cleanup() throws Exception
	{
		ensureNotificationPanelIsNotVisible();
		ensureInspectorViewIsClosed();
	}

	@Test
	public void notificationShowsUpInWorkspaceView() throws Exception
	{
		Node panelNode = notificationPanel();

		assertFalse( panelNode.isVisible() );

		sendMsgToNotificationPanel( "A message" );

		waitUntilVisible( panelNode );

		assertNodeExists( "#notification-text" );
		assertNodeExists( hasLabel( "A message" ) );
	}

	@Test
	public void notificationPanelGoesAwayWhenClickingOnHideButton() throws Exception
	{
		Node panelNode = notificationPanel();
		sendMsgToNotificationPanel( "A message" );
		waitUntilVisible( panelNode );

		click( "#hide-notification-panel" );

		waitUntilNotVisible( panelNode );
		assertFalse( panelNode.isVisible() );
	}

	@Test
	public void notificationDoesNotChangeWithMultipleQuickMessages() throws Exception
	{
		Node panelNode = notificationPanel();

		sendMsgToNotificationPanel( "A message" );

		sleep( 500 );

		sendMsgToNotificationPanel( "Second message" );

		waitUntilVisible( panelNode );

		Set<Node> textNodes = GuiTest.findAll( "#notification-text", panelNode );
		Set<Node> msgCountNodes = GuiTest.findAll( "#msgCount", panelNode );

		assertFalse( textNodes.isEmpty() );
		assertTrue( textNodes.iterator().next() instanceof Label );
		assertFalse( msgCountNodes.isEmpty() );
		assertTrue( msgCountNodes.iterator().next() instanceof Label );

		final Label msgLabel = ( Label )textNodes.iterator().next();
		Label msgCountLabel = ( Label )msgCountNodes.iterator().next();

		waitUntil( msgLabel, hasLabel( "A message" ) );
		waitUntil( msgCountLabel, hasLabel( "1" ) );

		sendMsgToNotificationPanel( "Second message" );

		waitUntil( msgLabel, hasLabel( "A message" ) );
		waitUntil( msgCountLabel, hasLabel( "2" ) );
	}

	@Test
	public void notificationShowsUpInProjectView() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();

		Node panelNode = notificationPanel();

		assertFalse( panelNode.isVisible() );

		sendMsgToNotificationPanel( "A message" );

		waitUntilVisible( panelNode );

		Set<Node> textNodes = GuiTest.findAll( "#notification-text", panelNode );

		assertFalse( textNodes.isEmpty() );
		assertTrue( textNodes.iterator().next() instanceof Label );
		assertEquals( "A message", ( ( Label )textNodes.iterator().next() ).getText() );
	}

	@Test
	@Ignore(value = "Feature disabled until further notice. See LOADUI-869 and LOADUI-871")
	public void notificationShowsUpInDetachedTab() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();

		Node panelNode = notificationPanel();

		click( "#statsTab" ).click( "#statsTab .graphic" );

		class DetachedAnalysisViewHolder
		{
			Node content;
		}
		final DetachedAnalysisViewHolder detachedHolder = new DetachedAnalysisViewHolder();
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				Set<Node> nodes = GuiTest.findAll( ".detached-content" );
				boolean ok = nodes.size() == 1;
				if( ok )
					detachedHolder.content = nodes.iterator().next();
				return ok;
			}
		}, 2 );

		assertNotNull( detachedHolder.content );

		Node clonedPanelNode = detachedHolder.content.lookup( ".notification-panel" );

		assertNotNull( clonedPanelNode );
		assertFalse( panelNode.isVisible() );
		assertFalse( clonedPanelNode.isVisible() );

		move( 200, 200 );

		sendMsgToNotificationPanel( "A message" );

		waitUntilVisible( panelNode );

		Set<Node> textNodes = GuiTest.findAll( "#notification-text", panelNode );

		assertFalse( textNodes.isEmpty() );
		assertTrue( textNodes.iterator().next() instanceof Label );
		assertEquals( "A message", ( ( Label )textNodes.iterator().next() ).getText() );

		assertTrue( clonedPanelNode.isVisible() );
		textNodes = GuiTest.findAll( "#notification-text", clonedPanelNode );

		assertFalse( textNodes.isEmpty() );
		assertTrue( textNodes.iterator().next() instanceof Label );
		assertEquals( "A message", ( ( Label )textNodes.iterator().next() ).getText() );

		click( "#hide-notification-panel" );

		waitUntilNotVisible( panelNode );

		assertFalse( panelNode.isVisible() );
		assertFalse( clonedPanelNode.isVisible() );
		closeCurrentWindow();

	}

	@Test
	public void inspectorViewIsShownWhenClickingOnButton() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();

		Node panelNode = notificationPanel();

		assertFalse( isInspectorViewOpen() );

		sendMsgToNotificationPanel( "A message" );
		waitUntilVisible( panelNode );
		click( "#show-system-log" );

		assertTrue( isInspectorViewOpen() );
	}

	@Test
	public void notificationPanelWontGoAwayIfMouseIsOnIt() throws Exception
	{
		Node panelNode = notificationPanel();

		sendMsgToNotificationPanel( "A message" );

		waitUntilVisible( panelNode );

		// find position of notification panel, close it, then put mouse just below it
		move( "#hide-notification-panel" ).click().moveBy( 0, 150 );

		waitUntilNotVisible( panelNode );

		sendMsgToNotificationPanel( "A message" );

		waitUntilVisible( panelNode );

		// put mouse on notification panel and stay there for a while
		move( "#hide-notification-panel" ).sleep( 5000 );
		assertTrue( panelNode.isVisible() );

		// if moving out and going back quickly, panel should still be visible
		moveBy( 0, 150 ).moveBy( 0, -150 ).sleep( 1000 );
		assertTrue( panelNode.isVisible() );

		// now go away and let the panel vanish
		moveBy( 150, 0 );

		waitUntilNotVisible( panelNode );

		assertFalse( panelNode.isVisible() );
	}

	private Node notificationPanel()
	{
		waitForNode( ".notification-panel" );
		return getOrFail( ".notification-panel" );
	}

	private void sendMsgToNotificationPanel( String msg )
	{
		try
		{
			BeanInjector.getBeanFuture( TestEventManager.class ).get( 500, TimeUnit.MILLISECONDS )
					.logMessage( MessageLevel.WARNING, msg );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	private void waitUntilVisible( final Node panelNode ) throws Exception
	{
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return panelNode.isVisible() && Double.compare( panelNode.getOpacity(), 1.0 ) == 0;
			}
		}, 2 );
	}

	private void waitUntilNotVisible( final Node panelNode ) throws Exception
	{
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return !panelNode.isVisible();
			}
		}, 1 );
	}

}
