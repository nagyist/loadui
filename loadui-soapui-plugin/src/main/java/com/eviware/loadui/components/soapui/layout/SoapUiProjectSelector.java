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
package com.eviware.loadui.components.soapui.layout;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.ui.dialog.FilePickerDialogFactory;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent.SoapUITestCaseRunner;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.sun.javafx.Utils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class SoapUiProjectSelector
{
	public static final String TEST_CASE = "testCase";

	private static final Logger log = LoggerFactory.getLogger( SoapUiProjectSelector.class );

	private final com.eviware.loadui.api.property.Property<File> projectFile;
	private final com.eviware.loadui.api.property.Property<String> testSuite;
	private final com.eviware.loadui.api.property.Property<String> testCase;
	private final javafx.beans.property.Property<String> convertedTestSuite;
	private final javafx.beans.property.Property<String> convertedTestCase;
	private final GeneralSettings settings;
	private final PropertyChangedListener propertyEventListener;
	private final ComponentContext context;
	private final File loaduiProjectDir;

	private CountDownLatch testCaseLatch = new CountDownLatch( 0 );

	private final ComboBox<String> testSuiteCombo = ComboBoxBuilder.<String>create().maxHeight( Double.MAX_VALUE )
			.maxWidth( Double.MAX_VALUE ).build();

	private final ComboBox<String> testCaseCombo = ComboBoxBuilder.<String>create().maxHeight( Double.MAX_VALUE )
			.maxWidth( Double.MAX_VALUE ).build();

	public static SoapUiProjectSelector newInstance( SoapUISamplerComponent component, ComponentContext context,
																	 SoapUITestCaseRunner testCaseRunner, GeneralSettings settings,
																	 File loaduiProjectDir )
	{
		return new SoapUiProjectSelector( context, settings, component, testCaseRunner, loaduiProjectDir );
	}

	private SoapUiProjectSelector( ComponentContext context, GeneralSettings settings,
											 SoapUISamplerComponent component,
											 SoapUITestCaseRunner testCaseRunner,
											 File loaduiProjectDir )
	{
		this.settings = settings;
		this.context = context;
		this.loaduiProjectDir = loaduiProjectDir;
		projectFile = context.createProperty( "projectFile", File.class, null, false );
		testSuite = context.createProperty( "testSuite", String.class );
		testCase = context.createProperty( TEST_CASE, String.class );
		convertedTestSuite = Properties.convert( testSuite );
		convertedTestCase = Properties.convert( testCase );
		this.propertyEventListener = new PropertyChangedListener( component, testCaseRunner );
		context.addEventListener( PropertyEvent.class, propertyEventListener );
		projectFile.getOwner().addEventListener( PropertyEvent.class, propertyEventListener );
	}

	public LayoutComponent buildLayout()
	{
		return new LayoutComponentImpl( ImmutableMap.<String, Object>builder().put( "component", buildNode() )
				.put( LayoutComponentImpl.CONSTRAINTS, "center, w 270!" ) //
				.build() );
	}

	public Node buildNode()
	{
		SelectionModelUtils.writableSelectedItemProperty( testSuiteCombo.getSelectionModel(), true ).bindBidirectional(
				convertedTestSuite );
		SelectionModelUtils.writableSelectedItemProperty( testCaseCombo.getSelectionModel(), true ).bindBidirectional(
				convertedTestCase );

		GridPane grid = GridPaneBuilder.create().rowConstraints( new RowConstraints( 18 ) )
				.columnConstraints( new ColumnConstraints( 70, 70, 70 ) ).hgap( 28 ).build();

		final MenuButton menuButton = MenuButtonBuilder.create().text( "Project" ).build();
		menuButton.setOnMouseClicked( new javafx.event.EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent arg0 )
			{
				new ProjectSelector( menuButton ).display();
			}
		} );

		if( !LoadUI.isHeadless() )
		{
			BeanInjector.getBean( Stage.class ).getScene().getStylesheets()
					.add( SoapUiProjectSelector.class.getResource( "loadui-soapui-plugin-style.css" ).toExternalForm() );
		}

		grid.add( menuButton, 0, 0 );
		grid.add( new Label( "TestSuite" ), 1, 0 );
		grid.add( new Label( "TestCase" ), 2, 0 );

		final Label projectLabel = new Label();
		updateProjectLabel( projectLabel );
		projectFile.getOwner().addEventListener( PropertyEvent.class, new EventHandler<PropertyEvent>()
		{
			@Override
			public void handleEvent( final PropertyEvent event )
			{
				if( event.getProperty() == projectFile )
					updateProjectLabel( projectLabel );
			}
		} );
		final Label testSuiteLabel = LabelBuilder.create().build();

		testSuiteLabel.textProperty().bind( convertedTestSuite );
		final Label testCaseLabel = new Label();
		testCaseLabel.textProperty().bind( convertedTestCase );

		VBox projectVBox = VBoxBuilder
				.create()
				.minWidth( 140 )
				.minHeight( 18 )
				.children( menuButton, projectLabel )
				.build();

		VBox testSuiteVBox = VBoxBuilder
				.create()
				.minWidth( 140 )
				.minHeight( 18 )
				.children( new Label( "TestSuite" ), testSuiteLabel )
				.build();

		VBox testCaseVBox = VBoxBuilder
				.create()
				.minWidth( 140 )
				.minHeight( 18 )
				.children( new Label( "TestCase" ), testCaseLabel )
				.build();

		return HBoxBuilder.create().spacing( 28 ).minWidth( 320 ).children( projectVBox, testSuiteVBox, testCaseVBox )
				.build();
	}

	protected void updateProjectLabel( final Label projectLabel )
	{
		if( LoadUI.isHeadless() )
			return;

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				projectLabel.setText( projectFile.getValue() == null ? "" : projectFile.getValue().getName()
						.replaceFirst( ".xml$", "" ) );
			}
		} );
	}

	public File getProjectFile()
	{
		return projectFile.getValue();
	}

	public String getProjectFileName()
	{
		return projectFile.getStringValue();
	}

	public void setProjectFile( File project )
	{
		projectFile.setValue( project );
	}

	public String getTestSuite()
	{
		return testSuite.getValue();
	}

	public void setTestSuite( final String name )
	{
		testSuite.setValue( name );
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

	public void setTestSuites( final String... testSuites )
	{
		if( LoadUI.isHeadless() )
			return;

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				testSuiteCombo.setItems( FXCollections.observableArrayList( testSuites ) );
			}
		} );
	}

	public void setTestCases( final String... testCases )
	{
		if( LoadUI.isHeadless() )
			return;

		if( testCases.length > 0 )
		{
			testCaseLatch = new CountDownLatch( 1 );
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					testCaseCombo.setItems( FXCollections.observableArrayList( testCases ) );
					testCase.setValue( findSelection( testCases ) );
					testCaseLatch.countDown();
				}
			} );
		}
	}

	private String findSelection( String[] testCases )
	{
		if( testCase.getValue() == null )
			return testCases[0];

		int selector = 0;
		for( String test : testCases )
		{
			if( testCase.getValue().equals( test ) )
				break;
			selector++;
		}

		if( selector < testCases.length )
			return testCases[selector];
		else
			return testCases[0];
	}

	private class ProjectSelector extends PopupControl
	{
		private final Parent parent;
		final javafx.beans.property.Property<File> fileProperty = Properties.convert( projectFile );

		/**
		 * This is created every time the user clicks on the project button - so cleanup is needed after hiding it
		 *
		 * @param parent
		 */
		private ProjectSelector( Parent parent )
		{
			this.parent = parent;
			Preconditions.checkNotNull( loaduiProjectDir, "LoadUI Project Directory must not be null" );

			setStyle( "-fx-border-radius: 3;" );
			setAutoHide( true );

			final SoapUiFilePicker picker = new SoapUiFilePicker( "Select SoapUI project",
					"SoapUI Project Files", "*.xml",
					BeanInjector.getBean( FilePickerDialogFactory.class ),
					loaduiProjectDir.getAbsoluteFile() );
			picker.selectedProperty().bindBidirectional( fileProperty );
			picker.getIsRelativePathProperty().bindBidirectional(
					Properties.convert( settings.getUserProjectRelativePathProperty() ) );

			setOnHidden( new javafx.event.EventHandler<WindowEvent>()
			{
				@Override
				public void handle( WindowEvent windowEvent )
				{
					picker.onHide();
					picker.selectedProperty().unbindBidirectional( fileProperty );
					//FIXME might need to remove these listeners elsewhere
					//context.removeEventListener( PropertyEvent.class, propertyEventListener );
					//projectFile.getOwner().removeEventListener( PropertyEvent.class, propertyEventListener );
				}
			} );

			Button closeButton = new Button( "Close" );
			closeButton.setId( "close-soapui-project-selector" );
			closeButton.setOnAction( new javafx.event.EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent actionEvent )
				{
					hide();
				}
			} );

			HBox buttonBox = HBoxBuilder.create()
					.alignment( Pos.BOTTOM_RIGHT )
					.minWidth( 300 )
					.children( closeButton )
					.build();

			VBox mainBox = VBoxBuilder
					.create()
					.styleClass( "project-selector" )
					.fillWidth( true )
					.prefWidth( 625 )
					.spacing( 10 )
					.padding( new Insets( 10 ) )
					.style( "-fx-background-color: #f4f4f4;" +
							" -fx-border-style: solid;" +
							" -fx-border-color: black;" +
							" -fx-border-radius: 3;" )
					.children( new Label( "SoapUI Project" ), picker,
							new Label( "TestSuite" ), testSuiteCombo,
							new Label( "TestCase" ), testCaseCombo,
							buttonBox )
					.build();

			bridge.getChildren().setAll( StackPaneBuilder.create().children( mainBox ).build() );
		}

		public void display()
		{
			Point2D point = Utils.pointRelativeTo( parent, 0, 0, HPos.LEFT, VPos.TOP, false );
			show( parent, point.getX(), point.getY() );
		}
	}

	private final class PropertyChangedListener implements EventHandler<PropertyEvent>
	{
		private final SoapUISamplerComponent component;
		private final SoapUITestCaseRunner testCaseRunner;

		public PropertyChangedListener( SoapUISamplerComponent component, SoapUITestCaseRunner testCaseRunner )
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
					component.onProjectUpdated( projectFile.getValue() );
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
