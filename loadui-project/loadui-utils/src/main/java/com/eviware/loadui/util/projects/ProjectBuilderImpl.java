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
	private WorkspaceProvider workspaceProvider;

	Logger log = LoggerFactory.getLogger( ProjectBuilder.class );

	public ProjectBuilderImpl( WorkspaceProvider workspaceProvider )
	{
		this.workspaceProvider = workspaceProvider;
	}

	@Override
	public ProjectBlueprint create()
	{
		return new ProjectBlueprint();
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
		project.getProject().setLimit( CanvasItem.FAILURE_COUNTER, blueprint.getAssertionFailureLimit() );

		save( project.getProjectFile() );

		return project;
	}

	public class ProjectBlueprint implements ProjectBuilder.ProjectBlueprint
	{
		private static final long DEFAULT_REQUEST_LIMIT = 0;
		private static final long DEFAULT_ASSERTION_FAILURE_LIMIT = 0;
		private static final long DEFAULT_TIME_LIMIT = 0;

		private File projectFile;
		private List<List<ComponentItem>> componentChains;
		private String label;
		private long requestLimit;
		private long timeLimit;
		private long assertionFailureLimit;

      private ProjectBlueprint(){
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
			setRequestLimit( DEFAULT_REQUEST_LIMIT );
			setAssertionFailureLimit( DEFAULT_ASSERTION_FAILURE_LIMIT );
			setTimeLimit( DEFAULT_TIME_LIMIT );
		}

      private File getProjectFile()
		{
			return projectFile;
		}

		private void setProjectFile( File where )
		{
			this.projectFile = where;
		}

		private List<List<ComponentItem>> getComponentChains()
		{
			return componentChains;
		}

		private void setComponentChains( List<List<ComponentItem>> componentChains )
		{
			this.componentChains = componentChains;
		}

		private String getLabel()
		{
			return label;
		}

		private void setLabel( String label )
		{
			this.label = label;
		}

		private long getRequestLimit()
		{
			return requestLimit;
		}

		private void setRequestLimit( long requestLimit )
		{
			this.requestLimit = requestLimit;
		}

		private long getTimeLimit()
		{
			return timeLimit;
		}

		private void setTimeLimit( long timeLimit )
		{
			this.timeLimit = timeLimit;
		}

		private long getAssertionFailureLimit()
		{
			return assertionFailureLimit;
		}

		private void setAssertionFailureLimit( long assertionFailureLimit )
		{
			this.assertionFailureLimit = assertionFailureLimit;
		}

		@Override
		public ProjectBlueprint where( File where )
		{
			projectFile = where;
			return this;
		}

		@Override
		public ProjectBlueprint requestsLimit( Long requests )
		{
			setRequestLimit( requests );
			return this;
		}

		@Override
		public ProjectBlueprint timeLimit( Long seconds )
		{
			setTimeLimit( seconds );
			return this;
		}

		@Override
		public ProjectBlueprint assertionLimit( Long assertionFailures )
		{
			setAssertionFailureLimit( assertionFailures );
			return this;
		}

		@Override
		public ProjectBlueprint label( String name )
		{
			setLabel( name );
			return this;
		}

		@Override
		public ProjectRef build()
		{
		   return assemble( this );
		}
	}
}
