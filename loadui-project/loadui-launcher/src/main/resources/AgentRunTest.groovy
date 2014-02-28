//
// Copyright 2011 SmartBear Software
//
// Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
// versions of the EUPL (the "Licence");
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at:
//
// http://ec.europa.eu/idabc/eupl5
//
// Unless required by applicable law or agreed to in writing, software distributed under the Licence is
// distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the Licence for the specific language governing permissions and limitations
// under the Licence.
//
import com.eviware.loadui.api.events.CollectionEvent
import com.eviware.loadui.api.events.EventHandler
import com.eviware.loadui.api.execution.TestRunner
import com.eviware.loadui.api.model.CanvasItem
import com.eviware.loadui.api.model.WorkspaceItem
import com.eviware.loadui.api.statistics.store.ExecutionListener
import com.eviware.loadui.api.statistics.store.TrackDescriptor
import com.eviware.loadui.api.statistics.store.ExecutionManager
import com.eviware.loadui.util.BeanInjector
import com.eviware.loadui.util.FormattingUtils
import com.google.common.io.Files

import java.util.concurrent.atomic.AtomicBoolean

def displayLimit( limit ) {
    limit <= 0 ? "-" : limit
}

log.info """

------------------------------------
 Initializing LoadUI Agent
------------------------------------

"""

//Load the proper workspace, use a copy of the file so that not changes are saved.
if ( !workspace ) {
    def tmpWorkspace = File.createTempFile( "workspace", "xml" )
    def sourceFile = workspaceFile ?: workspaceProvider.defaultWorkspaceFile
    if ( sourceFile.exists() ) {
        log.info "Loading Workspace file: {}", sourceFile.absolutePath
        Files.copy( sourceFile, tmpWorkspace )
    }
    workspace = workspaceProvider.loadWorkspace( tmpWorkspace )
}

workspace.localMode = localMode

def projectAdded = false
def workspaceCollectionListener = new EventHandler<CollectionEvent>() {
    public void handleEvent( CollectionEvent event ) {
        if ( CollectionEvent.Event.ADDED == event.event && WorkspaceItem.PROJECTS == event.key )
            projectAdded = true
    }
}
workspace.addEventListener( CollectionEvent, workspaceCollectionListener )

// Remove saved agents.
if ( workspace.localMode ) {
    log.info "Removing existing agents"
    for ( agent in new ArrayList( workspace.agents ) ) {
        log.info "Removing: $agent, ${System.identityHashCode( agent )}"
        agent.delete()
    }
} else {
    throw new RuntimeException( "Agent cannot run in distributed mode" )
}

//Get the project. Import it if needed.
def projectRef = null
for ( ref in workspace.projectRefs ) {
    if ( ref.projectFile.absolutePath == projectFile.absolutePath ) {
        projectRef = ref
        break
    }
}
if ( projectRef == null ) projectRef = workspace.importProject( projectFile, true )
log.info "Loading Project: {}", projectFile.absolutePath
projectRef.enabled = true
def project = projectRef.project

//Get the target
def target = testCase ? project.getSceneByLabel( testCase ) : project
if ( !target ) {
    log.error "Scenario '${testCase}' doesn't exist in Project '${project.label}'"
    workspace?.release()
    return 1
}

//Abort ongoing requests
if ( abort?.toLowerCase()?.startsWith( "t" ) ) {
    project.abortOnFinish = true
} else if ( abort?.toLowerCase()?.startsWith( "f" ) ) {
    project.abortOnFinish = false
}

// wait until workspace fires ADDED event for this
// project. this will ensure that RunningListener in
// ProjectManagerImpl is added to project before start
// so it can handle incoming events properly. Without
// this START event occurs before RunningLister is
// assigned to the project and START event is not handled
// at all.
while ( !projectAdded ) {
    sleep 100
}

//Run the test
log.info """

------------------------------------
 RUNNING TEST
 TARGET ${target.label}
 LIMITS Time: ${FormattingUtils.formatTime( target.getLimit( CanvasItem.TIMER_COUNTER ) )} Requests: ${
    displayLimit( target.getLimit( CanvasItem.SAMPLE_COUNTER ) )
} Failures: ${displayLimit( target.getLimit( CanvasItem.FAILURE_COUNTER ) )}
------------------------------------

"""

ExecutionManager executionManager = BeanInjector.getBean( ExecutionManager )

final testStopped = new AtomicBoolean( false )

def executionListener = new ExecutionListener() {
    void executionStopped( ExecutionManager.State oldState ) {
        testStopped.set( true )
    }
    void executionStarted( ExecutionManager.State oldState ) {}
    void trackRegistered( TrackDescriptor trackDescriptor ) {}
    void trackUnregistered( TrackDescriptor trackDescriptor ) {}
}

executionManager.addExecutionListener( executionListener )

try {
    def testRunner = BeanInjector.getBean( TestRunner )
    testRunner.enqueueExecution( target )

    def time = target.getCounter( CanvasItem.TIMER_COUNTER )
    def samples = target.getCounter( CanvasItem.SAMPLE_COUNTER )
    def failures = target.getCounter( CanvasItem.FAILURE_COUNTER )

    //wait until execution stops
    while ( !testStopped.get() ) {
        log.info "Time: ${FormattingUtils.formatTime( time.value )} Requests: ${samples.value} Failures: ${failures.value} Running? ${testStopped}"
        sleep 1000
    }

    //Shutdown
    log.info """

------------------------------------
 TEST EXECUTION COMPLETED
 FINAL RESULTS: ${FormattingUtils.formatTime( time.value )} Requests: ${samples.value} Failures: ${failures.value}
------------------------------------

"""


} finally {
    executionManager.removeExecutionListener( executionListener )
}


def success = project.getLimit( CanvasItem.FAILURE_COUNTER ) == -1 || project.getCounter( CanvasItem.FAILURE_COUNTER ).get() < project.getLimit( CanvasItem.FAILURE_COUNTER )

log.info "Shutting down..."
sleep 1000

project.release()

workspace.removeEventListener( CollectionEvent, workspaceCollectionListener )

return success