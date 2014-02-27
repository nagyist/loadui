package com.eviware.loadui.util.files;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public abstract class PathWatcher
{
	Logger log = Logger.getLogger( PathWatcher.class.getName() );

	private final ExecutorService executor = provideExecutorService();

	public abstract ExecutorService provideExecutorService();

	public void whenEventHappens( final WatchEvent.Kind eventKind,
											final Path path,
											final Reactor reactor )
	{
		log.info( "Registering to watch events " + eventKind.name() + " on path " + path );
		try
		{
			executor.execute( new WatcherRunner( eventKind, path, reactor ) );
		}
		catch( IOException e )
		{
			log.warning( "Unable to start watching " + path + ", " + e );
			e.printStackTrace();
		}
	}

	public void stopWatchingEverything()
	{
		executor.shutdownNow();
	}

	static int threadCount = 1;

	public static PathWatcher singleThreadedPathWatcher()
	{
		return new PathWatcher()
		{
			@Override
			public ExecutorService provideExecutorService()
			{
				return Executors.newSingleThreadExecutor( new ThreadFactory()
				{
					@Override
					public Thread newThread( @Nonnull Runnable r )
					{
						log.info( "Creating Thread for PathWatcher" );
						Thread t = new Thread( r, "single-threaded-path-watcher-" + threadCount++ );
						t.setDaemon( true );
						return t;
					}
				} );
			}
		};
	}

	/**
	 * Allow client code to react to events triggered by PathWatcher
	 */
	public static abstract class Reactor
	{

		/**
		 * Called when a problem occurs so that PathWatcher cannot watch a Path
		 */
		public void onCannotWatchPath()
		{
		}

		/**
		 * Called when PathWatcher is guaranteed to have started watching the Path
		 */
		public void onReady()
		{
		}

		/**
		 * Called when PathWatcher detects the event being watched has occurred
		 *
		 * @param event that was triggered
		 * @return true to keep watching, false to stop watching
		 */
		public boolean handle( WatchEvent event )
		{
			return true;
		}
	}

	class WatcherRunner implements Runnable
	{
		private final WatchEvent.Kind eventKind;
		private final Path path;
		private final Reactor reactor;
		private final WatchService watcher;

		public WatcherRunner( WatchEvent.Kind eventKind, Path path, Reactor reactor )
				throws IOException
		{
			this.eventKind = eventKind;
			this.path = path;
			this.reactor = reactor;

			watcher = path.getFileSystem().newWatchService();
			path.register( watcher, eventKind );
		}

		@Override
		public void run()
		{
			log.info( "******* Executing PathWatcher *******" );
			try
			{
				do
				{
					log.info( "PathWatcher waiting for events" );
				} while( waitForEvent() );
			}
			catch( InterruptedException ie )
			{
				log.warning( "PathWatcher interrupted, will stop watching " + path );
			}
			catch( Exception e )
			{
				e.printStackTrace();
				reactor.onCannotWatchPath();
			}
		}

		private boolean waitForEvent() throws InterruptedException
		{
			reactor.onReady();

			WatchKey watchKey = watcher.take();

			boolean keepWatching = true;
			for( WatchEvent event : watchKey.pollEvents() )
			{
				WatchEvent.Kind<?> kind = event.kind();

				if( kind == StandardWatchEventKinds.OVERFLOW )
					continue;

				if( kind == eventKind )
				{
					keepWatching = reactor.handle( event );
					break;
				}
			}

			if( keepWatching )
			{
				boolean isKeyValid = watchKey.reset();
				if( !isKeyValid )
				{
					log.warning( "PathWatcher watchKey became invalid!" );
					reactor.onCannotWatchPath();
				}
				return isKeyValid;
			}
			else
			{
				log.info( "Will not keep watching as handler does not want it" );
				watchKey.cancel();
				return false;
			}
		}
	}

}
