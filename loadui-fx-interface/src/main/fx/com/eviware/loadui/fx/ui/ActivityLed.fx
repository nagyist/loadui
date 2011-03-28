/* 
 * Copyright 2011 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.fx.ui;

import javafx.scene.CustomNode;
import javafx.scene.Group;

import com.sun.javafx.scene.layout.Region;

public class ActivityLed extends CustomNode {
	public var active = true on replace {
		styleClass = if( active ) "activity-led-on" else "activity-led-off";
	}
	
	override function create() {
		Group { disable: bind disabled, styleClass: "activity-led", content: Region { styleClass: "led" } }
	}
}