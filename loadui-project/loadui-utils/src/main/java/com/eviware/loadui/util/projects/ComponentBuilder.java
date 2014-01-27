package com.eviware.loadui.util.projects;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.util.CanvasItemNameGenerator;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ComponentBuilder
{
	private ProjectItem project;
	private ComponentRegistry componentRegistry;
	private String labeled;
	private List<PropertyDescriptor> properties;
	private boolean returnConnection;
	private List<ComponentItem> children;

	private ComponentBuilder( ProjectItem project, ComponentRegistry componentRegistry, String label )
	{
		this.project = project;
		this.componentRegistry = componentRegistry;
		this.labeled = label;
		this.returnConnection = false;
		children = new ArrayList<ComponentItem>();
		properties = new ArrayList<PropertyDescriptor>();
	}

	public <T> ComponentBuilder property( String key, Class<T> propertyType, Object value )
	{
		properties.add( new PropertyDescriptor( key, propertyType, value ) );
		return this;
	}

	public ComponentBuilder child( ComponentItem... components )
	{
		for ( ComponentItem component : components ){
			children.add( component );
		}

		return this;
	}

	public ComponentBuilder returnLink( boolean returnLink )
	{
		this.returnConnection = returnLink;
		return this;
	}

	public ComponentItem build() throws ComponentCreationException
	{
		ComponentDescriptor descriptor = componentRegistry.findDescriptor( labeled );

		try
		{
			Preconditions.checkNotNull( descriptor );
		}
		catch( NullPointerException e )
		{
			throw new ComponentCreationException( "Component descriptor " + labeled + " does not exist in the component-registry." );
		}

		String label = CanvasItemNameGenerator.generateComponentName( project.getCanvas(), descriptor.getLabel() );
		ComponentItem component = project.createComponent( label, descriptor );

		connectToChildren( component );
		modifyProperties( component );

		if( returnConnection )
		{
			applyReturnConnection( component );
		}

		return component;
	}

	private void applyReturnConnection( ComponentItem component )
	{
		String runningTerminal = "runningTerminal";
		boolean hasRunningTerminal = component.getTerminalByName( runningTerminal ) != null;

		if( !children.isEmpty() && hasRunningTerminal )
		{
			component.getTerminalByName( runningTerminal );
			CanvasItem canvas = project.getProject().getCanvas();

			for( ComponentItem child : children )
			{
				if( child.getCategory().equals( RunnerCategory.CATEGORY ) )
				{
					Terminal currentlyRunning = child.getTerminalByName( RunnerCategory.CURRENLY_RUNNING_TERMINAL );
					Terminal runningInputTerminal = component.getTerminalByName( runningTerminal );
					canvas.connect( ( OutputTerminal )currentlyRunning, ( InputTerminal )runningInputTerminal );
				}
			}
		}
	}

	private void modifyProperties( ComponentItem component )
	{
		for( PropertyDescriptor<?> newProperty : properties )
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

	private void connectToChildren( ComponentItem component )
	{
		if( !children.isEmpty() )
		{

			Iterator<Terminal> terminals = component.getTerminals().iterator();
			Terminal parentTerminal = terminals.next();


			for( ComponentItem child : children )
			{
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

				project.getCanvas().connect( ( OutputTerminal )parentTerminal, ( InputTerminal )childTerminal );
			}
		}
	}

	public static WithNoArguments create()
	{
		return new WithNoArguments();
	}

	public static class WithNoArguments
	{

		public WithProject project( ProjectItem project )
		{
			return new WithProject( project );
		}
	}

	public static class WithProject
	{
		private ProjectItem project;

		private WithProject( ProjectItem project )
		{
			this.project = project;
		}

		public WithProjectAndComponentRegistry componentRegistry( ComponentRegistry registry )
		{
			return new WithProjectAndComponentRegistry( project, registry );
		}
	}

	public static class WithProjectAndComponentRegistry
	{

		private ProjectItem project;
		private ComponentRegistry componentRegistry;

		private WithProjectAndComponentRegistry( ProjectItem project, ComponentRegistry componentRegistry )
		{
			this.project = project;
			this.componentRegistry = componentRegistry;
		}

		public ComponentBuilder type( String label )
		{
			return new ComponentBuilder( project, componentRegistry, label );
		}
	}

	private class PropertyDescriptor<Type extends Class>
	{

		private Type type;
		private String key;
		private Object value;

		PropertyDescriptor( String propertyName, Type type, Object value )
		{
			this.type = type;
			this.key = propertyName;
			this.value = value;
		}

		public Type getType()
		{
			return type;
		}

		public String getKey()
		{
			return key;
		}

		public Object getValue()
		{
			return value;
		}
	}
}
