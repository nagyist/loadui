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
package com.eviware.loadui.ui.fx.views.workspace;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.model.*;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.ui.fx.MenuItemsProvider;
import com.eviware.loadui.ui.fx.MenuItemsProvider.Options;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.Carousel;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.filechooser.LoadUIFileChooser;
import com.eviware.loadui.ui.fx.filechooser.LoadUIFileChooserBuilder;
import com.eviware.loadui.ui.fx.util.*;
import com.eviware.loadui.ui.fx.views.projectref.ProjectRefView;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.eviware.loadui.util.projects.ComponentBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static com.eviware.loadui.ui.fx.util.ObservableLists.bindSorted;
import static javafx.beans.binding.Bindings.bindContent;

public class WorkspaceView extends StackPane
{
	public static final String CREATE_PROJECT = "Create project";

	protected static final Logger log = LoggerFactory.getLogger( WorkspaceView.class );

	private static final ExtensionFilter XML_EXTENSION_FILTER = new FileChooser.ExtensionFilter( "LoadUI project file",
			"*.xml" );
	private static final String HELPER_PAGE_URL = "http://www.loadui.org/Working-with-loadUI/workspace-overview.html";
	private static final File PROP_FILE = LoadUI.relativeFile( "res/application.properties" );

	private final WorkspaceItem workspace;

	private final ObservableList<ProjectRef> projectRefList;
	private final ObservableList<ProjectRefView> projectRefViews;

	private ProjectBuilderFactory projectProvider;

	@FXML
	@SuppressWarnings( "unused" )
	private VBox carouselArea;

	@FXML
	@SuppressWarnings( "unused" )
	private ToolBox<Label> toolbox;

	@FXML
	@SuppressWarnings( "unused" )
	private Carousel<ProjectRefView> projectRefCarousel;

	@FXML
	@SuppressWarnings( "unused" )
	private TextField projectNameField;


	@FXML
	@SuppressWarnings( "unused" )
	private WebView webView;
	private ObservableList<ReadOnlyStringProperty> labelProperties;

	public WorkspaceView( final WorkspaceItem workspace )
	{
		projectProvider = BeanInjector.getBean( ProjectBuilderFactory.class );


		this.workspace = workspace;
		projectRefList = ObservableLists.fx( ObservableLists.ofCollection( workspace, WorkspaceItem.PROJECT_REFS,
				ProjectRef.class, workspace.getProjectRefs() ) );

		projectRefViews = ObservableLists.transform( projectRefList, new Function<ProjectRef, ProjectRefView>()
		{
			@Override
			public ProjectRefView apply( ProjectRef projectRef )
			{
				return new ProjectRefView( projectRef );
			}
		} );

		FXMLUtils.load( this );
	}

	@FXML
	protected void initialize()
	{
		addEventHandler( IntentEvent.ANY, new EventHandler<IntentEvent<?>>()
		{
			@Override
			public void handle( IntentEvent<?> event )
			{
				if( event.getEventType() == IntentEvent.INTENT_CLONE && event.getArg() instanceof ProjectRef )
				{
					final ProjectRef projectRef = ( ProjectRef )event.getArg();
					new CloneProjectDialog( workspace, projectRef, WorkspaceView.this ).show();
					event.consume();
				}
				else if( event.getEventType() == IntentEvent.INTENT_CREATE )
				{
					if( event.getArg() == ProjectItem.class )
					{
						new CreateNewProjectDialog( workspace, WorkspaceView.this ).show();
						event.consume();
					}
				}
				else if( event.getEventType() == IntentEvent.INTENT_OPEN && event.getArg() instanceof ProjectRef )
				{
					workspace.setAttribute( "lastOpenProject", ( ( ProjectRef )event.getArg() ).getProjectFile()
							.getAbsolutePath() );
				}
			}
		} );

		initProjectRefCarousel();

		java.util.Properties props = new java.util.Properties();

		try(InputStream propsStream = Files.newInputStreamSupplier( PROP_FILE ).getInput())
		{
			props.load( propsStream );
		}
		catch( IOException e )
		{
			log.warn( "Unable to load resource file 'application.properties!'", e );
		}

		webView.getEngine().setCreatePopupHandler( new Callback<PopupFeatures, WebEngine>()
		{
			@Override
			public WebEngine call( PopupFeatures pf )
			{
				final WebEngine popupWebEngine = new WebEngine();
				popupWebEngine.locationProperty().addListener( new InvalidationListener()
				{
					@Override
					public void invalidated( Observable _ )
					{
						UIUtils.openInExternalBrowser( popupWebEngine.getLocation() );
					}
				} );
				return popupWebEngine;
			}
		} );
		webView.getEngine().load( props.getProperty( "starter.page.url" ) + "?version=" + LoadUI.version() );

	}

	public ToolBox<Label> getToolbox()
	{
		return toolbox;
	}

	private void initProjectRefCarousel()
	{
		final Observables.Group<ReadOnlyStringProperty> group = Observables.group( new ReadOnlyStringProperty[0] );
		final MenuItem[] carouselMenuItems = MenuItemsProvider.createWith( projectRefCarousel, null,
				Options.are().noDelete().noRename().create( ProjectItem.class, CREATE_PROJECT ) ).items();

		final ContextMenu ctxMenu = ContextMenuBuilder.create().items( carouselMenuItems ).build();

		projectRefCarousel.setOnContextMenuRequested( new EventHandler<ContextMenuEvent>()
		{
			@Override
			public void handle( ContextMenuEvent event )
			{
				boolean hasProject = !projectRefCarousel.getItems().isEmpty();
				if( hasProject && NodeUtils.isMouseOn( projectRefCarousel.getSelected().getMenuButton() ) )
					return; // never show contextMenu when on top of the menuButton

				ctxMenu.getItems().setAll(
						hasProject && NodeUtils.isMouseOn( projectRefCarousel.getSelected() ) ? projectRefCarousel
								.getSelected().getMenuItemProvider().items() : carouselMenuItems );
				MenuItemsProvider.showContextMenu( projectRefCarousel, ctxMenu );
				event.consume();
			}
		} );

		bindSorted( projectRefCarousel.getItems(), projectRefViews, Ordering.usingToString(), group );

		labelProperties = ObservableLists.transform( projectRefCarousel.getItems(),
				new Function<ProjectRefView, ReadOnlyStringProperty>()
				{
					@Override
					public ReadOnlyStringProperty apply( ProjectRefView projectRefView )
					{
						return projectRefView.labelProperty();
					}
				} );

		bindContent( group.getObservables(), labelProperties );

		final String lastProject = workspace.getAttribute( "lastOpenProject", "" );
		projectRefCarousel.setSelected( Iterables.find( projectRefCarousel.getItems(), new Predicate<ProjectRefView>()
		{
			@Override
			public boolean apply( ProjectRefView view )
			{
				return lastProject.equals( view.getProjectRef().getProjectFile().getAbsolutePath() );
			}
		}, Iterables.getFirst( projectRefCarousel.getItems(), null ) ) );

		projectRefCarousel.addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( DraggableEvent event )
			{
				if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED && event.getData() instanceof NewProjectIcon )
				{
					event.accept();
				}
				else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
				{
					fireEvent( IntentEvent.create( IntentEvent.INTENT_CREATE, ProjectItem.class ) );
				}
			}
		} );
	}

	@FXML
	@SuppressWarnings( "unused" )
	public void importProject()
	{

		LoadUIFileChooser fileChooser = LoadUIFileChooserBuilder
				.usingWorkspace( workspace )
				.extensionFilters( XML_EXTENSION_FILTER ).build();
		File file = fileChooser.showOpenDialog( getScene().getWindow() );
		if( file != null )
		{
			fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, new ImportProjectTask( workspace, file ) ) );
		}
	}

	public VBox getCarouselArea()
	{
		return carouselArea;
	}

	public WorkspaceItem getWorkspace()
	{
		return workspace;
	}

	@FXML
	@SuppressWarnings( "unused" )
	public void openHelpPage()
	{
		UIUtils.openInExternalBrowser( HELPER_PAGE_URL );
	}

	@FXML
	@SuppressWarnings( "unused" )
	public void projectBuilder()
	{

		if (projectNameField.getText().isEmpty()){
			projectNameField.setText( "Example Project #" + new Random().nextInt( 200 ) );
		}
		ComponentRegistry registry = BeanInjector.getBean( ComponentRegistry.class );

		final ProjectRef project = projectProvider
				.newInstance()
				.create()
				.assertionLimit( 200L )
				.timeLimit( 500L )
				.requestsLimit( 20000L )
				.label( projectNameField.getText() )
				.build();

		ComponentBuilder.WithProjectAndComponentRegistry generateComponent = ComponentBuilder.create().project( project.getProject() ).componentRegistry( registry );

		ComponentItem runner = generateComponent
				.labeled( "Web Page Runner" )
				.property( "url", String.class, "http://05ten.se" )
				.build();

		ComponentItem rate = generateComponent
				.labeled( "Fixed Rate" )
				.property( "rate", Long.class, 1337L )
				.returnLink( true )
				.child( runner )
				.build();

		projectRefCarousel.setSelected( Iterables.find( projectRefCarousel.getItems(), new Predicate<ProjectRefView>()
		{
			@Override
			public boolean apply( ProjectRefView view )
			{
				return project.getProjectFile().getAbsolutePath().equals( view.getProjectRef().getProjectFile().getAbsolutePath() );
			}
		}, Iterables.getFirst( projectRefCarousel.getItems(), null ) ) );

		/*
		System.out.println("Properties of a Fixed Rate Generator:\n========================================");
		for(Property<?> p : rate.getProperties()){
			System.out.println( "Key: " + p.getKey() + ", Value: " + p.getStringValue() + ", Type: " + p.getType());
		}

		System.out.println("Properties of a Web Page Runner: \n========================================");
		for(Property<?> p : runner.getProperties()){
			System.out.println( "Key: " + p.getKey() + ", Value: " + p.getStringValue() + ", Type: " + p.getType());
		} */
	}

	@FXML
	@SuppressWarnings( "unused" )
	public void gettingStarted()
	{
		new GettingStartedDialog( workspace, WorkspaceView.this ).show();
	}

	public void exit()
	{
		getScene().getWindow().hide();
	}
}
