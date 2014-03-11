package com.eviware.loadui.util;

import com.eviware.loadui.api.base.Clock;

public class RealClock implements Clock
{
	@Override
	public long millis()
	{
		return System.currentTimeMillis();
	}

	@Override
	public long nanos()
	{
		return System.nanoTime();
	}
}
