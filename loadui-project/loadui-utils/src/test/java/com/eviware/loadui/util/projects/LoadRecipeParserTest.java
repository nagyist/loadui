package com.eviware.loadui.util.projects;

import com.google.gson.stream.JsonReader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by osten on 1/23/14.
 */
public class LoadRecipeParserTest
{
	@Test
	public void shouldValidateAMinimumProject()
	{
		File json = new File( getClass().getResource( "minimumProject.json" ).getFile() );

		LoadRecipeParser.Body messageBody = parseJsonFile( json );

		assertThat( messageBody.isValid(), is( true ) );
	}

	@Test
	public void shouldValidateAStandardProject()
	{
		File json = new File( getClass().getResource( "standardProject.json" ).getFile() );

		LoadRecipeParser.Body messageBody = parseJsonFile( json );

		assertThat( messageBody.isValid(), is( true ) );
	}

	private LoadRecipeParser.Body parseJsonFile( File json )
	{
		JsonReader reader = null;
		try
		{
			reader = new JsonReader( new FileReader( json ) );
		}
		catch( FileNotFoundException e )
		{
			System.err.println( "Cannot create FileReader" );
		}
		return new LoadRecipeParser().parse( reader );
	}

	@Test
	public void shouldValidateACrazyProject()
	{
		File json = new File( getClass().getResource( "crazyProject.json" ).getFile() );

		LoadRecipeParser.Body messageBody = parseJsonFile( json );

		assertThat( messageBody.isValid(), is( true ) );
	}

	@Test
	public void shouldInvalidateAnInvalidProject()
	{
		File json = new File( getClass().getResource( "invalidProject.json" ).getFile() );

		LoadRecipeParser.Body messageBody = parseJsonFile( json );

		assertThat( messageBody.isValid(), is( false ) );
	}

	@Test
	public void shouldInvalidateAnInsufficientProject()
	{
		File json = new File( getClass().getResource( "insufficientProject.json" ).getFile() );

		LoadRecipeParser.Body messageBody = parseJsonFile( json );

		assertThat( messageBody.isValid(), is( false ) );
	}
}
