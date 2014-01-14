package com.eviware.loadui.impl.model.canvas.project;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.*;
import com.eviware.loadui.util.CanvasItemNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by osten on 1/14/14.
 */
public class ProjectBuilderImpl implements ProjectBuilder
{
	Logger log = LoggerFactory.getLogger( ProjectBuilder.class );

	private static final String FIXED_RATE_GENERATOR = "Fixed Rate";
	private static final String WEB_RUNNER = "Web Page Runner";

	ComponentRegistry componentRegistry;

	private ProjectBuilder builder;
	private WorkspaceProvider workspaceProvider;
	private ProjectRef project;
	private CanvasItem canvas;

	private ComponentItem latestComponent;
	private boolean connectCurrentComponentToPreviousComponent;

	public ProjectBuilderImpl( WorkspaceProvider workspaceProvider, ComponentRegistry componentRegistry )
	{
		builder = this;
		this.componentRegistry = componentRegistry;
		this.workspaceProvider = workspaceProvider;
		workspaceProvider.loadDefaultWorkspace();
		canvas = project.getProject().getCanvas();
	}

	@Override
	public ProjectBuilder create()
	{

		try
		{
			create( File.createTempFile( "loadui-project", ".xml" ) );
		}
		catch( IOException e )
		{
			log.error( "cannot create a temporary project" );
		}

		connectCurrentComponentToPreviousComponent = false;
		latestComponent = null;
		return builder;

	}

	@Override
	public ProjectBuilder create( File where )
	{
		project = workspaceProvider.getWorkspace().createProject( where, where.getName(), true );
		return builder;
	}

	@Override
	public ProjectBuilder label( String newLabel )
	{
		project.setLabel( newLabel );
		return builder;
	}

	@Override
	public ProjectBuilder fixedRate( int rate )
	{
		ComponentDescriptor descriptor = componentRegistry.findDescriptor( FIXED_RATE_GENERATOR );
		createComponent( descriptor );

		return builder;
	}

	@Override
	public ProjectBuilder connectTo()
	{
		connectCurrentComponentToPreviousComponent = true;
		return builder;
	}

	@Override
	public ProjectBuilder connectTo( int terminalOnComponentA, int terminalOnComponentB )
	{
		connectCurrentComponentToPreviousComponent = true;
		return builder;
	}

	@Override
	public ProjectBuilder webRunner( String url )
	{
		return builder;
	}

	@Override
	public ProjectItem build()
	{
		return project.getProject();
	}

	private ComponentItem createComponent( ComponentDescriptor descriptor ){
		try
		{
			return canvas.createComponent( CanvasItemNameGenerator.generateComponentName( canvas, descriptor.getLabel() ), descriptor );
		}
		catch( ComponentCreationException e )
		{
			System.err.println("Component" + descriptor.getLabel() + " cannot be created: " );
		}
		return null;
	}

	private void connect()
	{

	}
}
