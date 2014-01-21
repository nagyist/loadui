package com.eviware.loadui.util.projects;

import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.*;
import com.eviware.loadui.util.projects.ProjectBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by osten on 1/14/14.
 */
public class ProjectBuilderFactoryImpl implements ProjectBuilderFactory
{

	private WorkspaceProvider workspaceProvider;

	public ProjectBuilderFactoryImpl( WorkspaceProvider workspaceProvider )
	{
		this.workspaceProvider = workspaceProvider;
	}

	public ProjectBuilderImpl newInstance()
	{
	  return new ProjectBuilderImpl( workspaceProvider );
	}
}

