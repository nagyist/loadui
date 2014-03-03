package com.eviware.loadui.api.cli;

public interface CommandLineParser
{
	static final String LOCAL_OPTION = "l";
	static final String FILE_OPTION = "f";
	static final String AGENT_OPTION = "a";
	static final String LIMITS_OPTION = "L";
	@Deprecated
	static final String TESTCASE_OPTION = "t";
	static final String VU_SCENARIO_OPTION = "v";
	static final String PROJECT_OPTION = "p";
	static final String WORKSPACE_OPTION = "w";
	static final String REPORT_DIR_OPTION = "r";
	static final String RETAIN_SAVED_ZOOM_LEVELS = "z";
	static final String REPORT_FORMAT_OPTION = "F";
	static final String STATISTICS_REPORT_OPTION = "S";
	static final String STATISTICS_REPORT_INCLUDE_SUMMARY_OPTION = "s";
	static final String STATISTICS_REPORT_COMPARE_OPTION = "c";
	static final String ABORT_ONGOING_REQUESTS_OPTION = "A";

	static final String NOFX_OPTION = "nofx";
	static final String SYSTEM_PROPERTY_OPTION = "D";
	static final String HELP_OPTION = "h";
	static final String IGNORE_CURRENTLY_RUNNING_OPTION = "nolock";

	public void parse( String[] args );

}
