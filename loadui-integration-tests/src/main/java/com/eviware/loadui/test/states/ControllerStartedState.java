/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.test.states;

import com.eviware.loadui.test.ControllerWrapper;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.util.concurrent.SettableFuture;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import java.util.concurrent.TimeUnit;

public class ControllerStartedState extends TestState
{
	private static ControllerStartedState instance;

	public ControllerWrapper controller;

	public static synchronized ControllerStartedState getState()
	{
		if( instance == null )
			instance = new ControllerStartedState();
		return instance;
	}

	private ControllerStartedState()
	{
		super( "Controller Started" );
	}

	protected ControllerStartedState( String name )
	{
		super( name );
	}

	@Override
	protected TestState parentState()
	{
		return TestState.ROOT;
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		controller = new ControllerWrapper();
		BeanInjector.setBundleContext( controller.getBundleContext() );
	}

	@Override
	protected void exitToParent()
	{
		try
		{
			controller.stop();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	public BundleContext getBundleContext()
	{
		return controller.getBundleContext();
	}

	public <T> T getService( Class<T> cls ) throws Exception
	{
		final BundleContext context = getBundleContext();

		final SettableFuture<T> beanFuture = SettableFuture.create();

		ServiceListener listener = new ServiceListener()
		{
			@Override
			public void serviceChanged( ServiceEvent serviceEvent )
			{
				if( serviceEvent.getType() == ServiceEvent.REGISTERED )
				{
					ServiceReference ref = serviceEvent.getServiceReference();
					beanFuture.set( ( T )context.getService( ref ) );
				}
			}
		};

		context.addServiceListener( listener, "(objectclass=" + cls.getName() + ")" );

		ServiceReference ref = context.getServiceReference( cls );
		if( ref != null )
			beanFuture.set( ( T )context.getService( ref ) );

		T service = beanFuture.get( 5, TimeUnit.SECONDS );
		context.removeServiceListener( listener );

		return service;
	}
}
