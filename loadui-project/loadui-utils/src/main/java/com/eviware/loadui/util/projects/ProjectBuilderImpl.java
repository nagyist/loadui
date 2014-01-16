package com.eviware.loadui.util.projects;

import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
		blueprint.setWhere( where );
		return this;
	}

	@Override
	public ProjectBuilder label( String name )
	{
		blueprint.setLabel( name );
		return this;
	}

	@Override
	public ProjectBuilder save()
	{
		blueprint.setSave( true );
   	return this;
	}

	@Override
	public ProjectBuilder components( ComponentItem... components )
	{
		/*if(components.length > 0){

			ComponentItem parentComponent = components[0];

         for( int i = 1; i < components.length; i++ ){
            ComponentItem currentComponent = components[i];
			}
		}*/
		return this;
	}

	@Override
	public ProjectItem build()
	{
		ProjectRef project = assemble( blueprint );
		workspaceProvider.loadDefaultWorkspace();
		return project.getProject();
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
		ProjectRef project = workspaceProvider.getWorkspace().createProject( blueprint.getWhere(), blueprint.getLabel(), true );
		project.setLabel( blueprint.getLabel() );

		if( blueprint.isSave() ){
		 	save( project.getProjectFile() );
		}

		return project;
	}

	private class ProjectBlueprint
	{
		private boolean save;
		private File where;
		private List<List<ComponentItem>> componentChains;
		private String label;

		public ProjectBlueprint(){

			setComponentChains( new ArrayList<List<ComponentItem>>() );
			setSave( false );
			try
			{
				setWhere( File.createTempFile( "loadui-project", ".xml" ) );
			}
			catch( IOException e )
			{
				log.error( "cannot create a temporary project" );
			}
			setLabel( where.getName() );
		}

		public boolean isSave()
		{
			return save;
		}

		public void setSave( boolean shouldSave )
		{
			this.save = shouldSave;
		}

		public File getWhere()
		{
			return where;
		}

		public void setWhere( File where )
		{
			this.where = where;
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
	}
}
