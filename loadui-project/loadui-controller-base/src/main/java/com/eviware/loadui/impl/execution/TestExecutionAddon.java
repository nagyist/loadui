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
package com.eviware.loadui.impl.execution;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.testevents.MessageLevel;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.events.EventFuture;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Fires expected events during test execution, and keeps execution in sync with
 * existing events.
 *
 * @author dain.nilsson
 */
public class TestExecutionAddon implements Addon
{
	private TestExecutionAddon()
	{
	}

	private static abstract class AbstractTestExecutionAddon extends TestExecutionAddon
	{
		protected static final Logger log = LoggerFactory.getLogger( TestExecutionAddon.class );

		protected final TestRunner testRunner = BeanInjector.getBean( TestRunner.class );
	}

	//FIXME this probably should not exist... executions should not occur from the workspace
	private static class WorkspaceTestExecutionAddon extends AbstractTestExecutionAddon implements Releasable
	{
		private final TestExecutionTask actionTask = new TestExecutionTask()
		{
			@Override
			public void invoke( TestExecution execution, Phase phase )
			{
				final CanvasItem canvas = execution.getCanvas();
				switch( phase )
				{
					case PRE_START:
						canvas.triggerAction( CanvasItem.COUNTER_RESET_ACTION );
						break;
					case START:
						canvas.triggerAction( CanvasItem.START_ACTION );
						BeanInjector.getBean( TestEventManager.class ).logMessage( MessageLevel.NOTIFICATION, "Test started" );
						//TestExecutionEvent.logExecutionEvent( TestExecutionEvent.ExecutionAction.STARTED );
						break;
					case PRE_STOP:
						canvas.triggerAction( CanvasItem.STOP_ACTION );
						canvas.triggerAction( CanvasItem.COMPLETE_ACTION );
						BeanInjector.getBean( TestEventManager.class ).logMessage( MessageLevel.NOTIFICATION, "Test completed" );
						//TestExecutionEvent.logExecutionEvent( TestExecutionEvent.ExecutionAction.COMPLETED );
						break;
				}
			}
		};

		private WorkspaceTestExecutionAddon()
		{
			testRunner.registerTask( actionTask, Phase.PRE_START, Phase.START, Phase.PRE_STOP );
		}

		@Override
		public void release()
		{
			testRunner.unregisterTask( actionTask, Phase.values() );
		}
	}

	private static class CanvasTestExecutionAddon extends AbstractTestExecutionAddon implements Releasable
	{
		private final CanvasItem canvas;
		private static final Predicate<BaseEvent> isReadyAction = new Predicate<BaseEvent>()
		{
			@Override
			public boolean apply( BaseEvent event )
			{
				return CanvasItem.READY_ACTION.equals( event.getKey() );
			}
		};
		private final TestExecutionTask readyWaiterTask = new TestExecutionTask()
		{
			private EventFuture<BaseEvent> waitForAllCanvasToGetReady;

			@Override
			public void invoke( TestExecution execution, Phase phase )
			{
				if( execution.contains( canvas ) )
				{
					if( phase == Phase.PRE_STOP )
					{
						waitForAllCanvasToGetReady = new EventFuture<>( canvas, BaseEvent.class, isReadyAction );
					}
					else if( phase == Phase.POST_STOP )
					{
						if(!canvas.isCompleted())
						{
							waitforReadyEvent();
						}
					}
				}
			}

			private void waitforReadyEvent()
			{
				try
				{
					waitForAllCanvasToGetReady.get( 1, TimeUnit.MINUTES );
				}
				catch( InterruptedException | ExecutionException | TimeoutException e )
				{
					log.error( "Failed waiting for READY event", e );
				}
			}
		};

		private CanvasTestExecutionAddon( CanvasItem canvas )
		{
			this.canvas = canvas;
			testRunner.registerTask( readyWaiterTask, Phase.PRE_STOP, Phase.POST_STOP );
		}

		@Override
		public void release()
		{
			testRunner.unregisterTask( readyWaiterTask, Phase.values() );
		}
	}

	public static class Factory implements Addon.Factory<TestExecutionAddon>
	{
		private final Set<Class<?>> eagerTypes = ImmutableSet.<Class<?>>of( WorkspaceItem.class, CanvasItem.class );

		@Nonnull
		@Override
		public Class<TestExecutionAddon> getType()
		{
			return TestExecutionAddon.class;
		}

		@Nonnull
		@Override
		public TestExecutionAddon create( @Nonnull Addon.Context context )
		{
			Object owner = Preconditions.checkNotNull( context.getOwner() );
			if( owner instanceof WorkspaceItem )
			{
				return new WorkspaceTestExecutionAddon();
			}
			else if( owner instanceof CanvasItem )
			{
				return new CanvasTestExecutionAddon( ( CanvasItem )owner );
			}
			throw new IllegalArgumentException();
		}

		@Nonnull
		@Override
		public Set<Class<?>> getEagerTypes()
		{
			return eagerTypes;
		}
	}
}
