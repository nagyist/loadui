package com.eviware.loadui.util.projects;

import com.eviware.loadui.api.component.ComponentCreationException;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.component.categories.GeneratorCategory;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by osten on 1/20/14.
 */
public class ComponentBuilderTest
{
	private ComponentRegistry componentRegistry;
	private ProjectItem project;
	private CanvasItem canvas;

	@Before
	public void setup()
	{

		componentRegistry = mock( ComponentRegistry.class );
		project = mock(ProjectItem.class);

		ComponentDescriptor rate = new ComponentDescriptor( "com.eviware.FixedRate",
				GeneratorCategory.CATEGORY,
				"Fixed Rate",
				"A standard fixed rate generator",
				new File( "path/to/icon.png" ).toURI(),
				"www.loadui.org/components/fixed-rate.html" );


		ComponentDescriptor runner = new ComponentDescriptor( "com.eviware.WebRunner",
				RunnerCategory.CATEGORY,
				"Web Page Runner",
				"A standard web page runner",
				new File( "path/to/icon.png" ).toURI(),
				"www.loadui.org/components/web-page-runner.html" );

		ComponentDescriptor omni = new ComponentDescriptor( "com.eviware.OmniRunner",
				RunnerCategory.CATEGORY,
				"Omnipotent Runner",
				"A bogo-component that does not exist",
				new File( "path/to/icon.png" ).toURI(),
				"www.loadui.org/components/omni-runner.html" );


		when( componentRegistry.findDescriptor( "Fixed Rate" ) ).thenReturn(
				rate );

		when( componentRegistry.findDescriptor( "Web Page Runner" ) ).thenReturn(
				runner );

		canvas = mock( CanvasItem.class );
		when(canvas.getProject()).thenReturn( project );


		OutputTerminal output = mock( OutputTerminal.class );
		InputTerminal input = mock( InputTerminal.class );

		when( canvas.connect( output, input ) ).thenReturn( mock( Connection.class ) );

		ComponentItem mockedRate = mock( ComponentItem.class );
		ComponentItem mockedRunner = mock( ComponentItem.class );

		when( mockedRate.getLabel() ).thenReturn( rate.getLabel() );
		when( mockedRunner.getLabel() ).thenReturn( runner.getLabel() );

		Property property = mock(Property.class);

		when( property.getValue()).thenReturn( "http://05ten.se" );
		when( property.getStringValue()).thenReturn( "http://05ten.se" );
		when( property.getKey() ).thenReturn( "url" );
		when( property.getType()).thenReturn( String.class );

		when( mockedRunner.getProperty( "not-a-valid-property" ) ).thenThrow( new IllegalArgumentException("No such property exists.") );
		when( mockedRunner.getProperty( "url")).thenReturn( property );

		when( project.getCanvas() ).thenReturn( canvas );

		try
		{
			when( project.createComponent( "Fixed Rate 1", rate ) ).thenReturn( mockedRate );
			when( project.createComponent( "Web Page Runner 1", runner ) ).thenReturn( mockedRunner );
			when( project.createComponent( "Omnipotent Runner 1", omni )).thenThrow( new ComponentCreationException( "Cannot find descriptor, does the component exist?" ) );

		}
		catch( ComponentCreationException e )
		{
			System.err.println( "Cannot create component " + e.getComponentType() + ", " + e.getMessage() );
			e.printStackTrace();
		}
	}

	@Test
	public void shouldAllowBuildingExistingComponent()
	{
		ComponentItem component = null;
		try
		{
			component = ComponentBuilder
					.create()
					.project( project )
					.componentRegistry( componentRegistry )
					.type( "Web Page Runner" )
					.build();

		}
		catch( ComponentCreationException e )
		{
			fail( "Cannot create component when it should be able to" );
		}

		assertThat( component.getLabel(), equalToIgnoringCase( "Web Page Runner" ) );
	}

	@Test
	public void shouldRejectTryingToBuildANonExistingComponent()
	{
		try
		{
			ComponentItem component = ComponentBuilder
					.create()
					.project( project )
					.componentRegistry( componentRegistry )
					.type( "Omnipotent Runner" )
					.build();

			fail( "Created the component without throwing exception where it should have" );

		}
		catch( ComponentCreationException e )
		{
			//Do nothing, the test is a success if an exception is caught.
		}
	}

	@Test
	public void shouldChangeExistingProperty(){
		String key = "url";
		String property = "http://05ten.se";
		ComponentItem component = null;
		try{
			component = ComponentBuilder
					 .create()
					 .project( project )
					 .componentRegistry( componentRegistry )
					 .type( "Web Page Runner" )
					 .property( key, String.class, property )
					 .build();

		}catch(ComponentCreationException e){
         fail( "Cannot create component.");
		 }catch(IllegalArgumentException e){
			fail( "Cannot create property of type " + key + ".");
		 }
		 assertThat( component.getProperty( key ).getStringValue(), equalToIgnoringWhiteSpace( property  ) );
	}

	@Test
	public void illegalPropertyShouldThrowIllegalArgumentException(){
		String key = "not-a-valid-property";
		String property = "http://05ten.se";
		ComponentItem component = null;
		try{
			component = ComponentBuilder
					.create()
					.project( project )
					.componentRegistry( componentRegistry )
					.type( "Web Page Runner" )
					.property( key, String.class, property )
					.build();
			fail( "Component was successfully created and did not throw an IllegalArgumentException, that is unintended.");
   	}catch(ComponentCreationException e){
			fail( "Cannot create component, but that is not the expected exception.");
		}catch(IllegalArgumentException e){
			//Do nothing,
		}
	}
}
