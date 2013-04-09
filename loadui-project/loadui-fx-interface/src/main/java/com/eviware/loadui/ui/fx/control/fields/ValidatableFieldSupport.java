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
package com.eviware.loadui.ui.fx.control.fields;

import javafx.scene.Node;

public class ValidatableFieldSupport
{
	public static final String INVALID_CLASS = "invalid";

	public static <T extends Node & Field<?>> void setInvalid( T parent )
	{
		parent.getStyleClass().add( INVALID_CLASS );
	}

	public static <T extends Node & Field<?>> void setValid( T parent )
	{
		parent.getStyleClass().remove( INVALID_CLASS );
	}
}