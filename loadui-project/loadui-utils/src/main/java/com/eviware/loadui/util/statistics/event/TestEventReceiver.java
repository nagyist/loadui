package com.eviware.loadui.util.statistics.event;

import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;

public class TestEventReceiver
{
	protected final ExecutionManager manager;

	public TestEventReceiver( ExecutionManager manager )
	{
		this.manager = manager;
	}


	public void beforeReceivingEvent( String typeLabel, TestEvent.Source<?> source, TestEvent testEvent )
	{
		// should be overridden by sub-classes
	}

	public void receiveEvent(
			String typeLabel,
			TestEvent.Source<?> source,
			long timestamp,
			byte[] eventData,
			TestEventSupport testEventSupport )
	{
		beforeReceivingEvent( typeLabel, source, testEventSupport.getTestEvent() );
		manager.writeTestEvent( typeLabel, source, timestamp, eventData, 0 );
		TestEvent.Entry entry = testEventSupport.createEntry( typeLabel, source );

		for( TestEventManager.TestEventObserver observer : testEventSupport.observers() )
		{
			observer.onTestEvent( entry );
		}
	}


}
