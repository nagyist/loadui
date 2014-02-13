package com.eviware.loadui.launcher;

import com.eviware.loadui.launcher.api.GroovyCommand;
import javafx.application.Application;
import javafx.stage.Stage;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class HeadlessFxLauncherBase extends LoadUILauncher
{

	private GroovyCommand command;

	public HeadlessFxLauncherBase( String[] args )
	{
		super( args );
	}

	@Override
	protected final void processOsgiExtraPackages()
	{
		JavaFxStarter.addJavaFxOsgiExtraPackages( configProps );
	}

	protected void setCommand( GroovyCommand command )
	{
		this.command = command;
	}

	protected GroovyCommand getCommand()
	{
		return command;
	}

	@Override
	protected void beforeBundlesStart( Bundle[] bundles )
	{
		final Set<String> doNotStart = new HashSet<>( Arrays.asList( "loadui-pro-fx" ) );

		for( Bundle bundle : bundles )
		{
			if( is( bundle ).in( doNotStart ) )
			{
				try
				{
					System.out.println( "Uninstalling bundle: " + bundle.getSymbolicName() );
					bundle.uninstall();
				}
				catch( BundleException e )
				{
					e.printStackTrace();
				}
			}
		}

	}

	private ContainsSimilarItem is( Bundle bundle )
	{
		return new ContainsSimilarItem( bundle.getSymbolicName() );
	}

	private class ContainsSimilarItem
	{

		String name;

		public ContainsSimilarItem( String name )
		{
			this.name = name;
		}

		boolean in( Set<String> set )
		{
			if( name == null )
			{
				return false;
			}
			for( String item : set )
				if( name.contains( item ) )
					return true;
			return false;
		}

	}


	public static abstract class HeadlessFxApp extends Application
	{
		private HeadlessFxLauncherBase launcher;

		@Override
		public void start( final Stage stage ) throws Exception
		{
			System.out.println( getClass().getName() + " starting" );

			Runnable task = new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						System.setSecurityManager( null );
						launcher = createLauncher( getParameters().getRaw().toArray( new String[0] ) );
						launcher.init();
						launcher.start();
					}
					catch( Exception e )
					{
						e.printStackTrace();
						exitInError();
					}
				}
			};

			new Thread( task ).start();
		}

		protected abstract HeadlessFxLauncherBase createLauncher( String[] args );

		@Override
		public void stop() throws Exception
		{
			launcher.stop();
		}

	}

}
