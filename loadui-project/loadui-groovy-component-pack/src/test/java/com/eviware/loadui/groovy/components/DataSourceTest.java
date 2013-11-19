package com.eviware.loadui.groovy.components;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.FlowCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;
import com.google.common.base.Joiner;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Author: maximilian.skog
 * Date: 2013-11-19
 * Time: 17:10
 */
public class DataSourceTest
{
	private ComponentItem component;
	private GroovyComponentTestUtils ctu;

	@Before
	public void classSetup()
	{
		ctu = new GroovyComponentTestUtils();
		ctu.initialize( Joiner.on( File.separator ).join( "src", "main", "groovy" ) );
	}

	@Before
	public void setup() throws ComponentCreationException
	{
		ctu.getDefaultBeanInjectorMocker();
		component = ctu.createComponent( "DataSource" );
	}

	@Test
	public void shouldHaveCorrectTerminals()
	{
		assertThat( component.getTerminals().size(), is( 2 ) );

		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( FlowCategory.INCOMING_TERMINAL );
		assertThat( incoming.getLabel(), is( "Incoming messages" ) );

		OutputTerminal output = ( OutputTerminal )component.getTerminalByName( "output" );
		assertThat( output.getLabel(), is( "Output Terminal 1" ) );
	}
}


