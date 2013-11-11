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
package com.eviware.loadui.util.execution;

import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.execution.TestState;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.util.BeanInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class TestExecutionUtils
{
	private static final Logger log = LoggerFactory.getLogger( TestExecutionUtils.class );
	public static final TestRunner testRunner = BeanInjector.getBean( TestRunner.class );

	public static TestExecution startCanvas( CanvasItem canvas )
	{
		log.info( "Trying to start canvas {}", canvas.getLabel() );
		TestExecution currentExecution = getCurrentExecution();

		if( currentExecution != null && currentExecution.getState() != TestState.COMPLETED )
		{
			log.warn( "Cannot start an Execution while another is running! Will ignore request!" );
			return currentExecution;
		}
		else
		{
			log.info( "Requesting new Execution to start on canvas {}", canvas.getDescription() );
			return testRunner.enqueueExecution( canvas );
		}
	}

	public static TestExecution stopCanvas( CanvasItem canvas )
	{
		TestExecution execution = getCurrentExecution();
		if( execution != null )
		{
			if( execution.getCanvas() == canvas )
				execution.complete();
			else
				log.warn( "Tried to stop canvas which is not associated with currently running execution!" );
		}
		else
		{
			log.warn( "Request to stop canvas '{}' being ignored as there is no execution running", canvas.getDescription() );
		}
		return execution;
	}

	public static boolean isExecutionRunning()
	{
		TestExecution execution = getCurrentExecution();
		return execution != null && execution.getState() != TestState.COMPLETED;
	}

	/**
	 * @return Currently running CanvasItem or null if nothing is running
	 */

	public static CanvasItem getCurrentlyRunningCanvasItem()
	{
		if( !isExecutionRunning() )
		{
			return null;
		}
		return getCurrentExecution().getCanvas();
	}

	private static TestExecution getCurrentExecution()
	{
		List<TestExecution> queuedExecutions = testRunner.getExecutionQueue();
		if( !queuedExecutions.isEmpty() )
		{
			return queuedExecutions.get( 0 );
		}
		return null;
	}

}
