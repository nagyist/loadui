package com.eviware.loadui.components.rest.statistics;

import com.eviware.loadui.api.base.Clock;
import com.eviware.loadui.util.RealClock;

import java.io.IOException;
import java.io.InputStream;

public class LatencyCalculator
{
	public static LatencyCalculator create()
	{
		return new LatencyCalculator( new RealClock() );
	}

	public static LatencyCalculator usingClock( Clock clock )
	{
		return new LatencyCalculator( clock );
	}

	private static byte[] firstByte = new byte[1];
	private final Clock clock;

	private LatencyCalculator( Clock clock )
	{
		this.clock = clock;
	}

	public long calculate( InputStream in, long startTime )
	{
		try
		{
			in.read( firstByte );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		return (clock.nanos() - startTime)/1000000;
	}

}
