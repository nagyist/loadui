package com.eviware.loadui.launcher.server;

import com.eviware.loadui.launcher.util.PathWatcher;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.Map;

import static com.eviware.loadui.launcher.server.LoadUiServerProjectWatcher.*;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class LoadUiServerProjectWatcherTest
{
	LoadUiServerProjectWatcher projectWatcher;
	FileSystemHelper mockFiles = mock( FileSystemHelper.class );
	LoadUiProjectRunner mockRunner = mock( LoadUiProjectRunner.class );
	PathWatcher mockWatcher = mock( PathWatcher.class );
	PathWatcherReactorProvider mockReactorProvider = mock( PathWatcherReactorProvider.class );

	final Path projectsLocation = mock( Path.class );

	@Before
	public void before()
	{
		projectWatcher = new LoadUiServerProjectWatcher( mockRunner, mockFiles );
		projectWatcher.reactorProvider = mockReactorProvider;
		when( mockFiles.providePathWatcher() ).thenReturn( mockWatcher );
	}

	@Test
	public void registerToListenForProjectFileCreationIfProjectsLocationExists()
	{
		Map<String, Object> attrs = Maps.newHashMap();

		when( mockFiles.isDirectory( projectsLocation ) ).thenReturn( true );
		when( mockFiles.exists( projectsLocation ) ).thenReturn( true );

		ProjectFileReactor mockProjFileReactor = mock( ProjectFileReactor.class );

		when( mockReactorProvider.getProjectFileReactor(
				eq( attrs ), eq( mockRunner ),
				any( ProjectFileNotWatchableListener.class ) ) )
				.thenReturn( mockProjFileReactor );

		projectWatcher.watchForProjectToRun( projectsLocation, attrs );

		verify( mockWatcher ).whenEventHappens(
				StandardWatchEventKinds.ENTRY_CREATE,
				projectsLocation,
				mockProjFileReactor
		);
	}

	@Test
	public void registerToListenForProjectsLocationCreationIfThatDoesNotExist()
	{
		Map<String, Object> attrs = Maps.newHashMap();

		when( mockFiles.isDirectory( projectsLocation ) ).thenReturn( false );
		when( mockFiles.exists( projectsLocation ) ).thenReturn( false );

		Path projLocationParent = mock( Path.class );
		when( projectsLocation.getParent() ).thenReturn( projLocationParent );
		when( mockFiles.isDirectory( projLocationParent ) ).thenReturn( true );

		ProjectsLocationReactor mockProjLocationReactor = mock( ProjectsLocationReactor.class );

		when( mockReactorProvider.getProjectsLocationWatchEventHandler(
				eq( projectsLocation ), eq( mockFiles ),
				any( ProjectsLocationCreatedListener.class ) ) )
				.thenReturn( mockProjLocationReactor );

		projectWatcher.watchForProjectToRun( projectsLocation, attrs );

		verify( mockWatcher ).whenEventHappens(
				StandardWatchEventKinds.ENTRY_CREATE,
				projLocationParent,
				mockProjLocationReactor
		);
	}

	@Test
	public void throwExceptionIfProjectsLocationAndItsParentDoNotExist()
	{
		Map<String, Object> attrs = Maps.newHashMap();

		when( mockFiles.isDirectory( projectsLocation ) ).thenReturn( false );
		when( mockFiles.exists( projectsLocation ) ).thenReturn( false );

		Path projLocationParent = mock( Path.class );
		when( projectsLocation.getParent() ).thenReturn( projLocationParent );
		when( mockFiles.isDirectory( projLocationParent ) ).thenReturn( false );

		try
		{
			projectWatcher.watchForProjectToRun( projectsLocation, attrs );
			fail( "Expected Exception to be thrown but nothing happened" );
		}
		catch( RuntimeException rte )
		{
		}

		verify( mockWatcher ).stopWatchingEverything();
	}

	@Test
	public void watchesParentPathWhenProjectsPathIsNotAnExistingDir()
	{
		when( mockFiles.providePathWatcher() ).thenReturn( mockWatcher );
		when( mockFiles.isDirectory( projectsLocation ) ).thenReturn( false );
		when( mockFiles.exists( projectsLocation ) ).thenReturn( false );

		Path mockParentPath = mock( Path.class );
		when( mockFiles.exists( mockParentPath ) ).thenReturn( true );
		when( projectsLocation.getParent() ).thenReturn( mockParentPath );
	}

}
