package com.eviware.loadui.util.statistics.event;


import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.testevents.TestEventRegistry;
import com.eviware.loadui.util.testevents.UnknownTestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class RemoteTestEventListener implements MessageListener
{
	static final Logger log = LoggerFactory.getLogger( RemoteTestEventListener.class );

	private final AddressableRegistry addressableRegistry;
	private final TestEventReceiver eventReceiver;
	private final TestEventRegistry testEventRegistry;
	private final Set<TestEventManager.TestEventObserver> observers;

	public RemoteTestEventListener( AddressableRegistry addressableRegistry,
											  TestEventReceiver eventReceiver,
											  TestEventRegistry testEventRegistry,
											  Set<TestEventManager.TestEventObserver> observers )
	{
		this.addressableRegistry = addressableRegistry;
		this.eventReceiver = eventReceiver;
		this.testEventRegistry = testEventRegistry;
		this.observers = observers;
	}

	@Override
	public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
	{
		@SuppressWarnings( "unchecked" )
		List<Object> args = ( List<Object> )data;

		String type = ( String )args.get( 0 );
		String typeLabel = ( String )args.get( 1 );
		TestEvent.Source<?> source = ( TestEvent.Source<?> )addressableRegistry.lookup( ( String )args.get( 2 ) );
		if( source == null )
		{
			log.debug( "No object found with ID: {}", args.get( 2 ) );
			return;
		}
		long timestamp = ( Long )args.get( 3 );
		byte[] eventData = ( byte[] )args.get( 4 );
		final TestEvent.Factory<?> factory = testEventRegistry.lookupFactory( type );

		if( factory == null )
			log.debug( "No factory found!" );
		else
			log.debug( "Factory found: {}", factory );

		final TestEvent testEvent = factory == null ?
				new UnknownTestEvent( timestamp ) :
				factory.createTestEvent( timestamp, source.getData(), eventData );

		eventReceiver.receiveEvent( typeLabel, source, timestamp, eventData,
				new TestEventSupportImpl( factory, testEvent, observers ) );
	}

}
