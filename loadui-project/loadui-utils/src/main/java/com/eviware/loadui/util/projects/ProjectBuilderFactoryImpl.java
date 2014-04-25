package com.eviware.loadui.util.projects;

import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.ProjectBuilderFactory;
import com.eviware.loadui.api.model.WorkspaceProvider;

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

