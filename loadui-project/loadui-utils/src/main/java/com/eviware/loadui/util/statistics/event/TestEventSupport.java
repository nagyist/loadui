package com.eviware.loadui.util.statistics.event;

import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.util.statistics.store.TestEventEntryImpl;

import java.util.Set;

public interface TestEventSupport
{

	public abstract TestEventEntryImpl createEntry( String typeLabel,
																	TestEvent.Source<?> source );

	public abstract Set<TestEventManager.TestEventObserver> observers();

	public TestEvent getTestEvent();

}
