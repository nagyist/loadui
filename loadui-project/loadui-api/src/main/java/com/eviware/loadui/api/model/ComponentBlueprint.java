package com.eviware.loadui.api.model;


import java.util.List;

public interface ComponentBlueprint{

	public String getComponentType();

	public List<ComponentBlueprint> getChildren();

	public List<PropertyDescriptor> getProperties();

	public PropertyDescriptor getProperty( String id );

	public boolean isConcurrentUsers();

	public interface PropertyDescriptor<Type extends Class>{

		public Type getType();

		public String getKey();

		public Object getValue();

	}

}

