package com.eviware.loadui.impl.model;

import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpointProvider;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.config.AgentItemConfig;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author renato
 */
public class AgentFactory
{

	private final BroadcastMessageEndpoint broadcastMessageEndpoint;
	private final MessageEndpointProvider messageEndpointProvider;
	private final ScheduledExecutorService executorService;

	public AgentFactory( BroadcastMessageEndpoint broadcastMessageEndpoint,
								MessageEndpointProvider messageEndpointProvider,
								ScheduledExecutorService executorService )
	{
		this.broadcastMessageEndpoint = broadcastMessageEndpoint;
		this.messageEndpointProvider = messageEndpointProvider;
		this.executorService = executorService;
	}

	public AgentItemImpl newInstance( WorkspaceItem workspace, AgentItemConfig config )
	{
		AgentItemImpl agent = new AgentItemImpl(
				broadcastMessageEndpoint,
				messageEndpointProvider,
				executorService,
				workspace, config );
		agent.init();
		agent.postInit();

		return agent;
	}
}
