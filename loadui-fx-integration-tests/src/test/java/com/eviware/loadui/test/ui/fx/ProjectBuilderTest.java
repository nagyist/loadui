package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectBuilder;
import com.eviware.loadui.api.model.ProjectBuilderFactory;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.OpenSourceFxLoadedState;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.projects.ComponentBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.loadui.testfx.categories.TestFX;

import static com.eviware.loadui.util.projects.ComponentBuilder.LoadUiComponent.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;

@Category( IntegrationTest.class )
public class ProjectBuilderTest
{
	private ProjectItem project;
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
		project = projectBuilder.create().build().getProject();

		//Then
		assertThat( "Component count", project.getComponents(), is( empty() ) );
	}

	@Test
	public void shouldCreateAProjectContainingConnectedComponents()
	{
		//When
		project = projectBuilder.create()
				.components(
						ComponentBuilder.create().type( FIXED_RATE ).property( "rate", Long.class, 10L ).child(
								ComponentBuilder.create().type( WEB_RUNNER ).property( "url", String.class, "win-srvmontest" ).build()
						).build()
				)
				.build().getProject();

		//Then
		CanvasItem canvas = project.getCanvas();
		assertThat( "Components exist", canvas.getComponents(), is( not( empty() ) ) );
		assertThat( "Connections count", canvas.getCanvas().getConnections().size(), equalTo( 1 ) );
		assertThat( "Fixed Rate created", canvas.getComponentByLabel( "Fixed Rate 1" ).getProperty( "rate" ).getStringValue(), equalTo( "10" ) );
		assertThat( "Web Page Runner created", project.getCanvas().getComponentByLabel( "Web Page Runner 1" ).getProperty( "url" ).getStringValue(), equalTo( "win-srvmontest" ) );

	}

	@Test
	public void shouldCreateAConcurrentUsersProject()
	{
		project = projectBuilder.create()
				.components(
						ComponentBuilder.create().type( FIXED_LOAD ).concurrent().property( "load", Long.class, 2L ).child(
								ComponentBuilder.create().type( WEB_RUNNER ).property( "url", String.class, "win-srvmontest" ).build()
						).build()
				)
				.build().getProject();

		//Then
		assertThat( "Project should contain Fixed Load", project.getCanvas().getComponentByLabel( "Fixed Load 1" ), notNullValue() );
		Terminal sampleCount = project.getCanvas().getComponentByLabel( "Fixed Load 1" ).getTerminalByName( "Sample Count" );
		assertThat( "Connected as a concurrent-users scenario", sampleCount.getConnections().size(), equalTo( 1 ) );
	}

	@After
	public void leaveState()
	{
		project.delete();
	}
}
