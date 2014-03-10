package com.eviware.loadui.components.rest.statistics;

import com.eviware.loadui.util.test.FakeClock;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LatencyCalculatorTest
{
	public static final long ACTUAL_LATENCY = 100;

	@Test
	public void shouldCalculateCorrectLatency()
	{
		// GIVEN
		FakeClock clock = new FakeClock();
		LatencyCalculator latencyCalculator = LatencyCalculator.usingClock( clock );

		// THEN
		long reportedLatency = latencyCalculator.calculate( FakeStream.usingClock( clock ), 0 );

		assertThat( reportedLatency, is( ACTUAL_LATENCY ));
	}

	@Test
	public void emptyStream_should_resultInZeroLatency()
	{
		// GIVEN
		FakeClock clock = new FakeClock();
		LatencyCalculator latencyCalculator = LatencyCalculator.usingClock( clock );

		// THEN
		long reportedLatency = latencyCalculator.calculate( new EmptyStream() , 0 );

		assertThat( reportedLatency, is( 0L ));
	}

	static class FakeStream extends InputStream
	{
		public static final int RANDOM_BYTE = 5;
		final FakeClock clock;

		public static InputStream usingClock( FakeClock clock )
		{
			return new FakeStream( clock );
		}

		private FakeStream( FakeClock clock )
		{
			this.clock = clock;
		}

		@Override
		public int read() throws IOException
		{
			clock.elapseTimeBy( ACTUAL_LATENCY );
			return RANDOM_BYTE;
		}
	}

	static class EmptyStream extends InputStream
	{
		@Override
		public int read() throws IOException
		{
			return 0;
		}
	}
}
