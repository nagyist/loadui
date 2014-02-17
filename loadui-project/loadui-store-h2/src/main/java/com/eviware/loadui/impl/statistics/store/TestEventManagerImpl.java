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
package com.eviware.loadui.impl.statistics.store;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventRegistry;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.statistics.event.RemoteTestEventListener;
import com.eviware.loadui.util.statistics.event.TestEventReceiver;
import com.eviware.loadui.util.statistics.event.TestEventSupportImpl;
import com.eviware.loadui.util.testevents.AbstractTestEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestEventManagerImpl extends AbstractTestEventManager implements Releasable
{

	public static final Logger log = LoggerFactory.getLogger( TestEventManagerImpl.class );

	private final MessageEndpoint endpoint;
	private final RemoteTestEventListener eventListener;
	private final TestEventReceiver eventReceiver;

	public TestEventManagerImpl( final TestEventRegistry testEventRegistry,
										  ExecutionManager manager,
										  BroadcastMessageEndpoint endpoint,
										  AddressableRegistry addressableRegistry,
										  final TestEventInterpolator interpolator )
	{
		super( testEventRegistry );
		this.endpoint = endpoint;
		eventReceiver = new TestEventReceiver( manager )
		{
			@Override
			public void beforeReceivingEvent( String typeLabel, TestEvent.Source<?> source, TestEvent testEvent )
			{
				interpolator.interpolate( typeLabel, source, testEvent );
			}
		};
		eventListener = new RemoteTestEventListener( addressableRegistry, eventReceiver, testEventRegistry, observers );

		endpoint.addMessageListener( CHANNEL, eventListener );
	}

	@Override
	public void logTestEvent( final TestEvent.Source<? extends TestEvent> source, final TestEvent testEvent )
	{
		final TestEvent.Factory<TestEvent> factory = testEventRegistry.lookupFactory( testEvent.getType().getName() );

		if( factory != null )
		{
			String typeLabel = factory.getLabel();
			long timestamp = testEvent.getTimestamp();
			byte[] eventData = factory.getDataForTestEvent( testEvent );

			eventReceiver.receiveEvent( typeLabel, source, timestamp, eventData,
					new TestEventSupportImpl( factory, testEvent, observers ) );
		}
		else
		{
			log.warn( "No TestEvent.Factory capable of storing TestEvent: {}, of type: {} has been registered!",
					testEvent, testEvent.getType() );
		}
	}

	@Override
	public void release()
	{
		endpoint.removeMessageListener( eventListener );
	}


}
