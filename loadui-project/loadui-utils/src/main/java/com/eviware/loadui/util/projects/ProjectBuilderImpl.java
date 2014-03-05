package com.eviware.loadui.util.projects;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.model.*;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.util.CanvasItemNameGenerator;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ProjectBuilderImpl implements ProjectBuilder
{
	private WorkspaceProvider workspaceProvider;
	private ComponentRegistry componentRegistry;

	Logger log = LoggerFactory.getLogger( ProjectBuilder.class );

	public ProjectBuilderImpl( ComponentRegistry componentRegistry, WorkspaceProvider workspaceProvider )
	{
		this.componentRegistry = componentRegistry;
		this.workspaceProvider = workspaceProvider;
	}

	@Override
	public LoadUiProjectBlueprint create()
	{
		return new LoadUiProjectBlueprint();
	}

	private ProjectRef assembleProjectByBlueprint( LoadUiProjectBlueprint blueprint )
	{
		if( !workspaceProvider.isWorkspaceLoaded() )
		{
			workspaceProvider.loadDefaultWorkspace();
		}

		try
		{
			File where = File.createTempFile( "loadui-project-", ".xml" );
			ProjectRef project = workspaceProvider.getWorkspace().createProject( where, where.getName(), true );

			project.getProject().setLimit( CanvasItem.REQUEST_COUNTER, blueprint.getRequestLimit() );
			project.getProject().setLimit( CanvasItem.TIMER_COUNTER, blueprint.getTimeLimit() );
			project.getProject().setLimit( CanvasItem.FAILURE_COUNTER, blueprint.getAssertionFailureLimit() );

			assembleComponentsByBlueprint( project, blueprint.getComponentBlueprints() );

			project.getProject().save();
			project.setEnabled( false );

			File directory = blueprint.getProjectDirectory();

			if( !directory.exists() ){
				directory.mkdirs();
			}

			where = new File( directory.getPath() + "/" + project.getProjectFile().getName() );



			Files.move( project.getProjectFile(), where );

			return project;
		}
		catch( IOException e )
		{
			log.error( "Unable to assemble project from blueprint " + e.getMessage() );
			e.printStackTrace();
		}
		return null;
	}

	private void assembleComponentsByBlueprint( ProjectRef project, List<ComponentBlueprint> componentBlueprints )
	{

		for( ComponentBlueprint blueprint : componentBlueprints )
		{
			try
			{
				assembleComponent( project, blueprint );
			}
			catch( ComponentCreationException e )
			{
				log.error( "Cannot create component. " + e.getMessage() );
			}
		}
	}

	public ComponentItem assembleComponent( ProjectRef project, ComponentBlueprint blueprint ) throws ComponentCreationException
	{
		ComponentDescriptor descriptor = componentRegistry.findDescriptor( blueprint.getComponentType() );

		try
		{
			Preconditions.checkNotNull( descriptor );
		}
		catch( NullPointerException e )
		{
			throw new ComponentCreationException( "Component descriptor " + blueprint.getComponentType() + " does not exist in the component-registry." );
		}
		String label = CanvasItemNameGenerator.generateComponentName( project.getProject(), descriptor.getLabel() );
		ComponentItem parentComponent = project.getProject().createComponent( label, descriptor );

		if( !blueprint.getChildren().isEmpty() )
		{
			for( ComponentBlueprint child : blueprint.getChildren() )
			{

				ComponentItem childComponent = assembleComponent( project, child );

				connectToChild( project, parentComponent, childComponent );

				if( blueprint.isConcurrentUsers() )
				{
					applyConcurrentUsersConnection( project, parentComponent, childComponent );
				}
			}
		}
		modifyProperties( parentComponent, blueprint.getProperties() );

		return parentComponent;
	}

	private void applyConcurrentUsersConnection( ProjectRef project, ComponentItem parentComponent, ComponentItem childComponent )
	{

		String sampleCountTerminal = "Sample Count";
		boolean parentHasSampleCountTerminal = parentComponent.getTerminalByName( sampleCountTerminal ) != null;

		if( parentHasSampleCountTerminal )
		{
			parentComponent.getTerminalByName( sampleCountTerminal );
			CanvasItem canvas = project.getProject().getCanvas();
			if( childComponent.getCategory().equalsIgnoreCase( RunnerCategory.CATEGORY ) )
			{
				Terminal currentlyRunning = childComponent.getTerminalByName( RunnerCategory.CURRENLY_RUNNING_TERMINAL );
				Terminal runningInputTerminal = parentComponent.getTerminalByName( sampleCountTerminal );
				canvas.connect( ( OutputTerminal )currentlyRunning, ( InputTerminal )runningInputTerminal );
			}
		}
		else
		{
			log.error( "Cannot apply additional connection for concurrent users. Can't find the runningTerminal on child component or Sample Count on parent." );
		}
	}

	private void modifyProperties( ComponentItem component, List<ComponentBlueprint.PropertyDescriptor> properties )
	{
		for( ComponentBlueprint.PropertyDescriptor newProperty : properties )
		{
			Property<?> componentProperty = component.getProperty( newProperty.getKey() );

			if( newProperty.getType().getSimpleName().equals( componentProperty.getType().getSimpleName() ) )
			{
				componentProperty.setValue( newProperty.getValue() );
			}
			else
			{
				throw new IllegalArgumentException( "Value of property " + newProperty.getKey() + " is of type " + component.getType() + " and is not applicable to " + newProperty.getType() );
			}
		}
	}

	private void connectToChild( ProjectRef project, ComponentItem parent, ComponentItem child )
	{
		Iterator<Terminal> terminals = parent.getTerminals().iterator();
		Terminal parentTerminal = terminals.next();

		while( parentTerminal instanceof InputTerminal )
		{
			parentTerminal = terminals.next();
		}

		Iterator<Terminal> childTerminals = child.getTerminals().iterator();
		Terminal childTerminal = childTerminals.next();

		while( childTerminal instanceof OutputTerminal )
		{
			childTerminal = childTerminals.next();
		}

		project.getProject().getCanvas().connect( ( OutputTerminal )parentTerminal, ( InputTerminal )childTerminal );
	}


	public class LoadUiProjectBlueprint implements ProjectBuilder.ProjectBlueprint
	{
		private static final long DEFAULT_REQUEST_LIMIT = 0;
		private static final long DEFAULT_ASSERTION_FAILURE_LIMIT = 0;
		private static final long DEFAULT_TIME_LIMIT = 0;

		private File projectFile;
		private List<ComponentBlueprint> components;
		private String label;
		private long requestLimit;
		private long timeLimit;
		private long assertionFailureLimit;

		private LoadUiProjectBlueprint()
		{
			setComponentBlueprints( new ArrayList<ComponentBlueprint>() );
			setRequestLimit( DEFAULT_REQUEST_LIMIT );
			setAssertionFailureLimit( DEFAULT_ASSERTION_FAILURE_LIMIT );
			setTimeLimit( DEFAULT_TIME_LIMIT );
		}

		private File getProjectDirectory()
		{
			return projectFile;
		}

		private void setProjectDirectory( File where )
		{
			this.projectFile = where;
		}

		private List<ComponentBlueprint> getComponentBlueprints()
		{
			return components;
		}

		private void setComponentBlueprints( List<ComponentBlueprint> components )
		{
			this.components = components;
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
		public ProjectBlueprint where( File folder )
		{
			projectFile = folder;
			return this;
		}

		@Override
		public ProjectBlueprint components( ComponentBlueprint... components )
		{
			Collections.addAll( this.components, components );
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
		public ProjectBlueprint scenario( String label )
		{
			//TODO Create scenario
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
			return assembleProjectByBlueprint( this );
		}
	}
}
