package com.eviware.loadui.impl.model.canvas.project;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.impl.model.canvas.SceneItemImpl;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Waits for ON_COMPLETE_DONE event from all scenes and calls
 * 'doGenerateSummary' method. This event is fired after 'onComplete' method
 * of test case is executed in local mode, and when controller receives agent
 * data in distributed mode.
 *
 * @author predrag.vucetic
 */
class SceneCompleteAwaiter implements EventHandler<BaseEvent>
{
	static final Logger log = LoggerFactory.getLogger( SceneCompleteAwaiter.class );

	// timeout scheduler. this is used when all test cases have property
	// abortOnFinish set to true, so since they should return immediately,
	// they will be discarded if they do not return in timeout period. if
	// there is a test case with this property set to false, respond time is
	// not known and there is no timeout.
	private ScheduledFuture<?> awaitingSummaryTimeout;

	private ProjectItemImpl projectItem;
	private ScheduledExecutorService scheduler;

	private final Predicate<SceneItem> notAbortOnFinish = new Predicate<SceneItem>()
	{
		@Override
		public boolean apply( SceneItem scene )
		{
			return scene.isFollowProject() && !scene.isAbortOnFinish();
		}

	};

	SceneCompleteAwaiter( ProjectItemImpl projectItem, final ScheduledExecutorService scheduler )
	{
		this.projectItem = projectItem;
		this.scheduler = scheduler;
	}

	void start()
	{
		tryComplete();
		if( !projectItem.isCompleted() )
		{
			startTimeoutScheduler();
		}
	}

	@Override
	public void handleEvent( BaseEvent event )
	{
		if( event.getKey().equals( ProjectItemImpl.ON_COMPLETE_DONE ) )
		{
			event.getSource().removeEventListener( BaseEvent.class, this );
			tryComplete();
		}
	}

	private void tryComplete()
	{
		log.debug( "Trying to complete all scenes" );
		boolean allScenesCompleted = true;
		for( SceneItemImpl scene : projectItem.getScenes() )
		{
			synchronized( scene )
			{
				if( scene.isActive() && !scene.isCompleted() )
				{
					log.debug( "Scene {} is not completed yet", scene.getLabel() );
					scene.addEventListener( BaseEvent.class, this );
					allScenesCompleted = false;
				}
			}
		}
		if( allScenesCompleted )
		{
			log.debug( "All scenes have completed! Project is completed!" );
			if( awaitingSummaryTimeout != null )
				awaitingSummaryTimeout.cancel( true );

			projectItem.setCompleted( true );
		}
	}

	// if abort is true for all test cases, set timer to wait 15 seconds and
	// then on each scene call setCompleted(true) which will throw
	// ON_COMPLETE_DONE event on every test case which will then call the
	// handleEvent method of this class which will call tryComplete() and
	// generate summary when all test cases are finished.
	// TODO add another, longer timeout when there are test cases with
	// abortOnFinish = false?
	private void startTimeoutScheduler()
	{
		if( Iterables.any( projectItem.getChildren(), notAbortOnFinish ) )
			return;

		log.info( "Starting a scenario completion awaiter to wait until all scenarios have completed" );

		awaitingSummaryTimeout = scheduler.schedule( new Runnable()
		{
			@Override
			public void run()
			{
				log.error( "Failed to get statistics from all expected Agents within timeout period!" );
				for( SceneItemImpl scene : projectItem.getChildren() )
				{
					synchronized( scene )
					{
						if( !scene.isCompleted() )
						{
							scene.setCompleted( true );
						}
					}
				}
			}
		}, 15, TimeUnit.SECONDS );
	}
}
