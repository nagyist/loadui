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
package com.eviware.loadui.groovy.components;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.categories.OutputCategory;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.groovy.util.GroovyComponentTestUtils;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.google.common.base.Joiner;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Category( IntegrationTest.class )
public class TableLogTest
{
	private ComponentItem component;
	private GroovyComponentTestUtils ctu;

	@Before
	public void setup() throws ComponentCreationException
	{
		ctu = new GroovyComponentTestUtils();
		ctu.initialize( Joiner.on( File.separator ).join( "src", "main", "groovy" ) );
		ctu.getDefaultBeanInjectorMocker();
		component = ctu.createComponent( "Table Log" );
	}

	@Test
	public void shouldHaveCorrectTerminals()
	{
		assertThat( component.getTerminals().size(), is( 2 ) );

		InputTerminal incoming = ( InputTerminal )component.getTerminalByName( OutputCategory.INPUT_TERMINAL );
		assertThat( incoming.getLabel(), is( "Data to output" ) );

		OutputTerminal output = ( OutputTerminal )component.getTerminalByName( OutputCategory.OUTPUT_TERMINAL );
		assertThat( output.getLabel(), is( "Passed through messages" ) );
	}
}
