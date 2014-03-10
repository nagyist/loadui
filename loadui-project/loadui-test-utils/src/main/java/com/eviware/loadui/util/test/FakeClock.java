package com.eviware.loadui.util.test;

import com.eviware.loadui.api.base.Clock;

public class FakeClock implements Clock
{
	public long millis = 0;
	public long nanos = 0;

	@Override
	public long millis()
	{
		return millis;
	}

	@Override
	public long nanos()
	{
		return nanos;
	}

	public void elapseTimeBy( long millis )
	{
		this.millis += millis;
		this.nanos += millis * 1_000_000;
	}
}
