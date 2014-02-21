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
import com.eviware.loadui.api.ui.dialog.FilePickerDialogFactory;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent.SoapUITestCaseRunner;
import com.eviware.loadui.components.soapui.utils.SoapUiProjectHolder;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.sun.javafx.Utils;
import com.sun.javafx.collections.ObservableListWrapper;
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

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

public class SoapUiProjectSelector extends SoapUiProjectHolder
{
	private final javafx.beans.property.Property<String> convertedTestSuite;
	private final javafx.beans.property.Property<String> convertedTestCase;

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
		super( context, settings, component, testCaseRunner, loaduiProjectDir );
		convertedTestSuite = Properties.convert( testSuite );
		convertedTestCase = Properties.convert( testCase );
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
		menuButton.getStyleClass().add( "soapui-project-menu-button" );
		menuButton.setOnMouseClicked( new javafx.event.EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent _ )
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

	private void updateProjectLabel( final Label projectLabel )
	{
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				String filePath = projectFile.getValue();
				if( filePath == null )
					return;
				String[] parts = filePath.split( Pattern.quote( File.separator ) );
				String lastPart = parts.length > 0 ? parts[parts.length - 1] : filePath;
				int lastDotIndex = lastPart.lastIndexOf( "." );
				lastPart = ( lastDotIndex > 0 ? lastPart.substring( 0, lastDotIndex ) : lastPart );
				projectLabel.setText( lastPart );
			}
		} );
	}

	@Override
	public void setTestSuites( final String... testSuites )
	{
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				testSuiteCombo.setItems( FXCollections.observableArrayList( testSuites ) );
			}
		} );
	}

	@Override
	public void setTestCases( final String... testCases )
	{
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
		else
		{
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{
					testCaseCombo.setItems( new ObservableListWrapper<>( Arrays.<String>asList() ) );
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

		/**
		 * This is created every time the user clicks on the project button - so cleanup is needed after hiding it
		 *
		 * @param parent the parent
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
					loaduiProjectDir.getAbsoluteFile(),
					projectFile, settings.getUserProjectRelativePathProperty() );

			setOnHidden( new javafx.event.EventHandler<WindowEvent>()
			{
				@Override
				public void handle( WindowEvent windowEvent )
				{
					picker.onHide();
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


}
