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
package com.eviware.loadui.cmd;

import com.eviware.loadui.api.command.GroovyCommand;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class CommandRunner
{
	public static final Logger log = LoggerFactory.getLogger( CommandRunner.class );

	private final ExecutorService executor;
	private final WorkspaceProvider workspaceProvider;
	private final GroovyShell shell;
	private final ExecutionManager executionManager;

	public CommandRunner( WorkspaceProvider workspaceProvider, ExecutionManager executionManager )
	{
		this.executor = Executors.newSingleThreadScheduledExecutor( new ThreadFactory()
		{
			@Override
			public Thread newThread( Runnable r )
			{
				return new Thread( r, "CommandRunner" );
			}
		} );
		this.workspaceProvider = workspaceProvider;
		this.executionManager = executionManager;

		shell = new GroovyShell();
	}

	public void execute( GroovyCommand command, Map properties )
	{
		executor.execute( new CommandRunnable( command ) );
	}

	public void destroy()
	{
		executor.shutdown();
	}

	private class CommandRunnable implements Runnable
	{
		private final GroovyCommand command;

		public CommandRunnable( GroovyCommand command )
		{
			this.command = command;
		}

		@Override
		public void run()
		{
			Binding binding = new Binding();
			binding.setVariable( "log", log );
			binding.setVariable( "workspaceProvider", workspaceProvider );
			binding.setVariable( "workspace", workspaceProvider.getWorkspace() );
			for( Entry<String, Object> entry : command.getAttributes().entrySet() )
				binding.setVariable( entry.getKey(), entry.getValue() );

			Object result = 1;
			try
			{
				Script script = shell.parse( command.getScript() );
				script.setBinding( binding );
				result = script.run();
			}
			catch( CompilationFailedException e )
			{
				log.error( "An error occured when compiling the script", e );
			}
			catch( RuntimeException e )
			{
				log.error( "An error occured when executing the script", e );
			} finally
			{
				if( executionManager.getState() != ExecutionManager.State.STOPPED )
					executionManager.stopExecution();
			}
			shell.resetLoadedClasses();

			if( command.exitOnCompletion() )
			{
				workspaceProvider.getWorkspace().release();
				if( result instanceof Number )
					System.exit( ( ( Number )result ).intValue() );
				else if( result instanceof Boolean )
					System.exit( ( Boolean )result ? 0 : 1 );
				else
					System.exit( 0 );
			}
		}
	}
}
