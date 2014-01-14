package com.eviware.loadui.groovy.components;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;
import com.eviware.loadui.util.groovy.GroovyEnvironment;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Joiner;
import groovy.lang.Binding;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by osten on 1/10/14.
 */
public class RampLoadTest
{
	private ComponentItem component;
	private GroovyComponentTestUtils ctu;

	@Before
	public void setup() throws ComponentCreationException
	{
		ctu = new GroovyComponentTestUtils();
		ctu.initialize( Joiner.on( File.separator ).join( "src", "main", "groovy" ) );
		ctu.getDefaultBeanInjectorMocker();
		component = ctu.createComponent( "Ramp Load" );
		component.fireEvent( new ActionEvent( component, CanvasItem.STOP_ACTION ) );
	}

	@Test
	public void shouldHaveCorrectTerminals()
	{
		assertThat( component.getTerminals().size(), is( 3 ) );

		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( GeneratorCategory.STATE_TERMINAL );
		assertThat( incoming.getLabel(), is( "Component activation" ) );

		InputTerminal running = ( InputTerminal) component.getTerminalByName( "Sample Count" );
		assertThat( running.getLabel(), is("Currently running feedback") );

		OutputTerminal trigger = ( OutputTerminal )component.getTerminalByName( GeneratorCategory.TRIGGER_TERMINAL );
		assertThat( trigger.getLabel(), is( "Trigger Signal" ) );
	}
}