package com.eviware.loadui.util.statistics;

import com.eviware.loadui.api.statistics.store.Entry;

import java.util.Set;

public class AdjustedEntry implements Entry
{
	private final Entry delegate;
	private final long timestamp;

	public AdjustedEntry( Entry delegate, long timestamp )
	{
		this.delegate = delegate;
		this.timestamp = timestamp;
	}

	@Override
	public long getTimestamp()
	{
		return timestamp;
	}

	@Override
	public Set<String> getNames()
	{
		return delegate.getNames();
	}

	@Override
	public Number getValue( String name )
	{
		return delegate.getValue( name );
	}

}