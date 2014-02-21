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
package com.eviware.loadui.groovy;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.component.*;
import com.eviware.loadui.api.component.categories.*;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.groovy.categories.*;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.events.EventSupport;
import com.eviware.loadui.util.groovy.ClassLoaderRegistry;
import com.eviware.loadui.util.groovy.ParsedGroovyScript;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import org.apache.commons.codec.digest.DigestUtils;

import java.beans.PropertyChangeEvent;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GroovyBehaviorProvider implements BehaviorProvider, EventFirer, Releasable
{
	public static final String TYPE = "com.eviware.loadui.groovy.GroovyComponent";

	private static final int UPDATE_FREQUENCY = 5;

	private final File scriptDir;
	private final ComponentRegistry registry;
	private final Map<File, ScriptDescriptor> scripts = new HashMap<>();
	private final ScheduledFuture<?> future;
	private final EventSupport eventSupport = new EventSupport( this );
	private final ClassLoaderRegistry clr;

	private final ComponentDescriptor emptyDescriptor = new ComponentDescriptor( TYPE, "misc", "EmptyScriptComponent",
			"", null );

	public GroovyBehaviorProvider( ComponentRegistry registry, ScheduledExecutorService scheduler, File scriptDir,
											 ClassLoaderRegistry clr )
	{
		if( !scriptDir.isAbsolute() )
		{
			scriptDir = LoadUI.relativeFile( scriptDir.getPath() );
		}
		this.scriptDir = scriptDir;
		this.registry = registry;
		this.clr = clr;

		File groovyRoot = new File( System.getProperty( "groovy.root" ) );
		if( !groovyRoot.isDirectory() )
			if( !groovyRoot.mkdirs() )
				throw new RuntimeException( "Unable to create required directories: " + groovyRoot.getAbsolutePath() );

		File grapeConfig = new File( groovyRoot, "grapeConfig.xml" );
		if( !grapeConfig.exists() )
		{
			try
			{
				Files.copy( new InputSupplier<InputStream>()
				{
					@Override
					public InputStream getInput() throws IOException
					{
						return getClass().getResourceAsStream( "/grapeConfig.xml" );
					}
				}, grapeConfig );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}

		// registry.registerDescriptor( emptyDescriptor, this );
		registry.registerType( TYPE, this );

		//TODO replace this with the java.nio directory watcher
		future = scheduler.scheduleWithFixedDelay( new DirWatcher(), 0, UPDATE_FREQUENCY, TimeUnit.SECONDS );
	}

	@Override
	public ComponentBehavior createBehavior( ComponentDescriptor descriptor, ComponentContext context )
			throws ComponentCreationException
	{
		if( descriptor instanceof ScriptDescriptor )
		{
			ScriptDescriptor scriptDescriptor = ( ScriptDescriptor )descriptor;
			context.setCategory( descriptor.getCategory() );
			context.setAttribute( GroovyBehaviorSupport.DIGEST_ATTRIBUTE, scriptDescriptor.getDigest() );
			context.setAttribute( GroovyBehaviorSupport.ID_ATTRIBUTE, scriptDescriptor.getId() );
			context.setAttribute( GroovyBehaviorSupport.CLASS_LOADER_ATTRIBUTE, scriptDescriptor.getClassLoaderId() );
			context.createProperty( GroovyBehaviorSupport.SCRIPT_PROPERTY, String.class, scriptDescriptor.getScript() );
			return instantiateBehavior( context, scriptDescriptor.getCategory() );
		}
		else if( descriptor == emptyDescriptor )
		{
			context.setCategory( descriptor.getCategory() );
			return instantiateBehavior( context, null );
		}
		return null;
	}

	@Override
	public ComponentBehavior loadBehavior( String componentType, ComponentContext context )
			throws ComponentCreationException
	{
		if( TYPE.equals( componentType ) )
		{
			String id = context.getAttribute( GroovyBehaviorSupport.ID_ATTRIBUTE, "" );
			String scriptPath = context.getAttribute( GroovyBehaviorSupport.SCRIPT_FILE_ATTRIBUTE, null );
			String digest = context.getAttribute( GroovyBehaviorSupport.DIGEST_ATTRIBUTE, null );
			if( digest != null )
			{
				for( Entry<File, ScriptDescriptor> entry : scripts.entrySet() )
				{
					ScriptDescriptor d = entry.getValue();
					if( id.equals( d.getId() ) )
					{
						if( !digest.equals( d.getDigest() ) )
						{
							// ID matches, update the component.
							context.setAttribute( GroovyBehaviorSupport.DIGEST_ATTRIBUTE, d.getDigest() );
							context.setAttribute( GroovyBehaviorSupport.CLASS_LOADER_ATTRIBUTE, d.getClassLoaderId() );
							context.createProperty( GroovyBehaviorSupport.SCRIPT_PROPERTY, String.class ).setValue(
									d.getScript() );
						}
						break;
					}
					if( entry.getKey().getAbsolutePath().equals( scriptPath ) )
					{
						if( !digest.equals( d.getDigest() ) )
						{
							// Script file has changed, update the component.
							context.setAttribute( GroovyBehaviorSupport.DIGEST_ATTRIBUTE, d.getDigest() );
							context.setAttribute( GroovyBehaviorSupport.ID_ATTRIBUTE, d.getId() );
							context.setAttribute( GroovyBehaviorSupport.CLASS_LOADER_ATTRIBUTE, d.getClassLoaderId() );
							context.createProperty( GroovyBehaviorSupport.SCRIPT_PROPERTY, String.class ).setValue(
									d.getScript() );
						}
						break;
					}
				}
			}
			return instantiateBehavior( context, context.getCategory() );
		}
		return null;
	}

	private ComponentBehavior instantiateBehavior( ComponentContext context, String category )
			throws ComponentCreationException
	{
		try
		{
			if( GeneratorCategory.CATEGORY.equalsIgnoreCase( category ) || "generator".equalsIgnoreCase( category ) )
			{
				return new GroovyGenerator( this, context );
			}
			else if( RunnerCategory.CATEGORY.equalsIgnoreCase( category ) || "runner".equalsIgnoreCase( category ) )
			{
				return new GroovyRunner( this, context );
			}
			else if( FlowCategory.CATEGORY.equalsIgnoreCase( category ) )
			{
				return new GroovyFlow( this, context );
			}
			else if( OutputCategory.CATEGORY.equalsIgnoreCase( category ) )
			{
				return new GroovyOutput( this, context );
			}
			else if( SchedulerCategory.CATEGORY.equalsIgnoreCase( category ) || "scheduler".equalsIgnoreCase( category ) )
			{
				return new GroovyScheduler( this, context );
			}
			else if( MiscCategory.CATEGORY.equalsIgnoreCase( category ) )
			{
				return new GroovyMisc( this, context );
			}
			else
			{
				return new GroovyMisc( this, context );
			}
		}
		catch( RuntimeException e )
		{
			throw new ComponentCreationException( context.getLabel(), "Error instantiating Component", e );
		}
	}

	@Override
	public void release()
	{
		future.cancel( true );
		ReleasableUtils.releaseAll( eventSupport, clr );
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<? super T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}

	@Override
	public void clearEventListeners()
	{
		eventSupport.clearEventListeners();
	}

	@Override
	public void fireEvent( EventObject event )
	{
		eventSupport.fireEvent( event );
	}

	public ClassLoaderRegistry getClassLoaderRegistry()
	{
		return clr;
	}

	public static class ScriptDescriptor extends ComponentDescriptor
	{
		private final String id;
		private final String classLoaderId;
		private final File script;
		private final long changed;
		private final String digest;

		public static ScriptDescriptor parseFile( File script )
		{
			String baseName = script.getName().substring( 0, script.getName().lastIndexOf( ".groovy" ) );
			ParsedGroovyScript headers = new ParsedGroovyScript( getFileContent( script ) );

			File icon = new File( script.getParentFile(), headers.getHeader( "icon", baseName + ".png" ) );
			String digest = null;
			try( FileInputStream fis = new FileInputStream( script ) )
			{
				digest = DigestUtils.md5Hex( fis );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}

			return new ScriptDescriptor( headers.getHeader( "id", baseName ), headers.getHeader( "classloader",
					headers.getHeader( "id", baseName ) ), script, headers.getHeader( "category", MiscCategory.CATEGORY ),
					headers.getHeader( "name", baseName ), headers.getDescription(), icon.exists() ? icon.toURI() : null, digest,
					headers.getHeader( "help", null ), headers.getHeader( "deprecated", "false" ) );
		}

		private static String getFileContent( File file )
		{
			try( Reader in = new FileReader( file ) )
			{
				StringBuilder sb = new StringBuilder();
				char[] chars = new char[1 << 16];
				int length;

				while( ( length = in.read( chars ) ) > 0 )
				{
					sb.append( chars, 0, length );
				}
				return sb.toString();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			return "";
		}

		private ScriptDescriptor( String id, String classLoaderId, File script, String category, String label,
										  String description, URI icon, String digest, String helpUrl, String deprecated )
		{
			super( TYPE, category, label, description, icon, helpUrl, !"false"
					.equals( deprecated ) );
			this.classLoaderId = classLoaderId;
			this.id = id;
			this.script = script;
			this.digest = digest;
			changed = script.lastModified();
		}

		public File getScriptFile()
		{
			return script;
		}

		public String getScript()
		{
			return getFileContent( script );
		}

		public boolean isModified()
		{
			return script.lastModified() != changed;
		}

		public String getDigest()
		{
			return digest;
		}

		public String getId()
		{
			return id;
		}

		public String getClassLoaderId()
		{
			return classLoaderId;
		}
	}

	private class DirWatcher implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if( !scriptDir.isDirectory() )
					return;

				List<File> files = Arrays.asList( scriptDir.listFiles() );
				for( Iterator<Entry<File, ScriptDescriptor>> it = scripts.entrySet().iterator(); it.hasNext(); )
				{
					Entry<File, ScriptDescriptor> entry = it.next();
					if( !entry.getKey().exists() || entry.getValue().isModified() )
					{
						registry.unregisterDescriptor( entry.getValue() );
						it.remove();
					}
				}

				for( File file : files )
				{
					if( file.getName().endsWith( ".groovy" ) && !scripts.containsKey( file ) )
					{
						ScriptDescriptor descriptor = ScriptDescriptor.parseFile( file );
						scripts.put( file, descriptor );
						registry.registerDescriptor( descriptor, GroovyBehaviorProvider.this );
						fireEvent( new PropertyChangeEvent( descriptor, file.getCanonicalPath(), null, descriptor.getScript() ) );
						fireEvent( new PropertyChangeEvent( descriptor, descriptor.getId(), null, descriptor.getScript() ) );
					}
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
}
