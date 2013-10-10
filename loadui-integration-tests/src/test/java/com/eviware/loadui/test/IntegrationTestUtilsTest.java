package com.eviware.loadui.test;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author renato
 */
public class IntegrationTestUtilsTest
{

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

		assertThat( newest, notNullValue() );

		for( File file : files )
		{
			assertThat( newest.lastModified(), greaterThanOrEqualTo( file.lastModified() ) );
		}
	}

	@Test
	public void testNewestDirectoryIn_WithFilesAndDirectories()
	{
		final int numberOfFilesToTestWith = 20;

		for( int run = 0; run < 10; run++ )
		{
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

			assertThat( Collections2.transform( Arrays.asList( files ), new Function<File, Boolean>()
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

			assertThat( newest, notNullValue() );
			assertThat( newest.isDirectory(), is( true ) );

			for( File file : files )
			{
				if( file.isDirectory() )
					assertThat( newest.lastModified(), greaterThanOrEqualTo( file.lastModified() ) );
			}
		}

	}

}
