package com.eviware.loadui.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static com.eviware.loadui.util.CanvasItemNameGenerator.findAvailableName;
import static org.junit.Assert.assertEquals;

public class CanvasItemNameGeneratorTest
{
	@Test
	public void testFindAvailableName()
	{
		List<String> occupiedNames = ImmutableList.of( "Web Page Runner 2", "Foobar", "Web Page Runner 1", "Foobar", "Fixed Rate Generator 3", "Fixed Rate Generator" );

		assertEquals( findAvailableName( "Web Page Runner", occupiedNames ), "Web Page Runner 3" );
		assertEquals( findAvailableName( "Interval Scheduler", occupiedNames ), "Interval Scheduler 1" );
		assertEquals( findAvailableName( "Fixed Rate Generator", occupiedNames ), "Fixed Rate Generator 1" );
	}
}
