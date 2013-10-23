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
package com.eviware.loadui.test.states;

import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.categories.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.osgi.framework.Bundle;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Category( IntegrationTest.class )
public class ControllerStartedStateTest
{

	@Before
	public void enterState()
	{
		ControllerStartedState.getState().enter();
	}

	@Test
	public void shouldHaveWorkspaceProvider() throws Exception
	{
		ensureAllBundlesHaveStartedUp();
		ensureWorkspaceProviderWasPublishedAsAService();
	}

	private void ensureAllBundlesHaveStartedUp()
	{
		Bundle[] bundles = ControllerStartedState.getState().controller.getBundleContext().getBundles();
		for( Bundle bundle : bundles )
		{
			System.out.println( "Bundle " + bundle.getSymbolicName() + " has status " + bundle.getState() );
			assertThat( bundle.getSymbolicName() + " is not Active or Resolved", bundle.getState(),
					anyOf( is( Bundle.ACTIVE ), is( Bundle.RESOLVED ) ) );

		}
	}

	private void ensureWorkspaceProviderWasPublishedAsAService() throws Exception
	{
		WorkspaceProvider workspaceProvider = ControllerStartedState.getState().getService( WorkspaceProvider.class );
		assertThat( workspaceProvider, notNullValue() );
	}

}
