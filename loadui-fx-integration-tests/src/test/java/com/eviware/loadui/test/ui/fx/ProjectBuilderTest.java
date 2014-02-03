package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.ProjectBuilder;
import com.eviware.loadui.api.model.ProjectBuilderFactory;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.terminal.Terminal;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.OpenSourceFxLoadedState;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.projects.ComponentBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Category( IntegrationTest.class )
public class ProjectBuilderTest extends FxIntegrationTestBase
{
	@Override
	public TestState getStartingState()
	{
		return OpenSourceFxLoadedState.STATE;
	}

	@Test
	public void shouldCreateTheSimplestOfProjects()
	{
		//Given
		ProjectBuilder projectBuilder = BeanInjector.getBean( ProjectBuilderFactory.class ).newInstance();
		HasProjects carousel = new HasProjects();

		//When
		ProjectRef project = projectBuilder.create().build();

		//Then
		carousel.assertProjectExistsWithName( project.getLabel() );
		assertThat( "Component count", project.getProject().getChildren().size(), equalTo( 0 ) );

		//Finally
		project.delete( true );
	}

	@Test
	public void shouldCreateAProjectContainingConnectedComponents()
	{
		//Given
		ProjectBuilder projectBuilder = BeanInjector.getBean( ProjectBuilderFactory.class ).newInstance();
		HasProjects carousel = new HasProjects();

		//When
		ProjectRef project = projectBuilder.create()
				.components(
						ComponentBuilder.create().type( "Fixed Rate" ).property( "rate", Long.class, 10L ).child(
								ComponentBuilder.create().type( "Web Page Runner" ).property( "url", String.class, "win-srvmontest" ).build()
						).build()
				)
				.build();

		//Then
		carousel.assertProjectExistsWithName( project.getLabel() );
		assertThat( "Component count", project.getProject().getChildren().size(), equalTo( 0 ) );
		assertThat( "Connections count", project.getProject().getConnections().size(), equalTo( 1 ) );
		assertThat( "Fixed Rate created", project.getProject().getCanvas().getComponentByLabel( "Fixed Rate 1" ).getProperty( "rate" ).getStringValue(), equalTo( "10" ) );
		assertThat( "Web Page Runner created", project.getProject().getCanvas().getComponentByLabel( "Web Page Runner 1" ).getProperty( "url" ).getStringValue(), equalTo( "win-srvmontest" ) );

		//Finally
		project.delete( true );
	}

	@Test
	public void shouldCreateAConcurrentUsersProject()
	{
		//Given
		ProjectBuilder projectBuilder = BeanInjector.getBean( ProjectBuilderFactory.class ).newInstance();

		//When
		ProjectRef project = projectBuilder.create()
				.components(
						ComponentBuilder.create().type( "Fixed Load" ).concurrent().property( "load", Long.class, 2L ).child(
								ComponentBuilder.create().type( "Web Page Runner" ).property( "url", String.class, "win-srvmontest" ).build()
						).build()
				)
				.build();

		//Then
		assertThat( "Project contains Fixed Load", project.getProject().getCanvas().getComponentByLabel( "Fixed Load 1" ), notNullValue() );
		Terminal sampleCount = project.getProject().getCanvas().getComponentByLabel( "Fixed Load 1" ).getTerminalByName( "Sample Count" );
		assertThat( "Connected as a concurrent-users scenario", sampleCount.getConnections().size(), equalTo( 1 ) );

		//Finally
		project.delete( true );
	}
}
