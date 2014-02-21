package com.eviware.loadui.util.statistics.event;

import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.util.statistics.store.TestEventEntryImpl;

import java.util.Set;

public class TestEventSupportImpl implements TestEventSupport
{
	private final TestEvent.Factory<?> factory;
	private final TestEvent testEvent;
	private final Set<TestEventManager.TestEventObserver> observers;


	public TestEventSupportImpl( TestEvent.Factory<?> factory,
										  TestEvent testEvent,
										  Set<TestEventManager.TestEventObserver> observers )
	{
		this.factory = factory;
		this.testEvent = testEvent;
		this.observers = observers;
	}

	@Override
	public TestEventEntryImpl createEntry( String typeLabel,
														TestEvent.Source<?> source )
	{
		return new TestEventEntryImpl( testEvent, source.getLabel(),
				factory == null ? "Unknown" : factory.getLabel(), 0 );
	}

	@Override
	public Set<TestEventManager.TestEventObserver> observers()
	{
		return observers;
	}

	@Override
	public TestEvent getTestEvent()
	{
		return testEvent;
	}
}