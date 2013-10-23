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
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.messaging.MessageAwaiter;
import com.eviware.loadui.api.messaging.MessageAwaiterFactory;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.traits.Releasable;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Hooks into the TestExecution at each phase, and lets each Agent run its own
 * tasks before reporting back and continuing with the next phase.
 *
 * @author dain.nilsson
 */
public class AgentTestExecutionAddon implements Addon, Releasable
{
	static final Logger log = LoggerFactory.getLogger( AgentTestExecutionAddon.class );

	private final ProjectItem project;
	private final DistributePhaseTask task = new DistributePhaseTask();
	private final SceneReloadedListener reloadListener = new SceneReloadedListener();
	private final MessageAwaiterFactory factory;
	private final TestRunner testRunner;
	private final AddressableRegistry addressableRegistry;
	private final ExecutorService executorService;

	private AgentTestExecutionAddon( ProjectItem project, TestRunner testRunner,
												MessageAwaiterFactory factory, AddressableRegistry addressableRegistry,
												ExecutorService executorService )
	{
		this.project = project;
		this.testRunner = testRunner;
		this.factory = factory;
		this.addressableRegistry = addressableRegistry;
		this.executorService = executorService;

		testRunner.registerTask( task, Phase.values() );
	}

	@Override
	public void release()
	{
		testRunner.unregisterTask( task, Phase.values() );
	}

	private class DistributePhaseTask implements TestExecutionTask
	{
		@Override
		public void invoke( final TestExecution execution, final Phase phase )
		{
			if( project.getWorkspace().isLocalMode() )
				return;

			if( Phase.PRE_START == phase )
			{
				for( SceneItem scene : project.getChildren() )
				{
					for( AgentItem agent : project.getAgentsAssignedTo( scene ) )
					{
						agent.addMessageListener( AgentItem.AGENT_CHANNEL, reloadListener );
					}
				}
			}

			if( Phase.PRE_STOP == phase )
			{
				for( SceneItem scene : project.getChildren() )
				{
					for( AgentItem agent : project.getAgentsAssignedTo( scene ) )
					{
						agent.removeMessageListener( reloadListener );
					}
				}
			}

			HashSet<AgentItem> agents = Sets.newHashSet();
			HashSet<MessageAwaiter> waiters = Sets.newHashSet();
			for( SceneItem scene : project.getChildren() )
			{
				for( AgentItem agent : project.getAgentsAssignedTo( scene ) )
				{
					if( agent.isReady() && agents.add( agent ) )
					{
						waiters.add( factory.create( agent, execution.getCanvas().getId(), phase ) );
					}
				}
			}

			for( MessageAwaiter waiter : waiters )
			{
				waiter.await();
			}
		}
	}

	private class SceneReloadedListener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, final MessageEndpoint endpoint, Object data )
		{
			@SuppressWarnings( "unchecked" )
			Map<String, String> message = ( Map<String, String> )data;
			final String canvasId = message.get( AgentItem.STARTED );
			SceneItem scene = ( SceneItem )addressableRegistry.lookup( canvasId );
			if( scene != null && scene.isRunning() && !scene.getProject().getWorkspace().isLocalMode() )
			{
				executorService.execute( new Runnable()
				{
					@Override
					public void run()
					{
						for( Phase phase : Arrays.asList( Phase.PRE_START, Phase.START, Phase.POST_START ) )
						{
							factory.create( ( AgentItem )endpoint, canvasId, phase ).await();
						}
					}
				} );
			}
		}
	}

	public static class Factory implements Addon.Factory<AgentTestExecutionAddon>
	{
		private final MessageAwaiterFactory factory;
		private final TestRunner testRunner;
		private final AddressableRegistry addressableRegistry;
		private final ExecutorService executorService;

		private final Set<Class<?>> eagerTypes = ImmutableSet.<Class<?>>of( ProjectItem.class );

		public Factory( TestRunner testRunner, MessageAwaiterFactory factory,
							 AddressableRegistry addressableRegistry, ExecutorService executorService )
		{
			this.testRunner = testRunner;
			this.factory = factory;
			this.addressableRegistry = addressableRegistry;
			this.executorService = executorService;
		}

		@Override
		public Class<AgentTestExecutionAddon> getType()
		{
			return AgentTestExecutionAddon.class;
		}

		@Override
		public AgentTestExecutionAddon create( Addon.Context context )
		{
			ProjectItem project = ( ProjectItem )Preconditions.checkNotNull( context.getOwner() );
			return new AgentTestExecutionAddon( project, testRunner, factory, addressableRegistry, executorService );
		}

		@Override
		public Set<Class<?>> getEagerTypes()
		{
			return eagerTypes;
		}
	}
}
