package com.eviware.loadui.util.projects;

import com.eviware.loadui.api.model.ComponentBlueprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ComponentBuilder
{

	/**
	 * LoadUI known components
	 */
	public enum LoadUIComponents
	{
		FIXED_RATE( "Fixed Rate" ),
		WEB_RUNNER( "Web Page Runner" ),
		NEW_WEB_RUNNER( "New Web Runner" ),
		REST_RUNNER( "REST Runner" ),
		FIXED_LOAD( "Fixed Load" ),
		RAMP_LOAD( "Ramp Load" ),
		RAMP( "Ramp" ),
		RAMP_SEQUENCE( "Ramp Sequence" ),
		DEJA_RUNNER( "DejaClick Runner" ),
		TABLE_LOG( "Table Log" );

		private final String name;

		private LoadUIComponents( String name )
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}


	}

	private String component;
	private List<ComponentBlueprint> child;
	private List<ComponentBlueprint.PropertyDescriptor> properties;
	private boolean concurrentUsers;

	private ComponentBuilder( LoadUIComponents component )
	{
		this( component.getName() );
	}

	private ComponentBuilder( String component )
	{
		this.component = component;
		this.child = new ArrayList<>();
		this.properties = new ArrayList<>();
		this.concurrentUsers = false;
	}

	public ComponentBuilder child( ComponentBlueprint... componentBlueprints )
	{
		Collections.addAll( this.child, componentBlueprints );
		return this;
	}

	public <T> ComponentBuilder property( String key, Class<T> propertyType, Object value )
	{
		this.properties.add( new PropertyDescriptor( key, propertyType, value ) );
		return this;
	}

	public ComponentBuilder concurrent()
	{
		this.concurrentUsers = true;
		return this;
	}

	public ComponentBuilder arrival()
	{
		this.concurrentUsers = false;
		return this;
	}

	public ComponentBlueprintImpl build()
	{
		return new ComponentBlueprintImpl( component, child, properties, concurrentUsers );
	}

	public static WithType create()
	{
		return new WithType();
	}

	public static class WithType
	{
		public ComponentBuilder type( LoadUIComponents component )
		{
			return new ComponentBuilder( component );
		}

		public ComponentBuilder type( String component )
		{
			return new ComponentBuilder( component );
		}
	}

	public class ComponentBlueprintImpl implements ComponentBlueprint
	{
		private String componentType;
		private List<ComponentBlueprint> children;
		private List<PropertyDescriptor> properties;
		private boolean concurrentUsers;

		public ComponentBlueprintImpl( String componentType, List<ComponentBlueprint> children, List<ComponentBlueprint.PropertyDescriptor> properties, boolean concurrentUsers )
		{

			this.componentType = componentType;
			this.children = children;
			this.properties = properties;
			this.concurrentUsers = concurrentUsers;
		}

		public String getComponentType()
		{
			return componentType;
		}

		public List<ComponentBlueprint> getChildren()
		{
			return children;
		}

		public List<PropertyDescriptor> getProperties()
		{
			return properties;
		}

		@Override
		public PropertyDescriptor getProperty( String id )
		{
			for( PropertyDescriptor property : properties )
			{
				if( property.getKey().equals( id ) )
				{
					return property;
				}
			}

			return null;
		}

		public boolean isConcurrentUsers()
		{
			return concurrentUsers;
		}
	}

	public class PropertyDescriptor<Type extends Class> implements ComponentBlueprint.PropertyDescriptor<Type>
	{
		private Type type;
		private String key;
		private Object value;

		private PropertyDescriptor( String propertyName, Type type, Object value )
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
