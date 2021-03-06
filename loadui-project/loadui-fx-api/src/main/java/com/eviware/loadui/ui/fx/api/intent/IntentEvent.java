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
package com.eviware.loadui.ui.fx.api.intent;

import javafx.event.Event;
import javafx.event.EventType;

import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.api.traits.Labeled;

@SuppressWarnings( "serial" )
public class IntentEvent<T> extends Event
{
	public static final EventType<IntentEvent<? extends Object>> ANY = new EventType<>( Event.ANY, "INTENT" );

	public static final EventType<IntentEvent<? extends Object>> INTENT_OPEN = new EventType<>( ANY, "INTENT_OPEN" );

	public static final EventType<IntentEvent<? extends Object>> INTENT_CLOSE = new EventType<>( ANY, "INTENT_CLOSE" );

	public static final EventType<IntentEvent<? extends Object>> INTENT_CREATE = new EventType<>( ANY, "INTENT_CREATE" );

	public static final EventType<IntentEvent<? extends Labeled.Mutable>> INTENT_RENAME = new EventType<>( ANY,
			"INTENT_RENAME" );

	public static final EventType<IntentEvent<? extends Object>> INTENT_CLONE = new EventType<>( ANY, "INTENT_CLONE" );

	public static final EventType<IntentEvent<? extends Deletable>> INTENT_DELETE = new EventType<>( ANY,
			"INTENT_DELETE" );

	public static final EventType<IntentEvent<? extends Runnable>> INTENT_RUN_BLOCKING = new EventType<>( ANY,
			"INTENT_RUN_BLOCKING" );
	
	public static final EventType<IntentEvent<? extends AbortableTask>> INTENT_RUN_BLOCKING_ABORTABLE = new EventType<>( ANY,
			"INTENT_RUN_BLOCKING_ABORTABLE" );

	public static final EventType<IntentEvent<? extends Object>> INTENT_SAVE = new EventType<>( ANY, "INTENT_SAVE" );

	private final T arg;

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static <T> IntentEvent<T> create( EventType<IntentEvent<? extends T>> eventType, T arg )
	{
		return new IntentEvent( eventType, arg );
	}

	private IntentEvent( EventType<IntentEvent<T>> eventType, T arg )
	{
		super( NULL_SOURCE_TARGET, NULL_SOURCE_TARGET, eventType );
		this.arg = arg;
	}

	public T getArg()
	{
		return arg;
	}

	@Override
	public String toString()
	{
		return getEventType() + "[arg=" + arg + "]";
	}
}
