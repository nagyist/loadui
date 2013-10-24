package com.eviware.loadui.impl.model.canvas.project;

import com.eviware.loadui.api.model.CanvasItem;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Renato
 */
class CanvasCompleteAwaiter
{
	static final Logger log = LoggerFactory.getLogger( CanvasCompleteAwaiter.class );

	private List<? extends CanvasItem> canvasItems;
	private ScheduledExecutorService scheduler;
	private CountDownLatch latch = new CountDownLatch( 1 );
	private Exception thrown;
	long startDelay = 50L;
	long period = 500L;

	CanvasCompleteAwaiter( Collection<? extends CanvasItem> canvasItems, final ScheduledExecutorService scheduler )
	{
		this.canvasItems = Lists.newArrayList( canvasItems );
		this.scheduler = scheduler;
	}

	public void startAndWait( long timeout, TimeUnit unit )
			throws Exception
	{
		ScheduledFuture future = scheduler.scheduleAtFixedRate( new CanvasCompleteRepeatingTask(),
				startDelay, period, TimeUnit.MILLISECONDS );
		boolean finishedWithinTimeout = latch.await( timeout, unit );
		log.debug( "Finished awaiting for all canvas to complete, finishedWithinTimeout? {}, exception? {}",
				finishedWithinTimeout, thrown );
		future.cancel( false );
		if( !finishedWithinTimeout )
			throw new TimeoutException();
		if( thrown != null )
			throw thrown;
	}

	private boolean isDone( CanvasItem item )
	{
		return !item.isRunning() || item.isCompleted();
	}

	private class CanvasCompleteRepeatingTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				for( Iterator<? extends CanvasItem> canvasIter = canvasItems.iterator(); canvasIter.hasNext(); )
				{
					CanvasItem item = canvasIter.next();
					if( isDone( item ) )
						canvasIter.remove();
				}
				if( canvasItems.isEmpty() )
					latch.countDown();
			}
			catch( Exception e )
			{
				thrown = e;
				latch.countDown();
			}
		}

	}
}
