package com.eviware.loadui.impl.model.canvas.project;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.util.collections.CollectionFuture;
import com.eviware.loadui.util.events.EventFuture;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class AgentReadyAwaiterTask implements TestExecutionTask
{
	static final Logger log = LoggerFactory.getLogger( AgentReadyAwaiterTask.class );

	private final ProjectItemImpl projectItem;

	AgentReadyAwaiterTask( ProjectItemImpl projectItem )
	{
		this.projectItem = projectItem;
	}

	@Override
	public void invoke( TestExecution execution, Phase phase )
	{
		if( execution.getCanvas() == projectItem && !projectItem.getWorkspace().isLocalMode() )
		{
			ArrayList<EventFuture<BaseEvent>> awaitingScenes = Lists.newArrayList();
			for( final SceneItem scene : projectItem.getChildren() )
			{
				for( final AgentItem agent : projectItem.getAgentsAssignedTo( scene ) )
				{
					if( agent.isEnabled() && !projectItem.isSceneLoaded( scene, agent ) )
					{
						awaitingScenes.add( new EventFuture<>( projectItem, BaseEvent.class,
								new Predicate<BaseEvent>()
								{
									@Override
									public boolean apply( BaseEvent event )
									{
										return ProjectItemImpl.SCENE_LOADED.equals( event.getKey() ) &&
												projectItem.isSceneLoaded( scene, agent );
									}
								} ) );
					}
				}
			}

			try
			{
				new CollectionFuture<>( awaitingScenes ).get( 10, TimeUnit.SECONDS );
			}
			catch( InterruptedException e )
			{
				log.error( "Error while waiting for Scenarios to become ready on Agents", e );
			}
			catch( ExecutionException e )
			{
				log.error( "Error while waiting for Scenario to become ready on Agents", e );
			}
			catch( TimeoutException e )
			{
				log.error( "Timed out waiting for Scenarios to become ready on Agents", e );
			}
		}
	}
}