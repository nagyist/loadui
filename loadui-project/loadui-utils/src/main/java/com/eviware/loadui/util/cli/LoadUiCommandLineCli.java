package com.eviware.loadui.util.cli;

import com.eviware.loadui.util.server.LoadUiServerProjectRunner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class LoadUiCommandLineCli extends AbstractCli
{
	static final Logger log = LoggerFactory.getLogger( LoadUiCommandLineCli.class );

	private final LoadUiServerProjectRunner projectRunner;

	public LoadUiCommandLineCli( LoadUiServerProjectRunner projectRunner )
	{
		this.projectRunner = projectRunner;
	}

	@Override
	protected void parse( CommandLine cmd )
	{
		log.debug( "Command line launcher processing command-line options" );
		Map<String, Object> attributes = new HashMap<>();

		if( cmd.hasOption( PROJECT_OPTION ) )
		{
			attributes.put( "workspaceFile",
					cmd.hasOption( WORKSPACE_OPTION ) ? new File( cmd.getOptionValue( WORKSPACE_OPTION ) ) : null );
			attributes.put( "projectFile",
					cmd.hasOption( PROJECT_OPTION ) ? new File( cmd.getOptionValue( PROJECT_OPTION ) ) : null );
			attributes.put( "testCase", cmd.getOptionValue( TESTCASE_OPTION ) );
			if( cmd.getOptionValue( VU_SCENARIO_OPTION ) != null )
				attributes.put( "testCase", cmd.getOptionValue( VU_SCENARIO_OPTION ) );
			attributes.put( "limits", cmd.hasOption( LIMITS_OPTION ) ? cmd.getOptionValue( LIMITS_OPTION ).split( ":" )
					: null );
			attributes.put( "localMode", cmd.hasOption( LOCAL_OPTION ) );
			Map<String, String[]> agents = null;
			if( cmd.hasOption( AGENT_OPTION ) )
			{
				agents = new HashMap<>();
				for( String option : cmd.getOptionValues( AGENT_OPTION ) )
				{
					int ix = option.indexOf( "=" );
					if( ix != -1 )
						agents.put( option.substring( 0, ix ), option.substring( ix + 1 ).split( "," ) );
					else
						agents.put( option, null );
				}
			}
			attributes.put( "agents", agents );

			attributes.put( "reportFolder", cmd.getOptionValue( REPORT_DIR_OPTION ) );
			attributes.put( "reportFormat",
					cmd.hasOption( REPORT_FORMAT_OPTION ) ? cmd.getOptionValue( REPORT_FORMAT_OPTION ) : "PDF" );

			String[] statisticPageOptionValues = cmd.getOptionValues( STATISTICS_REPORT_OPTION );
			List<String> statisticPages = statisticPageOptionValues == null ? Collections.<String>emptyList() : Arrays
					.asList( statisticPageOptionValues );

			attributes.put( "statisticPages", statisticPages );
			attributes.put( "compare", cmd.getOptionValue( STATISTICS_REPORT_COMPARE_OPTION ) );

			attributes.put( "abort", cmd.getOptionValue( ABORT_ONGOING_REQUESTS_OPTION ) );

			attributes.put( "includeSummary", cmd.hasOption( STATISTICS_REPORT_INCLUDE_SUMMARY_OPTION ) );

			attributes.put( "retainZoom", cmd.hasOption( RETAIN_SAVED_ZOOM_LEVELS ) );

			projectRunner.runProjectAsController( attributes, true );
		}
		else if( cmd.hasOption( FILE_OPTION ) )
		{
			projectRunner.runProjectAsController( new File( cmd.getOptionValue( FILE_OPTION ) ).toPath(), attributes, true );
		}
		else
		{
			printUsageAndQuit( createOptions() );
		}

	}

	@Override
	protected Options createOptions()
	{
		Options options = super.createOptions();
		options.addOption( WORKSPACE_OPTION, "workspace", true, "Sets the Workspace file to load" );
		options.addOption( PROJECT_OPTION, "project", true, "Sets the Project file to run" );
		options.addOption( TESTCASE_OPTION, "testcase", true,
				"Sets which TestCase to run (leave blank to run the entire Project)" );
		options.addOption( VU_SCENARIO_OPTION, "scenario", true,
				"Sets which Scenario to run (leave blank to run the entire Project)" );
		options.addOption( LIMITS_OPTION, "limits", true,
				"Sets the limits (<SECONDS>:<REQUESTS>:<FAILURES>) for the execution (e.g. -L 60:0:200 )" );
		options.addOption( OptionBuilder
				.withLongOpt( "agents" )
				.withDescription(
						"Sets the agents to use for the test ( usage -" + AGENT_OPTION
								+ " <ip>[:<port>][=<scenario>[,<scenario>] ...] )" ).hasArgs().create( AGENT_OPTION ) );
		options.addOption( FILE_OPTION, "file", true, "Executes the specified Groovy script file" );
		options.addOption( LOCAL_OPTION, "local", false, "Executes TestCases in local mode" );
		options.addOption( REPORT_DIR_OPTION, "reports", true, "Generates reports and saves them in specified folder" );
		options.addOption( REPORT_FORMAT_OPTION, "format", true,
				"Specify output format for the exported reports (supported formats are: PDF, XLS, HTML, RTF, CSV, TXT and XML)" );
		options.addOption( OptionBuilder
				.withLongOpt( "statistics" )
				.withDescription(
						"Sets which Statistics pages to add to the generated report (leave blank save all pages)" )
				.hasOptionalArgs().create( STATISTICS_REPORT_OPTION ) );
		options.addOption( STATISTICS_REPORT_INCLUDE_SUMMARY_OPTION, "summary", false,
				"Set to include summary report in statistics report" );
		options.addOption( STATISTICS_REPORT_COMPARE_OPTION, "compare", true,
				"Specify a saved execution to use as a base for comparison in the generated statistics report" );
		options.addOption(
				ABORT_ONGOING_REQUESTS_OPTION,
				"abort",
				true,
				"Overrides \"Abort ongoing requests on finish\" project property. If set to true ongoing requests will be canceled, if false test will finish when all ongoing requests complete. If not set, property value from project will be used to determine what to do with ongoing requests." );

		options.addOption( RETAIN_SAVED_ZOOM_LEVELS, false, "Use the saved zoom levels for charts from the project." );

		return options;
	}

}
