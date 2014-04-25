package com.eviware.loadui.util.server;

import com.eviware.loadui.util.files.FileSystemHelper;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static com.eviware.loadui.util.server.LoadUiServerProjectWatcher.ProjectsLocationReactor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectsLocationReactorTest
{

	Path projectLocation = mock( Path.class );
	FileSystemHelper files = mock( FileSystemHelper.class );
	ProjectsLocationReactor reactor;
	private int projectsLocationCreatedCount = 0;

	@Before
	public void before()
	{
		reactor = new ProjectsLocationReactor( projectLocation, files )
		{
			@Override
			public void onProjectsLocationCreated()
			{
				projectsLocationCreatedCount++;
			}
		};
	}

	@Test
	public void run_OnProjectsLocationCreated_IfDetectingCreationOfExpectedDir()
	{
		Path changedPath = mock( Path.class );
		when( changedPath.toString() ).thenReturn( "projects" );

		Path projectLocationParent = mock( Path.class );
		Path absolutePath = mock( Path.class );

		when( projectLocation.getParent() ).thenReturn( projectLocationParent );
		when( files.makeAbsolute( projectLocationParent, changedPath ) ).thenReturn( absolutePath );
		when( files.isDirectory( absolutePath ) ).thenReturn( true );
		when( files.areSameLocation( projectLocation, absolutePath ) ).thenReturn( true );

		WatchEvent mockEvent = mock( WatchEvent.class );
		when( mockEvent.context() ).thenReturn( changedPath );

		boolean continueWatching = reactor.handle( mockEvent );

		assertThat( projectsLocationCreatedCount, is( 1 ) );
		assertThat( continueWatching, is( false ) );
	}

	@Test
	public void doNotRun_OnProjectsLocationCreated_IfDetectingCreationOfDifferentDir()
	{
		Path changedPath = mock( Path.class );
		when( changedPath.toString() ).thenReturn( "projects" );

		Path projectLocationParent = mock( Path.class );
		Path absolutePath = mock( Path.class );

		when( projectLocation.getParent() ).thenReturn( projectLocationParent );
		when( files.makeAbsolute( projectLocationParent, changedPath ) ).thenReturn( absolutePath );
		when( files.isDirectory( absolutePath ) ).thenReturn( true );
		when( files.areSameLocation( projectLocation, absolutePath ) ).thenReturn( false );

		WatchEvent mockEvent = mock( WatchEvent.class );
		when( mockEvent.context() ).thenReturn( changedPath );

		boolean continueWatching = reactor.handle( mockEvent );

		assertThat( projectsLocationCreatedCount, is( 0 ) );
		assertThat( continueWatching, is( true ) );
	}

	@Test
	public void doNotRun_OnProjectsLocationCreated_IfDetectingCreationOfAFile()
	{
		Path changedPath = mock( Path.class );
		when( changedPath.toString() ).thenReturn( "project.xml" );

		Path projectLocationParent = mock( Path.class );
		Path absolutePath = mock( Path.class );

		when( projectLocation.getParent() ).thenReturn( projectLocationParent );
		when( files.makeAbsolute( projectLocationParent, changedPath ) ).thenReturn( absolutePath );
		when( files.isDirectory( absolutePath ) ).thenReturn( false );
		when( files.areSameLocation( projectLocation, absolutePath ) ).thenReturn( true );

		WatchEvent mockEvent = mock( WatchEvent.class );
		when( mockEvent.context() ).thenReturn( changedPath );

		boolean continueWatching = reactor.handle( mockEvent );

		assertThat( projectsLocationCreatedCount, is( 0 ) );
		assertThat( continueWatching, is( true ) );
	}

}
