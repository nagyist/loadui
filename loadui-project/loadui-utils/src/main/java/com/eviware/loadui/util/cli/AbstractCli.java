package com.eviware.loadui.util.cli;

import com.eviware.loadui.api.cli.CommandLineParser;
import com.google.common.base.Function;
import org.apache.commons.cli.*;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class AbstractCli implements CommandLineParser
{

	private Function<String, String> systemKeyToValue = new Function<String, String>()
	{
		@Nullable
		@Override
		public String apply( @Nullable String input )
		{
			return input == null ? "" : System.getProperty( input );
		}
	};

	@Override
	public final void parse( String[] args )
	{
		Options options = createOptions();
		try
		{
			CommandLine cmd = new PosixParser().parse( options, args );
			if( cmd.hasOption( SYSTEM_PROPERTY_OPTION ) )
			{
				parseSystemProperties( cmd );
			}
			parse( cmd );
		}
		catch( ParseException e )
		{
			System.err.println( "Error parsing commandline args: " + e.getMessage() );
			printUsageAndQuit( options );
		}
	}

	protected abstract void parse( CommandLine cmd );

	@OverridingMethodsMustInvokeSuper
	protected Options createOptions()
	{
		Options options = new Options();
		options.addOption( SYSTEM_PROPERTY_OPTION, true, "Sets system property with name=value" );
		options.addOption( NOFX_OPTION, false, "Do not include or require the JavaFX runtime" );
		options.addOption( "agent", false, "Run in Agent mode" );
		options.addOption( HELP_OPTION, "help", false, "Prints this message" );
		options.addOption( IGNORE_CURRENTLY_RUNNING_OPTION, false, "Disable lock file" );

		return options;
	}

	private void parseSystemProperties( CommandLine cmd )
	{
		for( String option : cmd.getOptionValues( SYSTEM_PROPERTY_OPTION ) )
		{
			int ix = option.indexOf( '=' );
			if( ix != -1 )
				System.setProperty( option.substring( 0, ix ), option.substring( ix + 1 ) );
			else
				System.setProperty( option, "true" );
		}
	}

	protected void printUsageAndQuit( Options options )
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "LoadUILauncher", options );
		System.exit( 0 );
	}

}
