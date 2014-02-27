package com.eviware.loadui.util.cli;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.util.server.LoadUiServerProjectWatcher;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.util.*;

public class LoadUiServerCli extends AbstractCli
{
	private final LoadUiServerProjectWatcher projectWatcher;

	public LoadUiServerCli( LoadUiServerProjectWatcher projectWatcher )
	{
		this.projectWatcher = projectWatcher;
	}

	@Override
	protected Options createOptions()
	{
		Options options = super.createOptions();

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

		//FIXME TEMPORARY
		options.addOption( LIMITS_OPTION, "limits", true,
				"Sets the limits (<SECONDS>:<REQUESTS>:<FAILURES>) for the execution (e.g. -L 60:0:200 )" );

		return options;
	}

	@Override
	protected void parse( CommandLine cmd )
	{
		System.out.println( "Server launcher processing command line" );
		Map<String, Object> attributes = new HashMap<>();

		Path projectsLocation = projectsLocation();

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

		// FIXME this options is being allowed only temporarily so we can easily stop tests with a limit
		// once SaaS is able to control tests, we should probably remove this
		attributes.put( "limits", cmd.hasOption( LIMITS_OPTION ) ? cmd.getOptionValue( LIMITS_OPTION ).split( ":" )
				: null );
		attributes.put( "workspaceFile", null );
		attributes.put( "testCase", null );
		attributes.put( "localMode", true );
		attributes.put( "agents", new HashMap<>() );

		projectWatcher.watchForProjectToRun( projectsLocation, attributes );
	}

	protected Path projectsLocation()
	{
		return LoadUI.serverProjectsLocation();
	}

}
