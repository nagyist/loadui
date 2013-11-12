package com.eviware.loadui.components.soapui.layout;

import com.google.common.base.Joiner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.Parameters;

/**
 * @author renato
 */
@RunWith( Parameterized.class )
public class FileResolverTest
{

	SoapUiFilePicker.FileResolver resolver = new SoapUiFilePicker.FileResolver();

	File absPath;
	String relPath;
	final File baseDir = new File( File.separator + "base" );

	@Parameters
	public static Collection data()
	{
		return Arrays.asList( new Object[][] {
				//           ABSOLUTE,               RELATIVE
				{ new String[] { "base", "b.xml" }, new String[] { "b.xml" } },
				{ new String[] { "base", "a", "b.xml" }, new String[] { "a", "b.xml" } },
				{ new String[] { "b.xml" }, new String[] { "..", "b.xml" } },
		} );
	}

	public FileResolverTest( String[] absPath, String[] relPath )
	{
		this.absPath = new File( absPath( absPath ) );
		this.relPath = relPath( relPath );
	}

	@Test
	public void test_rel2abs() throws IOException
	{
		assertThat( canonicalPath( resolver.rel2abs( baseDir, relPath ) ),
				equalTo( canonicalPath( absPath.getAbsolutePath() ) ) );
	}

	@Test
	public void test_abs2rel() throws IOException
	{
		assertThat( canonicalPath( baseDir, resolver.abs2rel( baseDir, absPath ) ),
				equalTo( canonicalPath( baseDir, relPath ) ) );
	}

	@Test
	public void testResolveFromText_rel2abs() throws IOException
	{
		File result = resolver.resolveFromText( false, baseDir, relPath );
		assertThat( canonicalPath( result.getAbsolutePath() ),
				equalTo( canonicalPath( absPath.getAbsolutePath() ) ) );
	}

	@Test
	public void testResolveFromText_abs2rel() throws IOException
	{
		File result = resolver.resolveFromText( true, baseDir, absPath.getAbsolutePath() );
		assertThat( result.getPath(), equalTo( relPath ) );
	}

	public static String canonicalPath( String path ) throws IOException
	{
		return new File( path ).getCanonicalPath();
	}

	public static String canonicalPath( File baseDir, String path ) throws IOException
	{
		return new File( baseDir, path ).getCanonicalPath();
	}


	public static String relPath( String... path )
	{
		return Joiner.on( File.separator ).join( path );
	}

	public static String absPath( String... path )
	{
		return File.separator + relPath( path );
	}

}
