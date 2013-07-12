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


import com.eviware.loadui.test.CommandLineLauncherUtils;
import com.eviware.loadui.test.categories.IntegrationTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.xml.transform.Source;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static com.eviware.loadui.util.test.matchers.StringLengthMatcher.lenghtGreaterThan;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.xmlmatchers.transform.XmlConverters.the;
import static org.xmlmatchers.xpath.HasXPath.hasXPath;

/**
 * Author: maximilian.skog
 * Date: 2013-07-04
 * Time: 10:59
 */
@Category( IntegrationTest.class )
public class commandLineRunnerReportTest
{
	private static String pathToRunner;
	private static final String OUTPUT_FOLDER_NAME = "test-out-put";
	private static final String GET_IMAGE_XPATH = "//jasperPrint[1]/page[1]/image[1]/imageSource[1]";


	@BeforeClass
	public static void findPath()
	{
		pathToRunner = CommandLineLauncherUtils.findPathToCommandLineBat();
	}


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
			System.err.println( "Failed to delete output directory" );
			e.printStackTrace();
		}
	}

	@Test
	public void reportCreatedTest()
	{
		//Given
		int exitValue = launchCommandLineRunnerWithCommands( "-p", getProjectFilePath( "basictest.xml" ), "-L", "1:0:0", "-F", "XML", "-r", OUTPUT_FOLDER_NAME );

		//Then
		assertThat( "Command line runner did not quit gracefully, exit value not as expected", exitValue, is( 0 ) );

		//Then
		assertThat( outputFiles(), is( not( empty() ) ) );

		//Then
		assertThat( statisticsXMLFile(), hasXPath( GET_IMAGE_XPATH, lenghtGreaterThan( 2000 ) ) );

	}

	private Source statisticsXMLFile()
	{
		return the( CommandLineLauncherUtils.getXMLFrom( findFileWithNameContaining( outputFiles(), "statistics" ) ) );
	}

	private Collection<File> outputFiles()
	{
		return CommandLineLauncherUtils.getFilesAt( getOutPutFolderPath() );
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

	private int launchCommandLineRunnerWithCommands( String... Commands )
	{
		return CommandLineLauncherUtils.launchCommandLineRunner( pathToRunner, Commands );
	}

	private String getOutPutFolderPath()
	{
		return pathToRunner + File.separator + OUTPUT_FOLDER_NAME;
	}

	private String getProjectFilePath( String name )
	{
		return this.getClass().getResource( name ).getFile();
	}

}
