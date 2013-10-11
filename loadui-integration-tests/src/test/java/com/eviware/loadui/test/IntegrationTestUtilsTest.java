package com.eviware.loadui.test;

import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import com.google.common.base.Function;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.Random;

import static com.google.common.collect.Collections2.transform;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author renato
 */
public class IntegrationTestUtilsTest
{
	@Rule
	public RepeatingRule rule = new RepeatingRule();

	@Test
	public void testNewestDirectoryIn_HappyPath()
	{
		final int numberOfFilesToTestWith = 6;
		File mockDir = mock( File.class );

		Random random = new Random();
		File[] files = new File[numberOfFilesToTestWith];
		for( int i = 0; i < numberOfFilesToTestWith; i++ )
		{
			File file = mock( File.class );
			when( file.lastModified() ).thenReturn( random.nextLong() );
			when( file.isDirectory() ).thenReturn( true );
			files[i] = file;
		}

		when( mockDir.listFiles() ).thenReturn( files );

		File newest = IntegrationTestUtils.newestDirectoryIn( mockDir );

		assertNotNull( newest );

		for( File file : files )
		{
			assertThat( newest.lastModified(), greaterThanOrEqualTo( file.lastModified() ) );
		}
	}

	@Test
	@Repeating( repetition = 10 )
	public void testNewestDirectoryIn_WithFilesAndDirectories()
	{
		System.out.println( "Running test" );
		final int numberOfFilesToTestWith = 20;

		File mockDir = mock( File.class );

		Random random = new Random();
		File[] files = new File[numberOfFilesToTestWith];
		for( int i = 0; i < numberOfFilesToTestWith; i++ )
		{
			File file = mock( File.class );
			when( file.lastModified() ).thenReturn( random.nextLong() );
			when( file.isDirectory() ).thenReturn( random.nextBoolean() );
			files[i] = file;
		}

		assertThat( transform( Arrays.asList( files ), new Function<File, Boolean>()
		{
			@Nullable
			@Override
			public Boolean apply( @Nullable File input )
			{
				return input != null && input.isDirectory();
			}
		} ), not( contains( false ) ) );

		when( mockDir.listFiles() ).thenReturn( files );

		File newest = IntegrationTestUtils.newestDirectoryIn( mockDir );

		assertNotNull( newest );
		assertThat( newest.isDirectory(), is( true ) );

		for( File file : files )
		{
			if( file.isDirectory() )
				assertThat( newest.lastModified(), greaterThanOrEqualTo( file.lastModified() ) );
		}

	}

}
