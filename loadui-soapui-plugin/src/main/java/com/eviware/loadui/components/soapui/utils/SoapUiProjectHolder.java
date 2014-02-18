package com.eviware.loadui.components.soapui.utils;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent;
import com.eviware.loadui.components.soapui.layout.GeneralSettings;
import com.eviware.loadui.components.soapui.layout.SoapUiFilePicker;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class SoapUiProjectHolder
{
	Logger log = LoggerFactory.getLogger( SoapUiProjectHolder.class );

	public static final String TEST_CASE = "testCase";

	// modified from File property to String property in 2.6.5
	protected final com.eviware.loadui.api.property.Property<String> projectFile;
	protected final com.eviware.loadui.api.property.Property<String> testSuite;
	protected final com.eviware.loadui.api.property.Property<String> testCase;

	protected final GeneralSettings settings;
	protected final PropertyChangedListener propertyEventListener;
	protected final ComponentContext context;
	protected final File loaduiProjectDir;
	protected final SoapUiFilePicker.FileResolver fileResolver = new SoapUiFilePicker.FileResolver();

	protected CountDownLatch testCaseLatch = new CountDownLatch( 0 );

	public static SoapUiProjectHolder newInstance( SoapUISamplerComponent component, ComponentContext context,
																  SoapUISamplerComponent.SoapUITestCaseRunner testCaseRunner, GeneralSettings settings,
																  File loaduiProjectDir )
	{
		return new SoapUiProjectHolder( context, settings, component, testCaseRunner, loaduiProjectDir );
	}

	protected SoapUiProjectHolder( ComponentContext context, GeneralSettings settings,
											 SoapUISamplerComponent component,
											 SoapUISamplerComponent.SoapUITestCaseRunner testCaseRunner,
											 File loaduiProjectDir )
	{
		this.settings = settings;
		this.context = context;
		this.loaduiProjectDir = loaduiProjectDir;

		projectFile = createProjectFileProperty( context );

		testSuite = context.createProperty( "testSuite", String.class );
		testCase = context.createProperty( TEST_CASE, String.class );
		this.propertyEventListener = new PropertyChangedListener( component, testCaseRunner );
		context.addEventListener( PropertyEvent.class, propertyEventListener );
		projectFile.getOwner().addEventListener( PropertyEvent.class, propertyEventListener );
	}

	public LayoutComponent buildLayout()
	{
		// headless
		return null;
	}

	private Property<String> createProjectFileProperty( ComponentContext context )
	{
		Property<?> currentProjectFile = context.getProperty( "projectFile" );
		log.info( "Current project file is {}", currentProjectFile );
		String initialProjectFileValue = null;

		// versions pre 2.6.5 kept the projectFile as a File property - here we convert that to the new system
		if( currentProjectFile != null && currentProjectFile.getType().equals( File.class ) )
		{
			log.info( "Converting projectFile property from File to String" );
			context.deleteProperty( "projectFile" );

			if( settings.getUserProjectRelativePathProperty().getValue() )
			{
				Property<?> relativePathProperty = context.getProperty( "projectRelativePath" );
				initialProjectFileValue = ( relativePathProperty != null ? relativePathProperty.getStringValue() : null );
			}
			else
			{
				initialProjectFileValue = currentProjectFile.getValue() == null ? null :
						( ( File )currentProjectFile.getValue() ).getAbsolutePath();
			}
		}

		log.info( "Initial project file will be set to {}", initialProjectFileValue );
		return context.createProperty( "projectFile", String.class, initialProjectFileValue, false );
	}

	@Nullable
	public File getProjectFile()
	{
		if( Iterables.any( Arrays.asList(
				settings.getUserProjectRelativePathProperty(),
				projectFile, projectFile.getValue() ), Predicates.isNull() ) )
			return null;

		if( settings.getUserProjectRelativePathProperty().getValue() )
			return new File( loaduiProjectDir, projectFile.getValue() );
		else
			return new File( projectFile.getValue() );
	}

	public String getProjectFileName()
	{
		return projectFile.getStringValue();
	}

	public void setProjectFile( File projectAbsolutePath )
	{
		String path = ( settings.getUserProjectRelativePathProperty().getValue() ?
				fileResolver.abs2rel( loaduiProjectDir, projectAbsolutePath ) :
				projectAbsolutePath.getAbsolutePath() );
		log.info( "Updating project file property to {}", path );
		projectFile.setValue( new File( path ) );
	}

	public String getTestSuite()
	{
		return testSuite.getValue();
	}

	public void setTestSuite( final String name )
	{
		testSuite.setValue( name );
	}

	public void setTestSuites( final String... testSuites )
	{
		// no op - used only to set GUI combo options
	}

	public void setTestCases( final String... testCases )
	{
		// no op - used only to set GUI combo options
	}

	public String getTestCase()
	{
		try
		{
			testCaseLatch.await();
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
		return testCase.getValue();
	}

	public void setTestCase( final String name )
	{
		testCase.setValue( name );
	}

	public void reset()
	{
		projectFile.setValue( null );
		testCase.setValue( null );
		testSuite.setValue( null );
	}

	public void onComponentRelease()
	{
		context.removeEventListener( PropertyEvent.class, propertyEventListener );
		projectFile.getOwner().removeEventListener( PropertyEvent.class, propertyEventListener );
	}

	private final class PropertyChangedListener implements EventHandler<PropertyEvent>
	{
		private final SoapUISamplerComponent component;
		private final SoapUISamplerComponent.SoapUITestCaseRunner testCaseRunner;

		public PropertyChangedListener( SoapUISamplerComponent component, SoapUISamplerComponent.SoapUITestCaseRunner testCaseRunner )
		{
			this.component = component;
			this.testCaseRunner = testCaseRunner;
		}

		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( event.getEvent() == PropertyEvent.Event.VALUE )
			{
				Property<?> property = event.getProperty();
				if( property == projectFile )
				{
					component.onProjectUpdated( getProjectFile() );
				}
				else if( property == testSuite )
				{
					log.debug( "Reload TestSuite because testSuite changed to " + testSuite.getValue() );
					testCaseRunner.setTestSuite( testSuite.getValue() );
				}
				else if( property == testCase )
				{
					log.debug( "Reload TestCase because testCase changed to " + testCase.getValue() );
					testCaseRunner.setNewTestCase( testCase.getValue() );
				}
			}
		}
	}

}
