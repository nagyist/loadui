package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.*;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.OpenSourceFxLoadedState;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.projects.ComponentBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static com.eviware.loadui.util.projects.ComponentBuilder.LoadUiComponent.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;

@Category( IntegrationTest.class )
public class ProjectBuilderTest
{
	private ProjectRef projectRef;
	private ProjectBuilder projectBuilder;
	private WorkspaceProvider workspaceProvider;

	@Before
	public void enterState()
	{
		//Given
		OpenSourceFxLoadedState.STATE.enter();
		projectBuilder = BeanInjector.getBean( ProjectBuilderFactory.class ).newInstance();
		workspaceProvider = BeanInjector.getBean( WorkspaceProvider.class );
	}

	@Test
	public void shouldCreateTheSimplestOfProjects()
	{
		//When
		projectRef = projectBuilder.create().build();

		enableProject();

		//Then
		assertThat( "Component count", getProject().getComponents(), is( empty() ) );
	}

	@Test
	public void shouldCreateAProjectContainingConnectedComponents()
	{
		//When
		projectRef = projectBuilder.create()
				.components(
						ComponentBuilder.create().type( FIXED_RATE ).property( "rate", Long.class, 10L ).child(
								ComponentBuilder.create().type( WEB_RUNNER ).property( "url", String.class, "win-srvmontest" ).build()
						).build()
				)
				.build();

		enableProject();

		//Then
		CanvasItem canvas = getProject().getCanvas();
		assertThat( "Components exist", canvas.getComponents(), is( not( empty() ) ) );
		assertThat( "Connections count", canvas.getCanvas().getConnections().size(), equalTo( 1 ) );
		assertThat( "Fixed Rate created", canvas.getComponentByLabel( "Fixed Rate 1" ).getProperty( "rate" ).getStringValue(), equalTo( "10" ) );
		assertThat( "Web Page Runner created", getProject().getCanvas().getComponentByLabel( "Web Page Runner 1" ).getProperty( "url" ).getStringValue(), equalTo( "win-srvmontest" ) );

	}

	@Test
	public void shouldCreateAConcurrentUsersProject()
	{
		projectRef = projectBuilder.create()
				.components(
						ComponentBuilder.create().type( FIXED_LOAD ).concurrent().property( "load", Long.class, 2L ).child(
								ComponentBuilder.create().type( WEB_RUNNER ).property( "url", String.class, "win-srvmontest" ).build()
						).build()
				)
				.build();

		enableProject();

		//Then
		assertThat( "Project should contain Fixed Load", getProject().getCanvas().getComponentByLabel( "Fixed Load 1" ), notNullValue() );
		Terminal sampleCount = getProject().getCanvas().getComponentByLabel( "Fixed Load 1" ).getTerminalByName( "Sample Count" );
		assertThat( "Connected as a concurrent-users scenario", sampleCount.getConnections().size(), equalTo( 1 ) );
	}

	private void enableProject(){
		try{
			projectRef.setEnabled( true );
		}catch(IOException e){
			fail();
		}
	}

	private void disableProject(){
		try{
			projectRef.setEnabled( false );
		}catch(IOException e){
			fail();
		}
	}

	private void fail(){
		assert false;
	}

	private ProjectItem getProject(){
		return projectRef.getProject();
	}

	@After
	public void leaveState()
	{
		disableProject();
		projectRef.delete( true );
	}
}
