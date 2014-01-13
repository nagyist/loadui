// 
// Copyright 2013 SmartBear Software
// 
// Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
// versions of the EUPL (the "Licence");
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at:
// 
// http://ec.europa.eu/idabc/eupl
// 
// Unless required by applicable law or agreed to in writing, software distributed under the Licence is
// distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the Licence for the specific language governing permissions and limitations
// under the Licence.
// 

/**
 * Ramps up, holds steady and then ramps down the "Classic" way.
 *
 * @id com.eviware.RampLoad
 * @name Ramp Load
 * @category generators
 * @help http://loadui.org/Generators/ramp-load.html
 * @nonBlocking true
 */

import java.util.concurrent.TimeUnit
import com.eviware.loadui.api.component.categories.RunnerCategory

sampleCount = createInput( 'Sample Count', 'Currently running feedback', 'Used to recieve the number of currently running requests from the triggered Runner.' ) {
    it.name == "runningTerminal"
}

feedbackProviders = [:]
sampleCount.connections.each { feedbackProviders[it.outputTerminal] = 0 }

createProperty( 'rampLength', Long, 10 ) { calculateAcceleration() }
createProperty( 'peakLength', Long, 10 )
createProperty( 'peakLoad', Long, 1){ calculateAcceleration() }

future = null
cancellingFuture = null
startTime = 0
triggersSent = 0
calculateAcceleration()
currentlyRunning = 0
peakLimit = 0
hasPeaked = false;

latestAction = 'NONE'

onAction( 'START' ) {
    latestAction = 'START'
    calculateAcceleration()
    startTime = currentTime()
    scheduleNext( startTime )
    triggersSent = 0
}

onAction( 'STOP' ) {
    latestAction = 'STOP'
    peakLimit = 0
    hasPeaked = false
    future?.cancel( true )
    cancellingFuture?.cancel( true )
    startTime = null
}

onMessage = { outgoing, incoming, message ->
    if ( incoming == sampleCount && latestAction != 'STOP'){
        feedbackProviders[outgoing] = message[RunnerCategory.CURRENTLY_RUNNING_MESSAGE_PARAM]

        def count = feedbackProviders.values().sum()

        if( count < peakLimit && peakLimit > 0){
            trigger()
            count += feedbackProviders.size()
        }
        currentlyRunning = count
    }
}

onConnect = { outgoing, incoming ->

    if( incoming == sampleCount )
        feedbackProviders[outgoing] = 0

    if( outgoing == triggerTerminal)
        trigger()
}

onDisconnect = { outgoing, incoming ->
    if( incoming == sampleCount){
        feedbackProviders.remove( outgoing )
    }
}


scheduleNext = { wakeTime ->
    def t0 = getT0()

    if( t0 >= rampLength.value && !hasPeaked) {

        hasPeaked = true
        peakLimit = peakLoad.value

        def delay = 1000000/peakLoad.value

        future = scheduleAtFixedRate( { trigger() }, delay, delay, TimeUnit.MICROSECONDS )
        cancellingFuture = schedule( {
            future?.cancel( true )
            a = a*-1
            scheduleNext( rampLength.value )
        }, peakLength.value, TimeUnit.SECONDS )
    }
    else if( t0 >= 0 ) {

        triggersSent = 0

        peakLimit =  Math.floor( t0 * Math.abs( a ) )

        while( triggersSent < ( peakLimit - currentlyRunning ) ) {
            trigger()
            triggersSent++
        }

        t1 =  Math.sqrt( 2/a + t0**2 );

        future?.cancel( true )
        def diff = Math.abs( t1 - getT0() )
        if( !Double.isNaN( diff ) ) {
            future = schedule( {
                if(peakLimit >= 1){
                    trigger()
                }
                scheduleNext( t1 )
            }, ( diff*1000000) as long, TimeUnit.MICROSECONDS )
        }else{
            peakLimit = 0
        }
    }
}

def getT0() {
    if( !startTime ) return 0
    relativeTime = currentTime() - startTime
    if( relativeTime >= rampLength.value + peakLength.value )
        return startTime + rampLength.value*2 + peakLength.value - currentTime()
    if( relativeTime >= rampLength.value )
        return rampLength.value
    return relativeTime
}

layout {
    property( property:rampLength, label:'Ramp Duration\n(sec)', min:1 )
    property( property:peakLength, label:'Peak Duration\n(sec)', min:0 )
    separator( vertical:true )
    property( property:peakLoad, label:'Peak Load', min: 1)
    separator( vertical:true )
    box( widget:'display' ) {
        node( label:'Load', content: { if( getT0() > 0 ) String.format( '%7.1f', Math.abs(a*getT0()) ) else 0 }, constraints:'w 60!' )
        node( label:'Running', content: { currentlyRunning })
    }
}

compactLayout {
    box( widget:'display' ) {
        node( label:'Load', content: { if( getT0() > 0 ) String.format( '%7.1f',Math.abs(a*getT0() ) ) else 0 }, constraints:'w 60!' )
        node( label:'Running', content: { currentlyRunning })
    }
}

def currentTime() {
    System.currentTimeMillis() / 1000
}

def calculateAcceleration() {
    a = peakLoad.value / rampLength.value
}