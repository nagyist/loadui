package com.eviware.loadui.util.component;

import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.TerminalMessage;
import org.junit.Before;

import java.util.concurrent.BlockingQueue;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.spy;

public abstract class ComponentTestBase
{

	protected ComponentItem component;
	protected ComponentTestUtils ctu;
	protected BlockingQueue<TerminalMessage> results;
	protected ComponentContext contextSpy;
	protected ComponentBehavior behavior;

	public abstract ComponentBehavior provideBehavior();

	@Before
	public void setup() throws ComponentCreationException
	{
		ctu = new ComponentTestUtils();
		ctu.getDefaultBeanInjectorMocker();

		component = ctu.createComponentItem();
		contextSpy = spy( component.getContext() );
		ctu.mockStatisticsFor( component, contextSpy );
		ctu.setComponentBehavior( component, behavior );
		this.behavior = provideBehavior();
	}

	protected void setProperty( String name, Object value )
	{
		component.getContext().getProperty( name ).setValue( value );
	}

	protected TerminalMessage getNextOutputMessage() throws InterruptedException
	{
		return results.poll( 1, SECONDS );
	}


}
