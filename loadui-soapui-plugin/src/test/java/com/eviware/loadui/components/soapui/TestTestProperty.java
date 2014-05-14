package com.eviware.loadui.components.soapui;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestProperty;

	public class TestTestProperty implements TestProperty
	{

		private String name;
		private String value;

		public TestTestProperty( String name, String value )
		{
			this.name = name;
			this.value = value;

		}

		@Override
		public String getDefaultValue()
		{
			return null;
		}

		@Override
		public String getDescription()
		{
			return null;
		}

		@Override
		public ModelItem getModelItem()
		{
			return null;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public SchemaType getSchemaType()
		{
			return null;
		}

		@Override
		public QName getType()
		{
			return null;
		}

		@Override
		public String getValue()
		{
			return value;
		}

		@Override
		public boolean isReadOnly()
		{
			return false;
		}

		@Override
		public boolean isRequestPart()
		{
			return false;
		}

		@Override
		public void setValue( String value )
		{
			this.value = value;
		}

		

	}