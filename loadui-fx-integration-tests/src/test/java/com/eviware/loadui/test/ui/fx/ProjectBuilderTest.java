package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.LoadUI;
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;

@Category( IntegrationTest.class )
public class ProjectBuilderTest
{
	private ProjectRef projectRef;
	private ProjectBuilder projectBuilder;

	@Before
	public void enterState()
	{
		//Given
		OpenSourceFxLoadedState.STATE.enter();
		projectBuilder = BeanInjector.getBean( ProjectBuilderFactory.class ).newInstance();
	}

	@Test
	public void shouldCreateTheSimplestOfProjects()
	{
		//When
		projectRef = projectBuilder.create().importProject( true ).build();

		ProjectItem project = enableProject( projectRef );

		//Then
		assertThat( "Component count", project.getComponents(), is( empty() ) );
	}

	@Test
	public void shouldCreateProjectsInAnotherFolder()
	{
		//When
		projectRef = projectBuilder.create().importProject( true ).where( LoadUI.getWorkingDir() ).build();

		assertThat( "Component count", projectRef.getProjectFile().getPath(), containsString( LoadUI.getWorkingDir().getPath() ) );
	}

	@Test
	public void shouldCreateAProjectContainingConnectedComponents()
	{
		//When
		projectRef = projectBuilder.create()
				.importProject( true )
				.components(
						ComponentBuilder.create().type( FIXED_RATE ).property( "rate", Long.class, 10L ).child(
								ComponentBuilder.create().type( WEB_RUNNER ).property( "url", String.class, "http://05ten.se" ).build()
						).build()
				)
				.build();

		ProjectItem project = enableProject( projectRef );

		//Then
		CanvasItem canvas = project.getCanvas();
		assertThat( "Components exist", canvas.getComponents(), is( not( empty() ) ) );
		assertThat( "Connections count", canvas.getCanvas().getConnections().size(), equalTo( 1 ) );
		assertThat( "Fixed Rate created", canvas.getComponentByLabel( "Fixed Rate 1" ).getProperty( "rate" ).getStringValue(), equalTo( "10" ) );
		assertThat( "Web Page Runner created", project.getCanvas().getComponentByLabel( "Web Page Runner 1" ).getProperty( "url" ).getStringValue(), equalTo( "http://05ten.se" ) );

	}

	@Test
	public void shouldCreateAConcurrentUsersProject()
	{
		projectRef = projectBuilder.create()
				.importProject( true )
				.components(
						ComponentBuilder.create().type( FIXED_LOAD ).concurrent().property( "load", Long.class, 2L ).child(
								ComponentBuilder.create().type( WEB_RUNNER ).property( "url", String.class, "http://05ten.se" ).build()
						).build()
				)
				.build();

		ProjectItem project = enableProject( projectRef );

		//Then
		assertThat( "Project should contain Fixed Load", project.getCanvas().getComponentByLabel( "Fixed Load 1" ), notNullValue() );
		Terminal sampleCount = project.getCanvas().getComponentByLabel( "Fixed Load 1" ).getTerminalByName( "Sample Count" );
		assertThat( "Connected as a concurrent-users scenario", sampleCount.getConnections().size(), equalTo( 1 ) );
	}

	public ProjectItem enableProject( ProjectRef project )
	{
		try
		{
			project.setEnabled( true );
			return project.getProject();
		}
		catch( IOException e )
		{
			System.err.println( "Cannot enable project" );
			return null;
		}
	}

	@After
	public void leaveState()
	{
		projectRef.delete();
	}
}
