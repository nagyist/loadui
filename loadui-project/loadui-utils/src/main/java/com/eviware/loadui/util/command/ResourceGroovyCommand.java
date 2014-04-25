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
package com.eviware.loadui.util.command;

import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

public final class ResourceGroovyCommand extends AbstractGroovyCommand
{
	private final String scriptName;

	public ResourceGroovyCommand( String scriptName, Map<String, Object> attributes )
	{
		super( attributes );
		this.scriptName = scriptName;
	}

	@Override
	public String getScript()
	{
		try(Reader reader = new InputStreamReader( getClass().getResourceAsStream( scriptName ) ))
		{
			return CharStreams.toString( reader );
		}
		catch( IOException ioe )
		{
			throw new RuntimeException( ioe );
		}
	}
}
