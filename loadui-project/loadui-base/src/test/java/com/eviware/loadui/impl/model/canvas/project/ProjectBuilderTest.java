package com.eviware.loadui.impl.model.canvas.project;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.ProjectBuilder;
import com.eviware.loadui.api.model.WorkspaceProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.mockito.Mockito.*;

/**
 * Created by osten on 1/14/14.
 */
public class ProjectBuilderTest
{
   ProjectBuilder builder;
	WorkspaceProvider wp;

	@Before
	public void setup(){
		wp = mock( WorkspaceProvider.class );
		when( wp.getDefaultWorkspaceFile() ).thenReturn( new File( LoadUI.getWorkingDir() + "/workspace.xml") );

		builder = new ProjectBuilderImpl( wp );
	}

	@Test
	public void shouldBuildEmptyProject(){
		builder.create().build();
	}
}
