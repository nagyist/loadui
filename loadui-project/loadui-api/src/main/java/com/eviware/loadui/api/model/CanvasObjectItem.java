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
package com.eviware.loadui.api.model;

import com.eviware.loadui.api.terminal.TerminalHolder;

public interface CanvasObjectItem extends ModelItem, TerminalHolder
{
	public static String RELOADED = CanvasObjectItem.class.getSimpleName() + "@reloaded";

	public static String ACTIVITY = CanvasObjectItem.class.getSimpleName() + "@activity";

	/**
	 * Get the base color for this CanvasObjectItem.
	 * 
	 * @return The color, as a hex String (eg. '#ff0000').
	 */
	public String getColor();

	/**
	 * Gets the activity status of the CanvasObjectItem, which is then visualized
	 * in the GUI.
	 * 
	 * @return
	 */
	public boolean isActive();
}
