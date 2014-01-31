package com.eviware.loadui.launcher.server;

import com.eviware.loadui.launcher.util.PathWatcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Map;
import java.util.logging.Logger;

import static com.eviware.loadui.launcher.util.PathWatcher.Reactor;

public class LoadUiServerProjectWatcher
{
	static final Logger log = Logger.getLogger( LoadUiServerProjectWatcher.class.getName() );
	private final LoadUiProjectRunner projectRunner;
	private final FileSystemHelper files;
	protected PathWatcherReactorProvider reactorProvider = new PathWatcherReactorProvider();

	LoadUiServerProjectWatcher( LoadUiProjectRunner projectRunner )
	{
		this( projectRunner, new FileSystemHelper() );
	}

	LoadUiServerProjectWatcher( LoadUiProjectRunner projectRunner, FileSystemHelper fileSystemHelper )
	{
		this.projectRunner = projectRunner;
		this.files = fileSystemHelper;
	}

	public void watchForProjectToRun( Path projectsLocation, final Map<String, Object> attributes )
	{
		log.info( "Will watch projects in directory: " + projectsLocation );
		PathWatcher pathWatcher = files.providePathWatcher();

		if( files.isDirectory( projectsLocation ) )
		{
			watchForProjectFileCreation( pathWatcher, projectsLocation, attributes );
		}
		else
		{
			if( files.isDirectory( projectsLocation.getParent() ) )
			{
				log.info( "Location given is not a directory, will wait until a directory is created in the same location" );
				waitForProjectLocationToBeCreatedThenWatchIt( pathWatcher, projectsLocation, attributes );
			}
			else
			{
				pathWatcher.stopWatchingEverything();
				throw new RuntimeException( "Cannot start watching the given project location as neither it or its parent exists" );
			}
		}

	}

	private void watchForProjectFileCreation( final PathWatcher pathWatcher,
															final Path projectsLocation,
															final Map<String, Object> attributes )
	{
		pathWatcher.whenEventHappens( StandardWatchEventKinds.ENTRY_CREATE,
				projectsLocation,
				reactorProvider.getProjectFileReactor( attributes, projectRunner,
						new ProjectFileNotWatchableListener()
						{
							@Override
							public void onProjectFileNotWatchable()
							{
								log.info( "Projects directory cannot be watched anymore. Watching its parent until it comes back again" );
								waitForProjectLocationToBeCreatedThenWatchIt( pathWatcher, projectsLocation, attributes );
							}
						} ) );
	}

	private void waitForProjectLocationToBeCreatedThenWatchIt( final PathWatcher pathWatcher,
																				  final Path projectsLocation,
																				  final Map<String, Object> attributes )
	{
		pathWatcher.whenEventHappens( StandardWatchEventKinds.ENTRY_CREATE,
				projectsLocation.getParent(),
				reactorProvider.getProjectsLocationWatchEventHandler(
						projectsLocation, files,
						getProjectsLocationCreatedListener( pathWatcher, projectsLocation, attributes ) ) );
	}

	private ProjectsLocationCreatedListener getProjectsLocationCreatedListener( final PathWatcher pathWatcher,
																										 final Path projectsLocation,
																										 final Map<String, Object> attributes )
	{
		return new ProjectsLocationCreatedListener()
		{
			@Override
			public void onProjectsLocationCreated()
			{
				watchForProjectFileCreation( pathWatcher, projectsLocation, attributes );
			}
		};
	}

	protected static class PathWatcherReactorProvider
	{

		public Reactor getProjectFileReactor( Map<String, Object> attributes,
														  LoadUiProjectRunner projectRunner,
														  final ProjectFileNotWatchableListener projectFileNotWatchableListener )
		{
			return new ProjectFileReactor( attributes, projectRunner )
			{
				@Override
				public void onCannotWatchPath()
				{
					projectFileNotWatchableListener.onProjectFileNotWatchable();
				}
			};
		}

		public Reactor getProjectsLocationWatchEventHandler( Path projectsLocation,
																			  FileSystemHelper files,
																			  final ProjectsLocationCreatedListener listener )
		{
			return new ProjectsLocationReactor( projectsLocation, files )
			{
				@Override
				public void onProjectsLocationCreated()
				{
					listener.onProjectsLocationCreated();
				}
			};
		}
	}

	protected static interface ProjectsLocationCreatedListener
	{
		void onProjectsLocationCreated();
	}

	protected static interface ProjectFileNotWatchableListener
	{
		void onProjectFileNotWatchable();
	}

	protected static abstract class ProjectFileReactor extends Reactor
	{
		private final Map<String, Object> attributes;
		private final LoadUiProjectRunner projectRunner;

		public ProjectFileReactor( Map<String, Object> attributes, LoadUiProjectRunner projectRunner )
		{
			this.attributes = attributes;
			this.projectRunner = projectRunner;
		}

		@Override
		public final boolean handle( WatchEvent event )
		{
			try
			{
				Path changedFile = ( Path )event.context();
				log.info( "Detected creation of potential project file: " + changedFile );
				if( changedFile.toString().endsWith( ".xml" ) )
					projectRunner.runProject( changedFile, attributes );
			}
			catch( Exception e )
			{
				log.info( "Tried to start running project but a problem happened: " + e );
				e.printStackTrace();
			}
			return true;
		}
	}

	protected static abstract class ProjectsLocationReactor extends Reactor
	{
		private final Path projectsLocation;
		private final FileSystemHelper files;

		public ProjectsLocationReactor( Path projectsLocation, FileSystemHelper files )
		{
			this.projectsLocation = projectsLocation;
			this.files = files;
		}

		@Override
		public final boolean handle( WatchEvent event )
		{
			Path changedFile = files.makeAbsolute( projectsLocation.getParent(), ( Path )event.context() );
			log.info( "Created possibly projects location: " + changedFile );
			if( files.isDirectory( changedFile ) && files.areSameLocation( projectsLocation, changedFile ) )
			{
				log.info( "Projects location created, will start watching it" );
				onProjectsLocationCreated();
				return false;
			}
			return true;
		}

		public abstract void onProjectsLocationCreated();

	}

	protected static class FileSystemHelper
	{

		public boolean isDirectory( Path path )
		{
			return Files.isDirectory( path );
		}

		public boolean exists( Path path )
		{
			return Files.exists( path );
		}

		public boolean areSameLocation( Path p1, Path p2 )
		{
			log.info( "Checking if " + p1 + " and " + p2 + " are the same path" );
			return p1.equals( p2 );
		}

		protected PathWatcher providePathWatcher()
		{
			return PathWatcher.singleThreadedPathWatcher();
		}

		public Path makeAbsolute( Path parent, Path path )
		{
			return parent.resolve( path );
		}
	}

}
