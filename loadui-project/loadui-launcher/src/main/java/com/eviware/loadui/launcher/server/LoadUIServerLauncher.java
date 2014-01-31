package com.eviware.loadui.launcher.server;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.launcher.HeadlessFxLauncherBase;
import com.eviware.loadui.launcher.api.GroovyCommand;
import com.eviware.loadui.launcher.impl.ResourceGroovyCommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author renato
 */
public class LoadUIServerLauncher extends HeadlessFxLauncherBase implements LoadUiProjectRunner
{
	protected static final String REPORT_DIR_OPTION = "r";
	protected static final String RETAIN_SAVED_ZOOM_LEVELS = "z";
	protected static final String REPORT_FORMAT_OPTION = "F";
	protected static final String STATISTICS_REPORT_OPTION = "S";
	protected static final String STATISTICS_REPORT_INCLUDE_SUMMARY_OPTION = "s";
	protected static final String STATISTICS_REPORT_COMPARE_OPTION = "c";
	protected static final String ABORT_ONGOING_REQUESTS_OPTION = "A";
	protected static final String PROJECTS_LOCATION_OPTION = "p";
	protected static final String LIMITS_OPTION = "L";

	private final Path DEFAULT_PROJ_LOCATION = Paths.get( System.getProperty( LoadUI.LOADUI_HOME ), "projects" );
	private final LoadUiServerProjectWatcher projectWatcher;

	public LoadUIServerLauncher( String[] args )
	{
		super( args );
		projectWatcher = new LoadUiServerProjectWatcher( this );
	}

	@Override
	protected void processCommandLine( CommandLine cmd )
	{
		System.out.println( "Server launcher processing command line" );
		Map<String, Object> attributes = new HashMap<>();

		Path projectsLocation = cmd.hasOption( PROJECTS_LOCATION_OPTION ) ?
				Paths.get( cmd.getOptionValue( PROJECTS_LOCATION_OPTION ) ) :
				DEFAULT_PROJ_LOCATION;

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

		//FIXME TEMPORARY
		attributes.put( "limits", cmd.hasOption( LIMITS_OPTION ) ? cmd.getOptionValue( LIMITS_OPTION ).split( ":" )
				: null );
		attributes.put( "workspaceFile", null );
		attributes.put( "testCase", null );
		attributes.put( "localMode", true );
		attributes.put( "agents", new HashMap<>() );

		projectWatcher.watchForProjectToRun( projectsLocation, attributes );

	}

	private ResourceGroovyCommand createCommand( Map<String, Object> attributes )
	{
		ResourceGroovyCommand command = new ResourceGroovyCommand( "/RunTest.groovy", attributes );
		command.setExit( false );
		setCommand( command );
		return command;
	}

	@Override
	@SuppressWarnings( "static-access" )
	protected Options createOptions()
	{
		Options options = super.createOptions();

		options.addOption( PROJECTS_LOCATION_OPTION, "projectsdir", true, "Sets the Projects directory" );
		options.addOption( REPORT_DIR_OPTION, "reports", true, "Generates reports and saves them in specified folder" );
		options
				.addOption( REPORT_FORMAT_OPTION, "format", true,
						"Specify output format for the exported reports (supported formats are: PDF, XLS, HTML, RTF, CSV, TXT and XML)" );
		options
				.addOption( OptionBuilder
						.withLongOpt( "statistics" )
						.withDescription(
								"Sets which Statistics pages to add to the generated report (leave blank save all pages)" )
						.hasOptionalArgs().create( STATISTICS_REPORT_OPTION ) );
		options.addOption( STATISTICS_REPORT_INCLUDE_SUMMARY_OPTION, "summary", false,
				"Set to include summary report in statistics report" );
		options.addOption( STATISTICS_REPORT_COMPARE_OPTION, "compare", true,
				"Specify a saved execution to use as a base for comparison in the generated statistics report" );
		options
				.addOption(
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
	public void runProject( Path projectPath, Map<String, Object> attributes )
	{
		System.out.println( "Starting project " + projectPath );
		attributes.put( "projectFile", projectPath.toFile() );
		setCommand( createCommand( attributes ) );
		publishService( GroovyCommand.class, getCommand(), null );
	}

}
