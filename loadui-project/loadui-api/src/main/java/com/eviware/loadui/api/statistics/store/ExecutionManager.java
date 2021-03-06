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
package com.eviware.loadui.api.statistics.store;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.testevents.TestEvent;

import java.util.Collection;

/**
 * Manages existing Executions, creating new ones, etc.
 * 
 * @author dain.nilsson
 */
public interface ExecutionManager extends EventFirer
{
	/**
	 * CollectionEvent key for recent Executions.
	 */
	public static final String RECENT_EXECUTIONS = ExecutionManager.class.getName() + "@recentexecutions";

	/**
	 * CollectionEvent key for archived Executions.
	 */
	public static final String ARCHIVE_EXECUTIONS = ExecutionManager.class.getName() + "@archivedexecutions";

	/**
	 * Gets the current Execution. Returns null if no Execution is currently
	 * running.
	 * 
	 * @return
	 */
	public Execution getCurrentExecution();

	/**
	 * Creates and starts a new Execution, making it current.
	 * 
	 * @param executionId
	 *           A unique ID to use for the Execution.
	 * @param startTime
	 *           The start time for the Execution as a timestamp.
	 * @param label
	 *           The label to show the user for the execution
	 * @param fileName
	 *           The name to use for the file/directory which will store the
	 *           execution data.
	 * @return
	 */
	public Execution startExecution( String executionId, long startTime, String label, String fileName );

	/**
	 * Creates and starts a new Execution, making it current.
	 * 
	 * @param executionId
	 * @param startTime
	 * @return
	 */
	public Execution startExecution( String executionId, long startTime, String label );

	/**
	 * @see ExecutionManager#getCurrentExecution()
	 */
	public Execution startExecution( String executionId, long startTime );

	/**
	 * Stops current execution.
	 * 
	 * @return
	 */
	public void stopExecution();

	/**
	 * Archive the execution with the given id.
	 * 
	 * @param executionId
	 */
	public void archiveExecution( String executionId );

	/**
	 * Registers a TrackDescriptor.
	 * 
	 * @param trackDescriptor
	 */
	public void registerTrackDescriptor( TrackDescriptor trackDescriptor );

	/**
	 * Unregisters a TrackDescriptor.
	 * 
	 * @param trackId
	 */
	public void unregisterTrackDescriptor( String trackId );

	/**
	 * Creates a new track in the current Execution with the given id and
	 * structure. If a Track with the given ID already exists, it will be
	 * returned.
	 * 
	 * @param trackId
	 * @return
	 */
	public Track getTrack( String trackId );

	/**
	 * Writes an Entry to the Track for the specified source. Instead of calling
	 * this method directly, usually the at( int ) method is used.
	 * 
	 * @param entry
	 */
	public void writeEntry( String trackId, Entry entry, String source );

	/**
	 * @see #writeEntry(String, Entry, String)
	 */
	public void writeEntry( String trackId, Entry entry, String source, int interpolationLevel );

	/**
	 * Gets the last stored Entry for a particular source, which is cached in
	 * memory.
	 * 
	 * @return
	 */
	public Entry getLastEntry( String trackId, String source );

	/**
	 * @see Entry(String, String)
	 */
	public Entry getLastEntry( String trackId, String source, int interpolationLevel );

	/**
	 * Adds a ValueListener for new Entries that satisfy the given constraints.
	 * 
	 * @param trackId
	 * @param source
	 * @param interpolationLevel
	 * @param listener
	 */
	public void addEntryListener( String trackId, String source, int interpolationLevel,
			ListenableValue.ValueListener<? super Entry> listener );

	/**
	 * Removes a previously added ValueListener
	 * 
	 * @param trackId
	 * @param source
	 * @param interpolationLevel
	 * @param listener
	 */
	public void removeEntryListener( String trackId, String source, int interpolationLevel,
			ListenableValue.ValueListener<? super Entry> listener );

	/**
	 * Writes the data for a TestEvent to the current Execution.
	 * 
	 * @param typeLabel
	 * @param source
	 * @param timestamp
	 * @param testEventData
	 * @param interpolationLevel
	 */
	public void writeTestEvent( String typeLabel, TestEvent.Source<?> source, long timestamp, byte[] testEventData,
			int interpolationLevel );

	/**
	 * Gets a list of the names of all available Executions.
	 * 
	 * @return
	 */
	public Collection<Execution> getExecutions();

	/**
	 * Gets a reference to a specific Execution by its ID.
	 * 
	 * @param executionId
	 * @return
	 */
	public Execution getExecution( String executionId );

	/**
	 * Gets the base directory where executions will be saved
	 */
	public String getDBBaseDir();

	/**
	 * Add execution listener
	 * 
	 * @param el
	 */
	public void addExecutionListener( ExecutionListener el );

	/**
	 * remove all listeners
	 */
	public void removeAllExecutionListeners();

	/**
	 * removes added execution listener
	 */
	public void removeExecutionListener( ExecutionListener el );

	/**
	 * ExecutionManager States, based on execution events. state of manager
	 * should be handled internaly ( setting the state )
	 * 
	 * @author robert
	 * 
	 */
	public enum State
	{
		STARTED, STOPPED
	}

	/**
	 * Return current state of ExecutionManager
	 */
	public State getState();

}
