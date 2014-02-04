package com.eviware.loadui.util.projects;

import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.*;
import com.eviware.loadui.util.projects.ProjectBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectBuilderFactoryImpl implements ProjectBuilderFactory
{

	private WorkspaceProvider workspaceProvider;
	private ComponentRegistry componentRegistry;

	public ProjectBuilderFactoryImpl( WorkspaceProvider workspaceProvider, ComponentRegistry componentRegistry )
	{
		this.workspaceProvider = workspaceProvider;
		this.componentRegistry = componentRegistry;
	}

	public ProjectBuilderImpl newInstance()
	{
	  return new ProjectBuilderImpl( componentRegistry, workspaceProvider );
	}
}

