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
package com.eviware.loadui.api.execution;

import com.eviware.loadui.api.model.CanvasItem;

import java.util.List;

/**
 * Schedules test executions. The TestRunner may have a single test execution
 * running at any one time.
 * 
 * @author dain.nilsson
 */
public interface TestRunner
{

	/**
	 * Enqueues a new TestExecution to run. As long as no TestExecution is
	 * running, the earliest queued TestExecution will be run, until completion.
	 * 
	 * @param canvas
	 * @return
	 */
	public TestExecution enqueueExecution( CanvasItem canvas );

	/**
	 * Returns an ordered List containing all the queued (and running)
	 * TestExecutions. The TestExecutions will be run in sequential order.
	 * 
	 * @return
	 */
	public List<TestExecution> getExecutionQueue();

	/**
	 * Registers a TestExecutionTask to be part of a Phase. All tasks registered
	 * for a Phase will be invoked in parallel during that phase, and the phase
	 * will not complete until all tasks have completed. The TestRunner will only
	 * keep weak references to the registered tasks to avoid memory leaks, but
	 * this means that callers need to keep at least one reference to the task
	 * for as long as it is used, or it may be garbage collected.
	 * 
	 * @param task
	 * @param phases
	 */
	public void registerTask( TestExecutionTask task, Phase... phases );

	/**
	 * Runs a TestExecutionTask during a Phase. All tasks registered
	 * for a Phase will be invoked in parallel during that phase, and the phase
	 * will not complete until all tasks have completed. Unlike with the registerTask
	 * methods, a STRONG REFERENCE is kept to the task so it is guaranteed that the
	 * task will be run exactly once, or never if the phase is never triggered.
	 *
	 * @param task
	 * @param phase
	 */
	public void runTaskOnce( TestExecutionTask task, Phase phase );

	/**
	 * Unregisters a TestExecutionTask from a specific Phase.
	 * 
	 * @param task
	 * @param phases
	 */
	public void unregisterTask( TestExecutionTask task, Phase... phases );
}
