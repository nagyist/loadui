package com.eviware.loadui.util.server;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static com.eviware.loadui.util.server.LoadUiServerProjectWatcher.ProjectFileListener;
import static com.eviware.loadui.util.server.LoadUiServerProjectWatcher.ProjectFileReactor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ProjectFileReactorTest
{

	ProjectFileReactor reactor;
	ProjectFileListener listener;

	@Before
	public void before()
	{
		listener = mock( ProjectFileListener.class );

		reactor = new LoadUiServerProjectWatcher.ProjectFileReactor( listener )
		{

		};
	}

	@Test
	public void runProjectIfFileChangedIsXmlFile()
	{
		Path changedPath = mock( Path.class );
		when( changedPath.toString() ).thenReturn( "project.xml" );

		WatchEvent mockEvent = mock( WatchEvent.class );
		when( mockEvent.context() ).thenReturn( changedPath );

		boolean continueWatching = reactor.handle( mockEvent );

		verify( listener ).onProjectFileCreated( changedPath );
		assertThat( continueWatching, is( true ) );
	}

	@Test
	public void doNotRunProjectIfFileIsNotXml()
	{
		Path changedPath = mock( Path.class );
		when( changedPath.toString() ).thenReturn( "not-project.pdf" );

		WatchEvent mockEvent = mock( WatchEvent.class );
		when( mockEvent.context() ).thenReturn( changedPath );

		boolean continueWatching = reactor.handle( mockEvent );

		verify( listener, never() ).onProjectFileCreated( changedPath );
		assertThat( continueWatching, is( true ) );
	}

	@Test
	public void continueWatchingEvenAfterException()
	{
		class TestException extends Exception
		{
		}

		Path changedPath = mock( Path.class );
		when( changedPath.toString() ).thenThrow( TestException.class );

		WatchEvent mockEvent = mock( WatchEvent.class );
		when( mockEvent.context() ).thenReturn( changedPath );

		boolean continueWatching = reactor.handle( mockEvent );

		verify( listener, never() ).onProjectFileCreated( changedPath );
		assertThat( continueWatching, is( true ) );
	}

}
