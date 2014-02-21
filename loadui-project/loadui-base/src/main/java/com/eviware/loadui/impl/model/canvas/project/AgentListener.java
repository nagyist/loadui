package com.eviware.loadui.impl.model.canvas.project;

import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.messaging.MessageAwaiterFactory;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.messaging.SceneCommunication;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.impl.model.ComponentItemImpl;
import com.eviware.loadui.impl.model.canvas.SceneItemImpl;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class AgentListener implements EventHandler<BaseEvent>, MessageListener, Releasable
{
	static final Logger log = LoggerFactory.getLogger( AgentListener.class );

	private final Set<AgentItem> agents = new HashSet<>();
	private final AgentContextListener subListener = new AgentContextListener();
	private final ProjectItemImpl projectItem;
	private final ConversionService conversionService;
	private final MessageAwaiterFactory messageAwaiterFactory;

	AgentListener( ProjectItemImpl projectItem, ConversionService conversionService, MessageAwaiterFactory messageAwaiterFactory )
	{
		this.projectItem = projectItem;
		this.conversionService = conversionService;
		this.messageAwaiterFactory = messageAwaiterFactory;
	}

	public void attach( AgentItem agent )
	{
		if( agents.add( agent ) )
		{
			agent.addEventListener( BaseEvent.class, this );
			agent.addMessageListener( AgentItem.AGENT_CHANNEL, this );
			agent.addMessageListener( ComponentContext.COMPONENT_CONTEXT_CHANNEL, subListener );
		}
	}

	public void detach( AgentItem agent )
	{
		if( agents.remove( agent ) )
		{
			agent.removeEventListener( BaseEvent.class, this );
			agent.removeMessageListener( this );
			agent.removeMessageListener( subListener );
		}
	}

	@Override
	public void release()
	{
		for( AgentItem agent : agents )
		{
			agent.removeEventListener( BaseEvent.class, this );
			agent.removeMessageListener( this );
			agent.removeMessageListener( subListener );
		}
		agents.clear();
	}

	@Override
	public void handleEvent( BaseEvent event )
	{
		AgentItem agent = ( AgentItem )event.getSource();
		if( AgentItem.READY.equals( event.getKey() ) )
		{
			final boolean ready = agent.isReady();
			log.debug( "Agent {} ready: {}", agent, ready );
			for( SceneItem scene : projectItem.getScenesAssignedTo( agent ) )
			{
				log.debug( "Send message assign: {}", scene.getLabel() );
				ProjectItemImpl.AssignmentImpl assignment = projectItem.getAssignment( scene, agent );
				if( assignment != null )
					assignment.setLoaded( false );
				if( ready )
					projectItem.sendAssignMessage( agent, scene );
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
	{
		if( !( endpoint instanceof AgentItem ) || !( data instanceof Map ) )
			return;

		final AgentItem agent = ( AgentItem )endpoint;

		Map<?, ?> message = ( Map )data;
		Object sceneDefine = message.get( AgentItem.DEFINE_SCENE );
		if( sceneDefine != null && sceneDefine instanceof String )
		{

			SceneItem scene = ( SceneItem )projectItem.getRegistry().lookup( ( String )sceneDefine );
			if( scene != null )
			{
				log.debug( "Agent {} has requested a Scenario: {}, sending...", agent,
						message.get( AgentItem.DEFINE_SCENE ) );
				ProjectItemImpl.AssignmentImpl assignment = projectItem.getAssignment( scene, agent );
				if( assignment != null )
					assignment.setLoaded( false );
				agent.sendMessage(
						channel,
						ImmutableMap.of( AgentItem.SCENE_ID, scene.getId(), AgentItem.SCENE_DEFINITION,
								conversionService.convert( scene, String.class ) ) );
			}
			else
			{
				log.warn( "An Agent {} has requested a nonexistant Scenario: {}", agent,
						message.get( AgentItem.DEFINE_SCENE ) );
			}
		}
		else
		{
			Object agentStarted = message.get( AgentItem.STARTED );
			if( agentStarted != null && agentStarted instanceof String )
			{
				SceneItem scene = ( SceneItem )projectItem.getRegistry().lookup( ( String )agentStarted );
				ProjectItemImpl.AssignmentImpl assignment = projectItem.getAssignment( scene, agent );

				if( assignment != null )
					assignment.setLoaded( true );

				if( scene.isRunning() && !projectItem.getWorkspace().isLocalMode() )
				{
					agent.sendMessage( SceneCommunication.CHANNEL,
							new Object[] { scene.getId(), Long.toString( scene.getVersion() ),
									SceneCommunication.ACTION_EVENT, ProjectItemImpl.START_ACTION, scene.getId() } );
				}
			}
		}
	}

	class AgentContextListener implements MessageListener
	{
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			if( endpoint instanceof AgentItem )
			{
				AgentItem agent = ( AgentItem )endpoint;
				Object[] args = ( Object[] )data;
				Addressable target = projectItem.getRegistry().lookup( ( String )args[0] );
				if( target instanceof ComponentItemImpl )
				{
					ComponentItemImpl component = ( ComponentItemImpl )target;
					TerminalMessage message = component.getContext().newMessage();
					message.load( args[1] );
					component.sendAgentMessage( agent, message );
				}
			}
			else
			{
				log.warn( "Cannot handle message on channel {} as endpoint is not AgentItem but {}",
						channel, endpoint.getClass() );
			}

		}
	}

}

