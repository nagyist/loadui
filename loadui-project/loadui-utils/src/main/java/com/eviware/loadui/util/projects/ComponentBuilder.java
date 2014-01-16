package com.eviware.loadui.util.projects;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.util.CanvasItemNameGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by osten on 1/16/14.
 */
public class ComponentBuilder
{

	private ProjectItem project;
	private ComponentRegistry componentRegistry;
	private String labeled;
	private List<Property> properties;
	private List<ComponentItem> children;

	private ComponentBuilder( ProjectItem project, ComponentRegistry componentRegistry, String label){
		this.project = project;
		this.componentRegistry = componentRegistry;
		this.labeled = label;
		children = new ArrayList<ComponentItem>();
		properties = new ArrayList<Property>();
	}

	public ComponentBuilder property( Property property ){
		properties.add( property );
		return this;
	}

	public ComponentBuilder child( ComponentItem component ){
		children.add( component );
		return this;
	}

	public ComponentItem build(){
		ComponentDescriptor descriptor = componentRegistry.findDescriptor( labeled );
		try{
			ComponentItem component = project.createComponent( CanvasItemNameGenerator.generateComponentName( project.getCanvas(), descriptor.getLabel() ), descriptor );

			connectToChildren( component );

			for(Property p : properties ){
				component.getProperty( p.getKey() ).setValue( p.getValue() );
			}

			return component;
		}catch(ComponentCreationException e){
			System.err.println("Cannot create component " + descriptor.getLabel());
			return null;
		}
	}

	private void connectToChildren( ComponentItem component ){
		if(!children.isEmpty()){

			Iterator<Terminal> terminals = component.getTerminals().iterator();
			Terminal parentTerminal = terminals.next();


			for(ComponentItem child : children){
				while( parentTerminal instanceof InputTerminal ){
					parentTerminal = terminals.next();
				}

				Iterator<Terminal> childTerminals = child.getTerminals().iterator();
				Terminal childTerminal = childTerminals.next();

				while( childTerminal instanceof OutputTerminal ){
					childTerminal = childTerminals.next();
				}

				project.getCanvas().connect( ( OutputTerminal) parentTerminal, ( InputTerminal) childTerminal );

			}
		}
	}

	public static WithNoArguments create( ) {
		return new WithNoArguments();
	}

	public static class WithNoArguments
	{

		public WithProject project( ProjectItem project ){
			return new WithProject( project );
		}
	}

	public static class WithProject
	{
		private ProjectItem project;

		private WithProject( ProjectItem project ){
			this.project = project;
		}

		public WithProjectAndComponentRegistry componentRegistry( ComponentRegistry registry ){
			return new WithProjectAndComponentRegistry( project, registry);
		}
	}

	public static class WithProjectAndComponentRegistry{

		private ProjectItem project;
		private ComponentRegistry componentRegistry;

		private WithProjectAndComponentRegistry( ProjectItem project, ComponentRegistry componentRegistry){
			this.project = project;
			this.componentRegistry = componentRegistry;
		}

		public ComponentBuilder labeled( String label ){
			return new ComponentBuilder( project, componentRegistry, label);
		}
	}


}
