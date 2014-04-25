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
 * Sends signals at a decreasing or increasing rate until it gets to a prespecified level
 *
 * @id com.eviware.Ramp
 * @name Ramp
 * @help http://www.loadui.org/Generators/ramp-updown-component.html
 * @category generators
 * @nonBlocking true
 */

import com.eviware.loadui.api.events.PropertyEvent

//Properties
createProperty('start', Long, 0)
createProperty('end', Long, 10)
createProperty('period', Long, 10)
createProperty('unit', String, 'Sec')

direction = "up"
msPerUnit = 1000
currentRate = 0
gradient = 0
timeEllapsed = 0
currentDelay = 0
targetReached = false

timer = new Timer(true)

scheduled = false
future = null

reset = {
    currentDelay = 0
    timeEllapsed = 0
    targetReached = false
    if (period.value > 0) {
        //Some sanity checks
//		if (start.value != 0) {
//			if (msPerUnit/start.value > period.value * 1000) {
//				targetReached = true
//				currentRate = end.value
//			}
//		} 

        if (!targetReached) {
            gradient = (end.value - start.value) / (period.value * 1000)
            currentRate = start.value
            while (currentRate.longValue() == 0) {
                timeEllapsed++
                currentRate = start.value + timeEllapsed * gradient
            }
        }
    } else {
        targetReached = true
        currentRate = end.value()
    }

    if (unit.value == "Sec")
        msPerUnit = 1000
    if (unit.value == "Min")
        msPerUnit = 60000
    if (unit.value == "Hour")
        msPerUnit = 3600000

    scheduled = false
}

begin = {
    if (stateProperty.value) {
        reset();
        schedule();
    }
}

schedule = {
    if (stateProperty.value && running && !scheduled) {
        if (!targetReached) {
            if (currentDelay > 0) {
                timeEllapsed = timeEllapsed + currentDelay
                currentRate = start.value + timeEllapsed * gradient
            }

            if (timeEllapsed / 1000 >= period.value) {
                targetReached = true
                currentRate = end.value
                direction = "none"
            }
            if (currentRate.longValue() > 0) {
                currentDelay = msPerUnit / (currentRate.longValue())
            } else {
                currentDelay = 1 //Handling edge cases
            }
        }

        future = timer.runAfter(currentDelay.intValue()) {
            trigger()
            scheduled = false
            schedule()
        }
        scheduled = true
    }
}

addEventListener(PropertyEvent) { event ->
    if (event.event == PropertyEvent.Event.VALUE) {

        future?.cancel()

        started = false
        if (end.value > start.value)
            direction = "up"

        if (start.value > end.value)
            direction = "down"

        if (start.value == end.value)
            direction = "none"
        begin()
    }
}

onAction("START") { schedule() }

onAction("STOP") {
    future?.cancel()
    started = false
    scheduled = false
}

onAction("COMPLETE") { reset() }

//Layout
layout {
    property(property: start, label: 'Start', min: 0)
    property(property: end, label: 'End', min: 0)
    separator(vertical: true)
    property(property: unit, label: 'Unit', options: ['Sec', 'Min', 'Hour'])
    separator(vertical: true)
    property(property: period, label: 'Period\n(Sec)', min: 1)
    separator(vertical: true)
    box(layout: "wrap, ins 0") {
        box(widget: 'display') {
            node(label: 'Rate', content: {
                "${currentRate.longValue()} / $unit.value $direction"
            }, constraints: "w 60!")
        }
        action(label: "Restart", action: { reset(); begin(); }, constraints: "align right")
    }
}

//Compact Layout
compactLayout {
    box(widget: 'display') {
        node(label: 'Rate', content: { "${currentRate.longValue()} / $unit.value $direction" })
    }
}

//Settings
//settings( label: "Properties", layout: 'wrap 2' ) {
//	box(layout:"growx, wrap 1") {
//		property( property:start, label:'Start', min:0 ) 
//		property( property:end, label:'End', min:0 ) 
//		property( property:period, label:'Period', min:1 ) 
//		property( property:unit, label:'Unit', options:['Sec','Min','Hour'] )
//	}
//} 

reset();
if (running)
    begin();
