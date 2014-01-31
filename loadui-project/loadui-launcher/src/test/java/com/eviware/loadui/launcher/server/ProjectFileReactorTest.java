package com.eviware.loadui.launcher.server;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Map;

import static com.eviware.loadui.launcher.server.LoadUiServerProjectWatcher.ProjectFileReactor;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ProjectFileReactorTest
{

	ProjectFileReactor reactor;
	LoadUiProjectRunner mockRunner;
	Map<String, Object> attrs;

	@Before
	public void before()
	{
		mockRunner = mock( LoadUiProjectRunner.class );
		attrs = mock( Map.class );

		reactor = new LoadUiServerProjectWatcher.ProjectFileReactor( attrs, mockRunner )
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

		verify( mockRunner ).runProject( changedPath, attrs );
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

		verify( mockRunner, never() ).runProject( any( Path.class ), any( Map.class ) );
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

		verify( mockRunner, never() ).runProject( any( Path.class ), any( Map.class ) );
		assertThat( continueWatching, is( true ) );
	}

}
