package com.eviware.loadui.util.test;

import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.model.CanvasItem;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CounterAsserter
{
	private Map<String, Long> expectedValues = new LinkedHashMap<>();
	private final CounterHolder counterHolder;

	private CounterAsserter( CounterHolder counterHolder )
	{
		this.counterHolder = counterHolder;
	}

	public static CounterAsserter forHolder( CounterHolder counterHolder )
	{
		return new CounterAsserter( counterHolder );
	}

	public static void oneSuccessfulRequest( CounterHolder counterHolder )
	{
		CounterAsserter counterAsserter = new CounterAsserter( counterHolder );
		counterAsserter.sent( 1 ).completed( 1 ).failures( 0 );
	}

	public CounterAsserter failures( long expected )
	{
		assertThat( counterHolder.getCounter( CanvasItem.REQUEST_FAILURE_COUNTER ).get(), is( expected ) );
		return this;
	}

	public CounterAsserter completed( long expected )
	{
		assertThat( counterHolder.getCounter( CanvasItem.SAMPLE_COUNTER ).get(), is( expected ) );
		return this;
	}

	public CounterAsserter sent( long expected )
	{
		assertThat( counterHolder.getCounter( CanvasItem.REQUEST_COUNTER ).get(), is( expected ) );
		return this;
	}
}
