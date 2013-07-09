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
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.FXAppLoadedState;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.TestUtils;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.eviware.loadui.ui.fx.util.test.FXTestUtils.getOrFail;
import static org.junit.Assert.*;

@Category( IntegrationTest.class )
public class NotificationPanelTest
{

	private static TestFX controller;

	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();
		controller = GUI.getController();
	}

	@AfterClass
	public static void cleanup()
	{
		controller = null;
		ProjectLoadedWithoutAgentsState.STATE.getParent().enter();
	}

	@Before
	public void onStart() throws Exception
	{
		try
		{
			waitUntilNotVisible( notificationPanel() );
		}
		catch( TimeoutException te )
		{
			controller.click( hideNotificationPanelButton() );
			waitUntilNotVisible( notificationPanel() );
		}
	}

	@Test
	public void notificationShowsUpInWorkspaceView() throws Exception
	{
		FXAppLoadedState.STATE.enter();
		Node panelNode = notificationPanel();

		assertFalse( panelNode.isVisible() );

		sendMsgToNotificationPanel( "A message" );

		waitUntilVisible( panelNode );

		assertTrue( panelNode.isVisible() );
		Set<Node> textNodes = TestFX.findAll( "#notification-text", panelNode );

		assertFalse( textNodes.isEmpty() );
		assertTrue( textNodes.iterator().next() instanceof Label );
		assertEquals( "A message", ( ( Label )textNodes.iterator().next() ).getText() );
		controller.click( hideNotificationPanelButton() );

		waitUntilNotVisible( panelNode );

		assertFalse( panelNode.isVisible() );

	}

	@Test
	public void notificationDoesNotChangeWithMultipleQuickMessages() throws Exception
	{
		FXAppLoadedState.STATE.enter();
		Node panelNode = notificationPanel();

		sendMsgToNotificationPanel( "A message" );

		controller.sleep( 500 );

		sendMsgToNotificationPanel( "Second message" );

		waitUntilVisible( panelNode );

		assertTrue( panelNode.isVisible() );
		Set<Node> textNodes = TestFX.findAll( "#notification-text", panelNode );
		Set<Node> msgCountNodes = TestFX.findAll( "#msgCount", panelNode );

		assertFalse( textNodes.isEmpty() );
		assertTrue( textNodes.iterator().next() instanceof Label );
		assertFalse( msgCountNodes.isEmpty() );
		assertTrue( msgCountNodes.iterator().next() instanceof Label );

		final Label msgLabel = ( Label )textNodes.iterator().next();
		Label msgCountLabel = ( Label )msgCountNodes.iterator().next();

		assertEquals( "A message", msgLabel.getText() );
		assertEquals( "1", msgCountLabel.getText() );

		sendMsgToNotificationPanel( "Second message" );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return "A message".equals( msgLabel.getText() );
			}
		}, 1000 );

		assertEquals( "A message", msgLabel.getText() );
		assertEquals( "2", msgCountLabel.getText() );
		controller.click( hideNotificationPanelButton() );

	}

	@Test
	public void notificationShowsUpInProjectView() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();

		Node panelNode = notificationPanel();

		assertFalse( panelNode.isVisible() );

		sendMsgToNotificationPanel( "A message" );

		waitUntilVisible( panelNode );

		assertTrue( panelNode.isVisible() );
		Set<Node> textNodes = TestFX.findAll( "#notification-text", panelNode );

		assertFalse( textNodes.isEmpty() );
		assertTrue( textNodes.iterator().next() instanceof Label );
		assertEquals( "A message", ( ( Label )textNodes.iterator().next() ).getText() );

		controller.click( hideNotificationPanelButton() );

		waitUntilNotVisible( panelNode );

		assertFalse( panelNode.isVisible() );

	}

	@Test
	public void notificationShowsUpInDetachedTab() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();

		Node panelNode = notificationPanel();

		controller.click( "#statsTab" ).click( "#statsTab .graphic" );

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
				Set<Node> nodes = TestFX.findAll( ".detached-content" );
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

		controller.move( 200, 200 );

		sendMsgToNotificationPanel( "A message" );

		waitUntilVisible( panelNode );

		assertTrue( panelNode.isVisible() );
		Set<Node> textNodes = TestFX.findAll( "#notification-text", panelNode );

		assertFalse( textNodes.isEmpty() );
		assertTrue( textNodes.iterator().next() instanceof Label );
		assertEquals( "A message", ( ( Label )textNodes.iterator().next() ).getText() );

		assertTrue( clonedPanelNode.isVisible() );
		textNodes = TestFX.findAll( "#notification-text", clonedPanelNode );

		assertFalse( textNodes.isEmpty() );
		assertTrue( textNodes.iterator().next() instanceof Label );
		assertEquals( "A message", ( ( Label )textNodes.iterator().next() ).getText() );

		controller.click( hideNotificationPanelButton() );

		waitUntilNotVisible( panelNode );

		assertFalse( panelNode.isVisible() );
		assertFalse( clonedPanelNode.isVisible() );
		controller.closeCurrentWindow();

	}

	@Test
	public void inspectorViewIsShownWhenClickingOnButton() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();

		Node panelNode = notificationPanel();

		final Node inspectorView = getOrFail( ".inspector-view" );

		// inspector view is closed
		assertTrue( ( ( Region )inspectorView ).getHeight() < 50 );

		sendMsgToNotificationPanel( "A message" );

		waitUntilVisible( panelNode );

		controller.click( getOrFail( "#show-system-log" ) );

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return ( ( Region )inspectorView ).getHeight() > 150;
			}
		}, 2000 );

		// inspector view is opened!
		assertTrue( ( ( Region )inspectorView ).getHeight() > 150 );

		// hide the inspector view so it won't break other tests
		controller.move( "#Assertions" ).moveBy( 400, 0 ).doubleClick();

		controller.click( hideNotificationPanelButton() );

		waitUntilNotVisible( panelNode );

		assertFalse( panelNode.isVisible() );
	}

	@Test
	public void notificationPanelWontGoAwayIfMouseIsOnIt() throws Exception
	{
		FXAppLoadedState.STATE.enter();

		Node panelNode = notificationPanel();

		sendMsgToNotificationPanel( "A message" );

		waitUntilVisible( panelNode );

		// find position of notification panel, close it, then put mouse just below it
		controller.move( hideNotificationPanelButton() ).click().moveBy( 0, 150 );

		waitUntilNotVisible( panelNode );

		sendMsgToNotificationPanel( "A message" );

		waitUntilVisible( panelNode );

		// put mouse on notification panel and stay there for a while
		controller.move( hideNotificationPanelButton() ).sleep( 5000 );
		assertTrue( panelNode.isVisible() );

		// if moving out and going back quickly, panel should still be visible
		controller.moveBy( 0, 150 ).moveBy( 0, -150 ).sleep( 1000 );
		assertTrue( panelNode.isVisible() );

		// now go away and let the panel vanish
		controller.moveBy( 0, 150 );

		waitUntilNotVisible( panelNode );

		assertFalse( panelNode.isVisible() );
	}

	private Node notificationPanel()
	{
		return getOrFail( ".notification-panel" );
	}

	private Node hideNotificationPanelButton()
	{
		return getOrFail( "#hide-notification-panel" );
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