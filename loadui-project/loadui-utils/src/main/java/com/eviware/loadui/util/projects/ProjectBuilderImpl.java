package com.eviware.loadui.util.projects;

import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by osten on 1/16/14.
 */

public class ProjectBuilderImpl implements ProjectBuilder
{
	private ComponentRegistry componentRegistry;
	private WorkspaceProvider workspaceProvider;
	ProjectBlueprint blueprint;

	Logger log = LoggerFactory.getLogger( ProjectBuilder.class );

	public ProjectBuilderImpl( WorkspaceProvider workspaceProvider, ComponentRegistry componentRegistry )
	{
		this.componentRegistry = componentRegistry;
		this.workspaceProvider = workspaceProvider;
	}

	@Override
	public ProjectBuilder create()
	{
		blueprint = new ProjectBlueprint();
		return this;
	}

	@Override
	public ProjectBuilder where( File where )
	{
		blueprint.setProjectFile( where );
		return this;
	}

	@Override
	public ProjectBuilder requestsLimit( Long requests )
	{
		blueprint.setRequestLimit( requests );
		return this;
	}

	@Override
	public ProjectBuilder timeLimit( Long seconds )
	{
		blueprint.setTimeLimit( seconds );
		return this;
	}

	@Override
	public ProjectBuilder assertionLimit( Long assertionFailures )
	{
		blueprint.setAssertionFailureLimit( assertionFailures );
		return this;
	}

	@Override
	public ProjectBuilder label( String name )
	{
		blueprint.setLabel( name );
		return this;
	}

	@Override
	public ProjectBuilder importProject( boolean bool )
	{
		blueprint.importProject( bool );
   	return this;
	}

	@Override
	public ProjectRef build()
	{
		ProjectRef project = assemble( blueprint );
		workspaceProvider.loadDefaultWorkspace();
		return project;
	}

	private boolean save( File file ){
		try
		{
			workspaceProvider.getWorkspace().importProject( file, true );
			return true;
		}
		catch( IOException e )
		{
			return false;
		}
	}

	private ProjectRef assemble( ProjectBlueprint blueprint ){

		ProjectRef project = workspaceProvider.getWorkspace().createProject( blueprint.getProjectFile(), blueprint.getLabel(), true );
		project.setLabel( blueprint.getLabel() );

      project.getProject().setLimit( CanvasItem.REQUEST_COUNTER, blueprint.getRequestLimit() );
		project.getProject().setLimit( CanvasItem.TIMER_COUNTER, blueprint.getTimeLimit() );
		project.getProject().setLimit( CanvasItem.FAILURE_COUNTER, blueprint.getTimeLimit() );

		if( blueprint.importProject() ){
		 	save( project.getProjectFile() );
		}

		return project;
	}

	private class ProjectBlueprint
	{
		private static final boolean DEFAULT_IMPORT_PROJECT = false;
		private static final long DEFAULT_REQUEST_LIMIT = 0;
		private static final long DEFAULT_ASSERTION_FAILURE_LIMIT = 0;
		private static final long DEFAULT_TIME_LIMIT = 0;

		private boolean importProject;
		private File projectFile;
		private List<List<ComponentItem>> componentChains;
		private String label;
		private long requestLimit;
		private long timeLimit;
		private long assertionFailureLimit;

		public ProjectBlueprint(){

			try
			{
				setProjectFile( File.createTempFile( "loadui-project", ".xml" ) );
			}
			catch( IOException e )
			{
				log.error( "cannot create a temporary project" );
			}
			setLabel( projectFile.getName() );

			setComponentChains( new ArrayList<List<ComponentItem>>() );
			importProject( DEFAULT_IMPORT_PROJECT );
			setRequestLimit( DEFAULT_REQUEST_LIMIT );
			setAssertionFailureLimit( DEFAULT_ASSERTION_FAILURE_LIMIT );
			setTimeLimit( DEFAULT_TIME_LIMIT );
		}

		public boolean importProject()
		{
			return importProject;
		}

		public void importProject( boolean importProject )
		{
			this.importProject = importProject;
		}

		public File getProjectFile()
		{
			return projectFile;
		}

		public void setProjectFile( File where )
		{
			this.projectFile = where;
		}

		public List<List<ComponentItem>> getComponentChains()
		{
			return componentChains;
		}

		public void setComponentChains( List<List<ComponentItem>> componentChains )
		{
			this.componentChains = componentChains;
		}

		public String getLabel()
		{
			return label;
		}

		public void setLabel( String label )
		{
			this.label = label;
		}

		public long getRequestLimit()
		{
			return requestLimit;
		}

		public void setRequestLimit( long requestLimit )
		{
			this.requestLimit = requestLimit;
		}

		public long getTimeLimit()
		{
			return timeLimit;
		}

		public void setTimeLimit( long timeLimit )
		{
			this.timeLimit = timeLimit;
		}

		public long getAssertionFailureLimit()
		{
			return assertionFailureLimit;
		}

		public void setAssertionFailureLimit( long assertionFailureLimit )
		{
			this.assertionFailureLimit = assertionFailureLimit;
		}
	}
}
