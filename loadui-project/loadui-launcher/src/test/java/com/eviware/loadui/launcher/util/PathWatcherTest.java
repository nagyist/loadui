package com.eviware.loadui.launcher.util;

import com.google.code.tempusfugit.temporal.Condition;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import static com.eviware.loadui.launcher.util.PathWatcher.Reactor;
import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PathWatcherTest
{

	PathWatcher pathWatcher;

	static
	{
		Logger logger = Logger.getLogger( PathWatcher.class.getName() );
		logger.addHandler( new ConsoleHandler() );
		logger.setUseParentHandlers( false );
	}

	@Before
	public void before()
	{
		pathWatcher = PathWatcher.singleThreadedPathWatcher();
	}

	@After
	public void after() throws Exception
	{
		pathWatcher.stopWatchingEverything();
		Thread.sleep( 100 );
	}

	@Test
	public void canWatchNewFilesInsideDirectory() throws Exception
	{
		Path testDir = Files.createTempDirectory( "PathWatcherTest" );
		System.out.println( "Using testDir: " + testDir );

		final List<WatchEvent> capturedEvents = mockReactorCapturingWatchEvents( pathWatcher, testDir, 2 );

		Path file1 = createFile( testDir.resolve( "test-file-1.txt" ) );
		Path file2 = createFile( testDir.resolve( "test-file-2.txt" ) );
		createFile( testDir.resolve( "test-file-3.txt" ) );

		waitOrTimeout( listHasSize( capturedEvents, 2 ), timeout( seconds( 2 ) ) );
		assertThat( file1.getFileName(), is( equalTo( ( Path )capturedEvents.get( 0 ).context() ) ) );
		assertThat( file2.getFileName(), is( equalTo( ( Path )capturedEvents.get( 1 ).context() ) ) );
	}

	@Test
	public void canWatchNewDirectoriesInsideDirectory() throws Exception
	{
		Path testDir = Files.createTempDirectory( "PathWatcherTest" );
		System.out.println( "Using testDir: " + testDir );

		final List<WatchEvent> capturedEvents = mockReactorCapturingWatchEvents( pathWatcher, testDir, 2 );

		Path dir1 = createDirectory( testDir.resolve( "test-dir-1" ) );
		Path dir2 = createDirectory( testDir.resolve( "test-dir-2" ) );
		createDirectory( testDir.resolve( "test-dir-3" ) );

		waitOrTimeout( listHasSize( capturedEvents, 2 ), timeout( seconds( 2 ) ) );
		assertThat( dir1.getFileName(), is( equalTo( ( Path )capturedEvents.get( 0 ).context() ) ) );
		assertThat( dir2.getFileName(), is( equalTo( ( Path )capturedEvents.get( 1 ).context() ) ) );
	}

	private static Path createDirectory( final Path path ) throws Exception
	{
		Files.createDirectory( path );
		waitOrTimeout( new Condition()
		{
			@Override
			public boolean isSatisfied()
			{
				return Files.exists( path );
			}
		}, timeout( seconds( 2 ) ) );

		// give NIO time to get notified about this event, so events won't come in the wrong order
		Thread.sleep( 150 );
		return path;
	}

	private static Path createFile( final Path path ) throws Exception
	{
		Files.createFile( path );
		waitOrTimeout( new Condition()
		{
			@Override
			public boolean isSatisfied()
			{
				return Files.exists( path );
			}
		}, timeout( seconds( 2 ) ) );

		// give NIO time to get notified about this event, so events won't come in the wrong order
		Thread.sleep( 150 );
		return path;
	}

	private static List<WatchEvent> mockReactorCapturingWatchEvents(
			PathWatcher pathWatcher,
			Path testDir,
			final int maximumNumberOfEventsToWatch ) throws Exception
	{
		final List<WatchEvent> capturedEvents = Collections.synchronizedList( new ArrayList<WatchEvent>() );

		final SettableFuture<Boolean> isReady = SettableFuture.create();
		pathWatcher.whenEventHappens( ENTRY_CREATE, testDir, new Reactor()
		{
			@Override
			public void onReady()
			{
				isReady.set( true );
			}

			@Override
			public boolean handle( WatchEvent event )
			{
				capturedEvents.add( event );
				return capturedEvents.size() < maximumNumberOfEventsToWatch;
			}

		} );

		assertThat( isReady.get(), is( true ) );

		// need to ensure NIO had time to register with the OS to watch for the directory
		Thread.sleep( 150 );

		return capturedEvents;
	}

	private static Condition listHasSize( final List<?> list, final int size )
	{
		return new Condition()
		{
			@Override
			public boolean isSatisfied()
			{
				return list.size() == size;
			}
		};
	}


}
