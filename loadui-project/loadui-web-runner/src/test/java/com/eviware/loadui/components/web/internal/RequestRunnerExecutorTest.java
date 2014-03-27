package com.eviware.loadui.components.web.internal;

import com.eviware.loadui.components.web.RequestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestRunnerExecutorTest
{

	static final long EACH_REQUEST_TIME = 50L;
	RequestRunnerExecutor executor = new RequestRunnerExecutor();

	@Before
	public void setup()
	{
		assertThat( executor.getExecutors(), is( empty() ) );
	}

	@After
	public void cleanup()
	{
		executor.shutdown();
	}

	@Test
	public void runsRequestsInDifferentThreads() throws Exception
	{
		RequestRunner.Request mockReq = mock( RequestRunner.Request.class );
		final CountDownLatch latch = new CountDownLatch( 2 );
		when( mockReq.call() ).thenAnswer( new Answer<Void>()
		{
			@Override
			public Void answer( InvocationOnMock invocation ) throws Throwable
			{
				Thread.sleep( EACH_REQUEST_TIME );
				latch.countDown();
				return null;
			}
		} );

		long startTime = System.currentTimeMillis();
		Future<List<Future<Boolean>>> results = executor.runAll( Arrays.asList( mockReq, mockReq ) );
		long endTime = System.currentTimeMillis();

		boolean ok = latch.await( 1, TimeUnit.SECONDS );
		long finalTime = System.currentTimeMillis();

		assertThat( ok, is( true ) );
		assertThat( endTime - startTime, is( lessThan( EACH_REQUEST_TIME ) ) );
		assertThat( finalTime - startTime, is(
				both( greaterThanOrEqualTo( EACH_REQUEST_TIME ) )
						.and( lessThan( 2 * EACH_REQUEST_TIME ) )
		) );
		assertThat( results.get(), hasSize( 2 ) );
	}

	@Test
	public void runsOverlappingRequestBatchesInDifferentExecutors() throws Exception
	{
		RequestRunner.Request mockReq = mock( RequestRunner.Request.class );
		final CountDownLatch latch = new CountDownLatch( 4 );

		when( mockReq.call() ).thenAnswer( new Answer<Void>()
		{
			@Override
			public Void answer( InvocationOnMock invocation ) throws Throwable
			{
				Thread.sleep( EACH_REQUEST_TIME );
				latch.countDown();
				return null;
			}
		} );

		long startTime = System.currentTimeMillis();
		Future<List<Future<Boolean>>> results1 = executor.runAll( Arrays.asList( mockReq, mockReq ) );
		long endTime1 = System.currentTimeMillis();
		Future<List<Future<Boolean>>> results2 = executor.runAll( Arrays.asList( mockReq, mockReq ) );
		long endTime2 = System.currentTimeMillis();

		boolean ok = latch.await( 1, TimeUnit.SECONDS );
		long finalTime = System.currentTimeMillis();

		assertThat( ok, is( true ) );
		assertThat( endTime1 - startTime, is( lessThan( EACH_REQUEST_TIME ) ) );
		assertThat( endTime2 - startTime, is( lessThan( EACH_REQUEST_TIME ) ) );
		assertThat( finalTime - startTime, is(
				both( greaterThanOrEqualTo( EACH_REQUEST_TIME ) )
						.and( lessThan( 2 * EACH_REQUEST_TIME ) )
		) );
		assertThat( results1.get(), hasSize( 2 ) );
		assertThat( results2.get(), hasSize( 2 ) );
		assertThat( executor.getExecutors(), hasSize( 2 ) );
	}

	@Test
	public void noNewExecutorIsCreatedIfRequestBatchesDoNotOverlap() throws Throwable
	{
		RequestRunner.Request mockReq = mock( RequestRunner.Request.class );
		final CountDownLatch latch1 = new CountDownLatch( 1 );
		final CountDownLatch latch2 = new CountDownLatch( 1 );

		when( mockReq.call() ).thenAnswer( new Answer<Void>()
		{
			@Override
			public Void answer( InvocationOnMock invocation ) throws Throwable
			{
				Thread.sleep( EACH_REQUEST_TIME );
				if( latch1.getCount() == 0 )
					latch2.countDown();
				else
					latch1.countDown();
				return null;
			}
		} );

		Future<List<Future<Boolean>>> results1 = executor.runAll( Arrays.asList( mockReq ) );
		boolean ok1 = latch1.await( 1, TimeUnit.SECONDS );

		assertThat( ok1, is( true ) );
		Thread.sleep( EACH_REQUEST_TIME );

		Future<List<Future<Boolean>>> results2 = executor.runAll( Arrays.asList( mockReq ) );
		boolean ok2 = latch2.await( 1, TimeUnit.SECONDS );

		assertThat( ok2, is( true ) );
		assertThat( results1.get(), hasSize( 1 ) );
		assertThat( results2.get(), hasSize( 1 ) );
		assertThat( executor.getExecutors(), hasSize( 1 ) );
	}

}
