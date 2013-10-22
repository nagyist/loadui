package com.eviware.loadui.impl;

import com.eviware.loadui.api.model.AttributeHolder;
import com.eviware.loadui.api.ui.LatestDirectoryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Author: maximilian.skog
 * Date: 2013-10-01
 * Time: 15:39
 */
public class SavedDirectoryServiceImplTest
{
	private LatestDirectoryService latestDirectoryService;
	private String storedAttribute;
	private AttributeHolder attributeHolder;

	@Before
	public void setup() throws Exception
	{

		storedAttribute = System.getProperty( "user.dir" );

		attributeHolder = new AttributeHolder()
		{
			@Override
			public void setAttribute( String key, String value )
			{
				storedAttribute = value;
			}

			@Override
			public String getAttribute( String key, String defaultValue )
			{
				return storedAttribute;
			}

			@Override
			public void removeAttribute( String key )
			{

			}

			@Nonnull
			@Override
			public Collection<String> getAttributes()
			{
				return Collections.emptyList();
			}
		};

		latestDirectoryService = new LatestDirectoryServiceImpl();
	}

	@After
	public void tearDown() throws Exception
	{

	}

	@Test
	public void isGettingLatestValidAndStoredDirectory()
	{
		//given
		storedAttribute = System.getProperty( "user.dir" );

		//when
		File outputDirectory = latestDirectoryService.getLatestDirectory( "identifier", attributeHolder );

		//then
		assertTrue( "Directory " + outputDirectory.getAbsolutePath() + " is not a directory", outputDirectory.isDirectory() );
		assertThat( outputDirectory, equalTo( new File( storedAttribute ) ) );
	}

	@Test
	public void whenLatestIsNonvalidItShouldReturnAValidOne()
	{
		//given
		storedAttribute = "non-valid dir";

		//when
		File outputDirectory = latestDirectoryService.getLatestDirectory( "identifier", attributeHolder );

		//then
		assertTrue( "Directory " + outputDirectory.getAbsolutePath() + " is not a directory", outputDirectory.isDirectory() );
	}


	@Test
	public void SettingAValidDirectoryShouldMakeItReturnTheSame() throws Exception
	{
		//given
		File validInputDirectory = new File( System.getProperty( "user.dir" ) );
		latestDirectoryService.setLatestDirectory( "identifier", validInputDirectory.getAbsoluteFile(), attributeHolder );

		//when
		File outputDirectory = latestDirectoryService.getLatestDirectory( "identifier", attributeHolder );

		//then
		assertThat( outputDirectory, equalTo( validInputDirectory ) );
	}

	@Test
	public void SettingANonValidDirectoryShouldMakeItReturnAValidOne() throws Exception
	{
		//given
		File nonValidInputDirectory = new File( "non-valid dir" );
		latestDirectoryService.setLatestDirectory( "identifier", nonValidInputDirectory.getAbsoluteFile(), attributeHolder );

		//when
		File outputDirectory = latestDirectoryService.getLatestDirectory( "identifier", attributeHolder );

		//then
		assertThat( outputDirectory, not( equalTo( nonValidInputDirectory ) ) );
		assertTrue( "Directory " + outputDirectory.getAbsolutePath() + " is not a directory", outputDirectory.isDirectory() );
	}
}
