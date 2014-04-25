/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.component.categories;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticVariable.Mutable;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.util.component.ComponentTestUtils;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class RunnerBaseTest
{
	private RunnerBase runnerBase;
	private ComponentItem component;
	private InputTerminal triggerTerminal;
	private OutputTerminal resultsTerminal;
	private ComponentTestUtils ctu;

	/**
	 * Set this to something else to do something every time the runnerBase runs a sample
	 */
	private Runnable runOnSample = new Runnable()
	{
		@Override
		public void run()
		{
			// does nothing by default.
		}
	};

	@Before
	@SuppressWarnings( "unchecked" )
	public void setup()
	{
		ctu = new ComponentTestUtils();
		ctu.getDefaultBeanInjectorMocker();

		component = ctu.createComponentItem();
		ComponentItem componentSpy = spy( component );
		ComponentContext contextSpy = spy( component.getContext() );
		doReturn( contextSpy ).when( componentSpy ).getContext();
		doReturn( componentSpy ).when( contextSpy ).getComponent();

		final Mutable mockVariable = mock( StatisticVariable.Mutable.class );
		when( mockVariable.getStatisticHolder() ).thenReturn( componentSpy );
		@SuppressWarnings( "rawtypes" )
		final Statistic statisticMock = mock( Statistic.class );
		when( statisticMock.getStatisticVariable() ).thenReturn( mockVariable );
		when( mockVariable.getStatistic( anyString(), anyString() ) ).thenReturn( statisticMock );
		doReturn( mockVariable ).when( contextSpy ).addStatisticVariable( anyString(), anyString(),
				Matchers.<String> anyVararg() );
		doReturn( mockVariable ).when( contextSpy ).addListenableStatisticVariable( anyString(), anyString(),
				Matchers.<String> anyVararg() );

		runnerBase = new RunnerBase( contextSpy )
		{
			@Override
			protected TerminalMessage sample( TerminalMessage triggerMessage, Object sampleId )
					throws SampleCancelledException
			{
				runOnSample.run();
				return triggerMessage;
			}

			@Override
			protected int onCancel()
			{
				return 0;
			}

		};
		ctu.setComponentBehavior( component, runnerBase );
		contextSpy.setNonBlocking( true );
		component = componentSpy;

		triggerTerminal = runnerBase.getTriggerTerminal();
		resultsTerminal = runnerBase.getResultTerminal();
	}

	@Test
	public void shouldSampleOnIncomingMessage() throws InterruptedException
	{
		BlockingQueue<TerminalMessage> results = ctu.getMessagesFrom( resultsTerminal );
		ctu.sendMessage( triggerTerminal,
				ImmutableMap.<String, Object> of( GeneratorCategory.TRIGGER_TIMESTAMP_MESSAGE_PARAM, 0 ) );

		TerminalMessage message = results.poll( 5, TimeUnit.SECONDS );
		assertThat( message.get( GeneratorCategory.TRIGGER_TIMESTAMP_MESSAGE_PARAM ), is( ( Object )0 ) );
		assertThat( message.size(), is( 3 ) );
		assertThat( runnerBase.getRequestCounter().getValue(), is( 1L ) );
		assertThat( runnerBase.getSampleCounter().getValue(), is( 1L ) );
		assertThat( runnerBase.getFailureCounter().getValue(), is( 0L ) );
	}

	@Test
	public void shouldSampleEvenOnExceptionThrown() throws InterruptedException
	{
		runOnSample = new Runnable()
		{
			@Override
			public void run()
			{
				throw new RuntimeException( "Break the sample method" );
			}
		};

		BlockingQueue<TerminalMessage> results = ctu.getMessagesFrom( resultsTerminal );
		ctu.sendMessage( triggerTerminal,
				ImmutableMap.<String, Object> of( GeneratorCategory.TRIGGER_TIMESTAMP_MESSAGE_PARAM, 0 ) );

		TerminalMessage message = results.poll( 5, TimeUnit.SECONDS );
		assertThat( message.get( GeneratorCategory.TRIGGER_TIMESTAMP_MESSAGE_PARAM ), is( ( Object )0 ) );
		assertThat( runnerBase.getRequestCounter().getValue(), is( 1L ) );
		assertThat( runnerBase.getSampleCounter().getValue(), is( 1L ) );
		assertThat( runnerBase.getFailureCounter().getValue(), is( 1L ) );
	}

}
