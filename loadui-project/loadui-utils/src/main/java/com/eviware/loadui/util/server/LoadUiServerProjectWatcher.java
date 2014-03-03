package com.eviware.loadui.util.server;


import com.eviware.loadui.util.files.FileSystemHelper;
import com.eviware.loadui.util.files.PathWatcher;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Map;
import java.util.logging.Logger;


public class LoadUiServerProjectWatcher
{
	static final Logger log = Logger.getLogger( LoadUiServerProjectWatcher.class.getName() );
	private final LoadUiServerProjectRunner projectRunner;
	private final FileSystemHelper files;
	protected PathWatcherReactorProvider reactorProvider = new PathWatcherReactorProvider();

	public LoadUiServerProjectWatcher( LoadUiServerProjectRunner projectRunner )
	{
		this( projectRunner, new FileSystemHelper() );
	}

	LoadUiServerProjectWatcher( LoadUiServerProjectRunner projectRunner, FileSystemHelper fileSystemHelper )
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
				reactorProvider.getProjectFileReactor(
						new ProjectFileListener()
						{
							@Override
							public void onProjectFileNotWatchable()
							{
								log.info( "Projects directory cannot be watched anymore. Watching its parent until it comes back again" );
								waitForProjectLocationToBeCreatedThenWatchIt( pathWatcher, projectsLocation, attributes );
							}

							@Override
							public void onProjectFileCreated( Path projectFile )
							{
								projectRunner.runProjectAsAgent(
										files.makeAbsolute( projectsLocation, projectFile ), attributes, false );
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

		public PathWatcher.Reactor getProjectFileReactor( final ProjectFileListener projectFileListener )
		{
			return new ProjectFileReactor( projectFileListener )
			{
				@Override
				public void onCannotWatchPath()
				{
					projectFileListener.onProjectFileNotWatchable();
				}
			};
		}

		public PathWatcher.Reactor getProjectsLocationWatchEventHandler( Path projectsLocation,
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

	protected static interface ProjectFileListener
	{
		void onProjectFileNotWatchable();

		void onProjectFileCreated( Path projectFile );
	}

	public static abstract class ProjectFileReactor extends PathWatcher.Reactor
	{
		private final ProjectFileListener listener;

		public ProjectFileReactor( ProjectFileListener listener )
		{
			this.listener = listener;
		}

		@Override
		public final boolean handle( WatchEvent event )
		{
			try
			{
				Path changedFile = ( Path )event.context();
				log.info( "Detected creation of potential project file: " + changedFile );
				if( changedFile.toString().endsWith( ".xml" ) )
					listener.onProjectFileCreated( changedFile );
			}
			catch( Exception e )
			{
				log.info( "Tried to start running project but a problem happened: " + e );
				e.printStackTrace();
			}
			return true;
		}

	}

	public static abstract class ProjectsLocationReactor extends PathWatcher.Reactor
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


}
