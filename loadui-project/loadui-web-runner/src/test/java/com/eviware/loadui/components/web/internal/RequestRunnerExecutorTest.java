package com.eviware.loadui.components.web.internal;

import com.eviware.loadui.components.web.WebRunnerStatsSender;
import com.eviware.loadui.util.test.FakeClock;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class RequestRunnerExecutorTest
{

	static final long EACH_REQUEST_TIME = 50L;
	RequestRunnerExecutor executor;
	CloseableHttpAsyncClient client = mock( CloseableHttpAsyncClient.class );
	WebRunnerStatsSender statsSender = mock( WebRunnerStatsSender.class );
	FakeClock clock = new FakeClock();

	@Before
	public void setup()
	{
		executor = new RequestRunnerExecutor( client, statsSender, clock );
	}

	@Test
	public void noTests()
	{

	}

	@After
	public void cleanup()
	{
		executor.cancelAll();
	}


}
