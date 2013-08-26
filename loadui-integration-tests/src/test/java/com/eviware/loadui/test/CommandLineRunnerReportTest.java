package com.eviware.loadui.test;/*
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


import com.eviware.loadui.test.categories.IntegrationTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static com.eviware.loadui.util.test.matchers.StringLengthMatcher.lenghtGreaterThan;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.transform.XmlConverters.the;
import static org.xmlmatchers.xpath.HasXPath.hasXPath;

/**
 * Author: maximilian.skog
 * Date: 2013-07-04
 * Time: 10:59
 */
@Category( IntegrationTest.class )
public class CommandLineRunnerReportTest
{
	protected static final Logger log = LoggerFactory.getLogger( CommandLineRunnerReportTest.class );

	private static final String OUTPUT_FOLDER_NAME = "test-out-put";
	private static final String GET_FIRST_CHART_IMAGE_XPATH = "//jasperPrint[1]/page[1]/image[1]/imageSource[1]";
	private static final int MINIMUM_REASONABLE_CHARACTER_LENGTH_OF_CHART_IMAGE = 2000;

	@After
	public void deleteOutputFiles()
	{
		File outputFolder = new File( getOutPutFolderPath() );

		try
		{
			FileUtils.deleteDirectory( outputFolder );
		}
		catch( IOException e )
		{
			log.warn( "Failed to delete output directory", e );
		}
	}

	@Test( timeout = 60_000 )
	public void reportCreatedTest()
	{
		//Given
		int exitValue = CommandLineLauncherTestUtils.launchCommandLineRunnerWithCommands(
				"-p", getProjectFilePath( "basictest.xml" ), "-L", "1:0:0", "-F", "XML", "-r", OUTPUT_FOLDER_NAME );

		//Then
		assertThat( "Command line runner did not quit gracefully, exit value not as expected", exitValue, is( 0 ) );

		//Then
		assertThat( outputFiles(), is( not( empty() ) ) );

		//Then
		assertThat( statisticsXMLFile(), hasXPath( GET_FIRST_CHART_IMAGE_XPATH,
				lenghtGreaterThan( MINIMUM_REASONABLE_CHARACTER_LENGTH_OF_CHART_IMAGE ) ) );

	}

	private Source statisticsXMLFile()
	{
		return the( CommandLineLauncherTestUtils.getXMLFrom( findFileWithNameContaining( outputFiles(), "statistics" ) ) );
	}

	private Collection<File> outputFiles()
	{
		return CommandLineLauncherTestUtils.getFilesAt( getOutPutFolderPath() );
	}

	private File findFileWithNameContaining( Collection<File> outputFiles, String partOfName )
	{
		for( File file : outputFiles )
		{
			if( file.getName().contains( partOfName ) )
				return file;
		}

		throw new RuntimeException( "Could not find file with a name that contains \"" + partOfName + "\"" );
	}

	private String getOutPutFolderPath()
	{
		return CommandLineLauncherTestUtils.getPathToCommandLineRunnerFile() + File.separator + OUTPUT_FOLDER_NAME;
	}

	private String getProjectFilePath( String name )
	{
		return this.getClass().getResource( name ).getFile();
	}

}
