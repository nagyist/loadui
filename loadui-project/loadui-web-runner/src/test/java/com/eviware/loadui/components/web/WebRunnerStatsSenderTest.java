package com.eviware.loadui.components.web;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.util.test.FakeClock;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.concurrent.Immutable;
import java.net.URI;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class WebRunnerStatsSenderTest
{
	public static final String ENDPOINT = "http://www.example.org";
	public static final String ASSET_1 = ENDPOINT + "/image1.png";
	public static final String ASSET_2 = ENDPOINT + "/image2.png";

	WebRunnerStatsSender statsSender;
	ComponentContext context;
	private StatisticVariable.Mutable statisticVariable;

	@Before
	public void setup()
	{
		context = mock( ComponentContext.class );
		statisticVariable = mock( StatisticVariable.Mutable.class );
		when( context.addStatisticVariable( anyString(), anyString(), anyString() ) ).thenReturn( statisticVariable );
		statsSender = new WebRunnerStatsSender( context, new FakeClock() );
	}

	@Test
	public void allResourcesShouldBeAdded() throws Exception
	{
		statsSender.addResources( ImmutableList.of(
				URI.create( ENDPOINT ) )
		);
		statsSender.addResources( ImmutableList.of(
				URI.create( ASSET_1 ),
				URI.create( ASSET_2 )
		) );

		statsSender.updateLatency( ENDPOINT, 4711 );
		statsSender.updateRequestFailed( ASSET_1 );
		statsSender.updateRequestSent( ASSET_2 );

		verify( statisticVariable, times( 3 ) ).update( anyLong(), ( Number )any() );
	}
}
