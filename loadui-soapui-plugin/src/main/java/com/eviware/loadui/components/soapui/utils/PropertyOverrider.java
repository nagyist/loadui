package com.eviware.loadui.components.soapui.utils;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class PropertyOverrider
{

	public static final String OVERRIDING_VALUE_PREFIX = "_valueToOverride_";

	protected static final Logger log = LoggerFactory.getLogger( PropertyOverrider.class );

	/**
	 * Applies context properties to a TestCase from an incoming message.
	 *
	 * @param testCase
	 * @param contextProperties
	 */
	public static void overrideTestCaseProperties( WsdlTestCase testCase, Collection<Property<?>> contextProperties )
	{
		log.debug( "1setting property:" );
		for( Property<?> contextProperty : contextProperties )
		{

			if( contextProperty.getKey().startsWith( OVERRIDING_VALUE_PREFIX ) )
			{
				log.debug( "setting property:" + contextProperty.getKey() + ", " + contextProperty.getValue() );
				testCase.setPropertyValue( contextProperty.getKey().replaceFirst( OVERRIDING_VALUE_PREFIX, "" ),
						contextProperty.getValue() + "" );
			}
		}
	}

	/**
	 * Applies triggerMessage properties to a TestCase from an incoming message.
	 *
	 * @param testCase
	 * @param triggerMessage
	 */
	public static void overrideTestCaseProperties( WsdlTestCase testCase, TerminalMessage triggerMessage )
	{
		for( String name : testCase.getPropertyNames() )
		{
			if( triggerMessage.containsKey( name ) )
				testCase.setPropertyValue( name, String.valueOf( triggerMessage.get( name ) ) );
		}
	}

	public static List<TestProperty> applyOveriddenProperties( List<TestProperty> customProperties,
																				  ComponentContext context )
	{
		for( TestProperty p : customProperties )
		{
			Property<?> savedProperty = context.getProperty( OVERRIDING_VALUE_PREFIX + p.getName() );

			if( savedProperty != null )
			{
				p.setValue( savedProperty.getValue() + "" );
			}
		}

		return customProperties;
	}

}
