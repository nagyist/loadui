/* 
 * Copyright 2011 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.util.test;

import java.util.Collections;

import org.junit.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static com.eviware.loadui.util.test.CustomMatchers.*;

import com.eviware.loadui.util.BeanInjector;

public class BeanInjectorMockerTest
{
	/**
	 * Dummy interface to create mocks for.
	 * 
	 * @author dain.nilsson
	 */
	private interface TestInterface
	{
	}

	/**
	 * Subclass of TestInterface, also used for testing.
	 * 
	 * @author dain.nilsson
	 */
	private interface TestInterfaceSubclass extends TestInterface
	{
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void shouldReturnMocks()
	{
		new BeanInjectorMocker();

		TestInterface beanOne = BeanInjector.getBean( TestInterface.class );
		TestInterface beanTwo = BeanInjector.getBean( TestInterface.class );

		assertThat( beanOne, mockObject() );
		assertThat( beanOne, instanceOf( TestInterface.class ) );

		assertThat( beanTwo, allOf( mockObject(), is( TestInterface.class ) ) );

		assertThat( beanOne, sameInstance( beanTwo ) );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void shouldReturnMappedBeanAddedInConstructor()
	{
		final TestInterface testBean = new TestInterface()
		{
		};

		new BeanInjectorMocker( Collections.<Class<?>, Object> singletonMap( TestInterface.class, testBean ) );

		TestInterface beanOne = BeanInjector.getBean( TestInterface.class );
		TestInterface beanTwo = BeanInjector.getBean( TestInterface.class );

		assertThat( beanOne, is( notMockObject() ) );
		assertThat( beanOne, is( instanceOf( TestInterface.class ) ) );
		assertThat( beanOne, is( sameInstance( testBean ) ) );

		assertThat( beanTwo, allOf( notMockObject(), is( TestInterface.class ), sameInstance( testBean ) ) );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void shouldReturnMappedBeanAddedByPut()
	{
		final TestInterface testBean = new TestInterface()
		{
		};

		new BeanInjectorMocker().put( TestInterface.class, testBean );

		TestInterface beanOne = BeanInjector.getBean( TestInterface.class );
		TestInterface beanTwo = BeanInjector.getBean( TestInterface.class );

		assertThat( beanOne, notMockObject() );
		assertThat( beanOne, is( TestInterface.class ) );
		assertThat( beanOne, sameInstance( testBean ) );

		assertThat( beanTwo, allOf( notMockObject(), is( TestInterface.class ), sameInstance( testBean ) ) );
	}

	@Test
	public void shouldNotReturnMappedBeanForSuperClass()
	{
		final TestInterfaceSubclass testSubclassBean = new TestInterfaceSubclass()
		{
		};

		new BeanInjectorMocker().put( TestInterfaceSubclass.class, testSubclassBean );

		TestInterface bean = BeanInjector.getBean( TestInterface.class );
		TestInterfaceSubclass subclassBean = BeanInjector.getBean( TestInterfaceSubclass.class );

		assertThat( bean, not( sameInstance( ( TestInterface )testSubclassBean ) ) );
		assertThat( bean, mockObject() );

		assertThat( subclassBean, sameInstance( testSubclassBean ) );
	}
}
