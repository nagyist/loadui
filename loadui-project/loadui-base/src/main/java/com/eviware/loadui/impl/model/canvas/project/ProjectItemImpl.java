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
package com.eviware.loadui.impl.model.canvas.project;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.counter.CounterSynchronizer;
import com.eviware.loadui.api.events.*;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.MessageAwaiterFactory;
import com.eviware.loadui.api.messaging.SceneCommunication;
import com.eviware.loadui.api.model.*;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.property.PropertySynchronizer;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.config.*;
import com.eviware.loadui.impl.XmlBeansUtils;
import com.eviware.loadui.impl.counter.AggregatedCounterSupport;
import com.eviware.loadui.impl.model.canvas.CanvasItemImpl;
import com.eviware.loadui.impl.model.canvas.SceneItemImpl;
import com.eviware.loadui.impl.statistics.model.StatisticPagesImpl;
import com.eviware.loadui.impl.summary.ProjectSummaryCreator;
import com.eviware.loadui.impl.summary.SummaryCreator;
import com.eviware.loadui.impl.terminal.ConnectionImpl;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.collections.CollectionEventSupport;
import com.eviware.loadui.util.messaging.BroadcastMessageEndpointImpl;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class ProjectItemImpl extends CanvasItemImpl<ProjectItemConfig> implements ProjectItem
{
	protected static final Logger log = LoggerFactory.getLogger( ProjectItemImpl.class );


	private final WorkspaceItem workspace;
	private final LoaduiProjectDocumentConfig doc;
	private final CollectionEventSupport<Assignment, Void> assignments = new CollectionEventSupport<>( this, ASSIGNMENTS );
	private final AgentListener agentListener;
	private final SceneListener sceneListener = new SceneListener();
	private final WorkspaceListener workspaceListener = new WorkspaceListener();
	private final SceneComponentListener sceneComponentListener = new SceneComponentListener();
	private final Map<SceneItem, BroadcastMessageEndpoint> sceneEndpoints = Maps.newHashMap();
	private final AgentReadyAwaiterTask agentAwaiter = new AgentReadyAwaiterTask( this );
	private final ConversionService conversionService;
	private final PropertySynchronizer propertySynchronizer;
	private final CounterSynchronizer counterSynchronizer;
	private final Set<SceneItemImpl> scenes = Sets.newHashSet();
	private final StatisticPagesImpl statisticPages;
	private final Property<Boolean> saveReport;
	private final Property<String> reportFolder;
	private final Property<String> reportFormat;

	private final File projectFile;

	public static ProjectItemImpl loadProject( WorkspaceItem workspace, File projectFile ) throws XmlException,
			IOException
	{
		ProjectItemImpl project = new ProjectItemImpl( workspace, projectFile,
				projectFile.exists() ? LoaduiProjectDocumentConfig.Factory.parse( projectFile )
						: LoaduiProjectDocumentConfig.Factory.newInstance() );
		project.init();
		project.postInit();
		return project;
	}

	private ProjectItemImpl( WorkspaceItem workspace, File projectFile, LoaduiProjectDocumentConfig doc )
	{
		super( doc.getLoaduiProject(), new AggregatedCounterSupport() );
		this.doc = doc;
		this.projectFile = projectFile;
		this.workspace = workspace;
		conversionService = BeanInjector.getBean( ConversionService.class );
		propertySynchronizer = BeanInjector.getBean( PropertySynchronizer.class );
		counterSynchronizer = BeanInjector.getBean( CounterSynchronizer.class );
		saveReport = createProperty( SAVE_REPORT_PROPERTY, Boolean.class, false );
		reportFolder = createProperty( REPORT_FOLDER_PROPERTY, String.class, "" );
		reportFormat = createProperty( REPORT_FORMAT_PROPERTY, String.class, "" );
		statisticPages = new StatisticPagesImpl( getConfig().getStatistics() == null ? getConfig().addNewStatistics()
				: getConfig().getStatistics() );

		agentListener = new AgentListener( this, conversionService, BeanInjector.getBean( MessageAwaiterFactory.class ) );

		BeanInjector.getBean( TestRunner.class ).registerTask( agentAwaiter, Phase.PRE_START );
	}

	@Override
	protected void init()
	{
		loadScenes();

		super.init();

		workspace.addEventListener( BaseEvent.class, workspaceListener );

		loadAgents();

		for( SceneAssignmentConfig conf : getConfig().getSceneAssignmentList() )
		{
			SceneItem scene = ( SceneItem )addressableRegistry.lookup( conf.getSceneRef() );
			AgentItem agent = ( AgentItem )addressableRegistry.lookup( conf.getAgentRef() );
			if( scene == null )
			{
				break;
			}

			if( agent == null && conf.isSetAgentAddress() )
			{
				for( AgentItem r : workspace.getAgents() )
				{
					if( r.getUrl().equals( conf.getAgentAddress() ) )
					{
						agent = r;
						break;
					}
				}
				if( agent == null && conf.isSetAgentLabel()
						&& ( Boolean )workspace.getProperty( WorkspaceItem.IMPORT_MISSING_AGENTS_PROPERTY ).getValue() )
				{
					agent = workspace.createAgent( conf.getAgentAddress(), conf.getAgentLabel() );
				}
			}

			if( agent != null )
			{
				sceneEndpoints.get( scene ).registerEndpoint( agent );
				if( assignments.addItem( getOrCreateAssignment( scene, agent ) ) )
				{
					sendAssignMessage( agent, scene );
				}
			}
		}
	}

	private void loadAgents()
	{
		for( AgentItem agent : workspace.getAgents() )
			agentListener.attach( agent );
	}

	private void loadScenes()
	{
		for( SceneItemConfig conf : getConfig().getSceneList() )
		{
			attachScene( SceneItemImpl.newInstance( this, conf ) );
		}
	}

	@Override
	protected void postInit()
	{
		super.postInit();
		statisticPages.init();
	}

	void sendAssignMessage( AgentItem agent, SceneItem scene )
	{
		agent.sendMessage( AgentItem.AGENT_CHANNEL,
				ImmutableMap.<String, String>of( AgentItem.ASSIGN, scene.getId(), AgentItem.PROJECT_ID, getId() ) );
	}

	private boolean attachScene( SceneItemImpl scene )
	{
		if( scenes.add( scene ) )
		{
			scene.addEventListener( BaseEvent.class, sceneListener );
			( ( AggregatedCounterSupport )counterSupport ).addChild( scene );
			sceneEndpoints.put( scene, new BroadcastMessageEndpointImpl() );
			BroadcastMessageEndpoint sceneEndpoint = sceneEndpoints.get( scene );
			propertySynchronizer.syncProperties( scene, sceneEndpoint );
			counterSynchronizer.syncCounters( scene, sceneEndpoint );
			for( ComponentItem component : scene.getComponents() )
			{
				propertySynchronizer.syncProperties( component, sceneEndpoint );
				counterSynchronizer.syncCounters( component, sceneEndpoint );
				component.addEventListener( RemoteActionEvent.class, sceneComponentListener );
			}
			return true;
		}
		return false;
	}

	private void detachScene( SceneItemImpl scene )
	{
		if( scenes.remove( scene ) )
		{
			log.debug( "Detaching {}", scene );
			scene.removeEventListener( BaseEvent.class, sceneListener );

			( ( AggregatedCounterSupport )counterSupport ).removeChild( scene );

			for( AgentItem agent : getAgentsAssignedTo( scene ) )
			{
				log.debug( "Telling {} to stop scene {}", agent, scene );
				agent.sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.UNASSIGN, scene.getId() ) );
			}

			sceneEndpoints.remove( scene );
			fireCollectionEvent( SCENES, CollectionEvent.Event.REMOVED, scene );
		}
	}

	@Nonnull
	@Override
	public WorkspaceItem getWorkspace()
	{
		return workspace;
	}

	@Nonnull
	@Override
	public ProjectItem getProject()
	{
		return this;
	}

	@Override
	public SceneItem getSceneByLabel( @Nonnull String label )
	{
		for( SceneItem scene : scenes )
			if( scene.getLabel().equals( label ) )
				return scene;

		return null;
	}

	@Nonnull
	@Override
	public SceneItem createScene( @Nonnull String label )
	{
		SceneItemConfig sceneConfig = getConfig().addNewScene();
		sceneConfig.setLabel( label );

		SceneItemImpl scene = SceneItemImpl.newInstance( this, sceneConfig );
		if( attachScene( scene ) )
			fireCollectionEvent( SCENES, CollectionEvent.Event.ADDED, scene );
		return scene;
	}

	@Override
	protected Connection createConnection( OutputTerminal output, InputTerminal input )
	{
		return new ConnectionImpl( getConfig().addNewConnection(), output, input );
	}

	@Override
	public File getProjectFile()
	{
		return projectFile;
	}

	@Override
	public void save()
	{
		try
		{
			log.info( "Saving Project {}...", getLabel() );

			if( !projectFile.exists() )
				if( !projectFile.createNewFile() )
					throw new RuntimeException( "Unable to create project file: " + projectFile.getAbsolutePath() );

			XmlBeansUtils.saveToFile( doc, projectFile );
			markClean();
		}
		catch( IOException e )
		{
			log.error( "Unable to save project: " + getLabel(), e );
		}
	}

	@Override
	public void saveAs( @Nonnull File saveAsFile )
	{
		try
		{
			log.info( "Saving Project {}...", getLabel() );

			if( !saveAsFile.exists() )
				if( !saveAsFile.createNewFile() )
					throw new RuntimeException( "Unable to create project file: " + projectFile.getAbsolutePath() );

			XmlBeansUtils.saveToFile( doc, saveAsFile );
		}
		catch( IOException e )
		{
			log.error( "Unable to save project: " + getLabel() + " to " + saveAsFile.getName(), e );
		}
	}

	@Override
	public void release()
	{
		BeanInjector.getBean( TestRunner.class ).unregisterTask( agentAwaiter, Phase.PRE_START );
		getWorkspace().removeEventListener( BaseEvent.class, workspaceListener );
		ReleasableUtils.releaseAll( agentListener, statisticPages, getChildren() );

		super.release();
	}

	@Override
	public void delete()
	{
		for( SceneItem scene : getChildren() )
			scene.delete();

		if( !projectFile.delete() )
			throw new RuntimeException( "Unable to delete project file: " + projectFile.getAbsolutePath() );

		super.delete();

		release();
	}

	@Override
	public void markClean()
	{
		super.markClean();
		for( SceneItemImpl scene : scenes )
		{
			scene.markClean();
		}
	}

	@Override
	public void assignScene( @Nonnull SceneItem scene, @Nonnull AgentItem agent )
	{
		AssignmentImpl assignment = getOrCreateAssignment( scene, agent );

		if( assignments.addItem( assignment ) )
		{
			sceneEndpoints.get( scene ).registerEndpoint( agent );
			SceneAssignmentConfig conf = getConfig().addNewSceneAssignment();
			String sceneId = scene.getId();
			conf.setSceneRef( sceneId );
			conf.setAgentRef( agent.getId() );
			conf.setAgentLabel( agent.getLabel() );
			conf.setAgentAddress( agent.getUrl() );

			sendAssignMessage( agent, scene );
		}
	}

	@Nonnull
	@Override
	public Collection<AgentItem> getAgentsAssignedTo( SceneItem scene )
	{
		Set<AgentItem> agents = new HashSet<>();
		for( Assignment assignment : assignments.getItems() )
			if( scene.equals( assignment.getScene() ) )
				agents.add( assignment.getAgent() );
		return agents;
	}

	@Nonnull
	@Override
	public Collection<SceneItem> getScenesAssignedTo( @Nonnull AgentItem agent )
	{
		Set<SceneItem> scenesOnAgent = new HashSet<>();
		for( Assignment assignment : assignments.getItems() )
			if( agent.equals( assignment.getAgent() ) )
				scenesOnAgent.add( assignment.getScene() );
		return scenesOnAgent;
	}

	@Nonnull
	@Override
	public Collection<Assignment> getAssignments()
	{
		return Collections.unmodifiableCollection( assignments.getItems() );
	}

	@Override
	public void unassignScene( @Nonnull SceneItem scene, @Nonnull AgentItem agent )
	{
		AssignmentImpl assignment = getAssignment( scene, agent );
		if( assignment != null )
		{
			assignments.removeItem( assignment );
			BroadcastMessageEndpoint bme = sceneEndpoints.get( scene );
			if( bme != null )
			{
				bme.deregisterEndpoint( agent );
				agent.sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap( AgentItem.UNASSIGN, scene.getId() ) );
			}

			int size = getConfig().sizeOfSceneAssignmentArray();
			for( int i = 0; i < size; i++ )
			{
				SceneAssignmentConfig conf = getConfig().getSceneAssignmentArray( i );
				if( scene.getId().equals( conf.getSceneRef() ) && agent.getId().equals( conf.getAgentRef() ) )
				{
					getConfig().removeSceneAssignment( i );
					break;
				}
			}
		}
	}

	AddressableRegistry getRegistry()
	{
		return addressableRegistry;
	}

	private void sendSceneCommand( SceneItem scene, String... args )
	{
		List<String> message = new ArrayList<>();
		message.add( scene.getId() );
		message.add( Long.toString( scene.getVersion() ) );
		Collections.addAll( message, args );

		broadcastMessage( scene, SceneCommunication.CHANNEL, message.toArray() );
	}

	AssignmentImpl getAssignment( SceneItem scene, AgentItem agent )
	{
		for( Assignment assignment : assignments.getItems() )
			if( assignment.getScene() == scene && assignment.getAgent() == agent )
				return ( AssignmentImpl )assignment;

		return null;
	}

	private AssignmentImpl getOrCreateAssignment( SceneItem scene, AgentItem agent )
	{
		AssignmentImpl assignment = getAssignment( scene, agent );
		return assignment != null ? assignment : new AssignmentImpl( scene, agent );
	}

	@Override
	public void broadcastMessage( @Nonnull SceneItem scene, @Nonnull String channel, Object data )
	{
		// log.debug( "BROADCASTING: " + scene + " " + channel + " " + data );
		sceneEndpoints.get( scene ).sendMessage( channel, data );
	}

	@Override
	public void onComplete( EventFirer source )
	{
		Collection<? extends SceneItemImpl> children = getChildren();
		// At this point all project components are finished. They are either
		// canceled or finished normally which depends on project 'abortOnFinish'
		// property. So here we have to wait for all test cases to finish which we
		// do by listening for ON_COMPLETE_DONE event which is fired after
		// 'onComplete' method was called in local mode and after controller
		// received test case data in distributed mode.
		try
		{
			new CanvasCompleteAwaiter( children, scheduler ).startAndWait( 1, TimeUnit.MINUTES );
		}
		catch( Exception e )
		{
			for( CanvasItemImpl child : children )
			{
				child.setCompleted( true );
			}
		}
		finally
		{
			setCompleted( true );
		}
	}

	@Override
	public SummaryCreator getSummaryCreator()
	{
		return new ProjectSummaryCreator( this );
	}


	@Nonnull
	@Override
	public Collection<? extends SceneItemImpl> getChildren()
	{
		return ImmutableSet.copyOf( scenes );
	}

	@Nonnull
	@Override
	public CanvasObjectItem duplicate( @Nonnull CanvasObjectItem obj )
	{
		if( !( obj instanceof SceneItem ) )
			return super.duplicate( obj );

		if( !( obj instanceof SceneItemImpl ) )
			throw new IllegalArgumentException( obj + " needs to be an instance of: " + SceneItemImpl.class.getName() );

		SceneItemConfig config = getConfig().addNewScene();

		config.set( ( ( SceneItemImpl )obj ).getConfig() );
		config.setLabel( "Copy of " + config.getLabel() );
		Map<String, String> addresses = new HashMap<>();
		addresses.put( config.getId(), addressableRegistry.generateId() );
		for( ComponentItemConfig component : config.getComponentList() )
			addresses.put( component.getId(), addressableRegistry.generateId() );
		String data = config.xmlText();
		for( Entry<String, String> e : addresses.entrySet() )
			data = data.replaceAll( e.getKey(), e.getValue() );
		try
		{
			config.set( XmlObject.Factory.parse( data ) );
		}
		catch( XmlException e )
		{
			throw new RuntimeException( e );
		}

		SceneItemImpl scene = SceneItemImpl.newInstance( this, config );
		if( attachScene( scene ) )
			fireCollectionEvent( SCENES, CollectionEvent.Event.ADDED, scene );

		return scene;
	}

	@Override
	public boolean isSceneLoaded( @Nonnull SceneItem scene, @Nonnull AgentItem agent )
	{
		AssignmentImpl assignment = getAssignment( scene, agent );
		return assignment != null && assignment.loaded;
	}

	@Override
	public boolean isSaveReport()
	{
		return saveReport.getValue();
	}

	@Override
	public Property<Boolean> saveReportProperty()
	{
		return saveReport;
	}

	@Override
	public void setSaveReport( boolean save )
	{
		saveReport.setValue( save );
	}

	@Override
	public String getReportFolder()
	{
		return reportFolder.getValue();
	}

	@Override
	public void setReportFolder( String path )
	{
		reportFolder.setValue( path );
	}

	@Override
	public String getReportFormat()
	{
		return reportFormat.getValue();
	}

	@Override
	public void setReportFormat( String format )
	{
		reportFormat.setValue( format );
	}

	@Override
	public StatisticPages getStatisticPages()
	{
		return statisticPages;
	}

	@Override
	public void setAbortOnFinish( boolean abort )
	{
		// when property changes on project set new value to all test cases
		super.setAbortOnFinish( abort );
		for( SceneItem s : getChildren() )
		{
			s.setAbortOnFinish( abort );
		}
	}

	@Override
	public ModelItemType getModelItemType()
	{
		return ModelItemType.PROJECT;
	}

	class AssignmentImpl implements Assignment
	{
		private final SceneItem scene;
		private final AgentItem agent;
		private boolean loaded = false;

		private AssignmentImpl( SceneItem scene, AgentItem agent )
		{
			this.scene = scene;
			this.agent = agent;
		}

		public void setLoaded( boolean loaded )
		{
			if( this.loaded != loaded )
			{
				this.loaded = loaded;
				fireBaseEvent( SCENE_LOADED );
			}
		}

		@Override
		public AgentItem getAgent()
		{
			return agent;
		}

		@Override
		public SceneItem getScene()
		{
			return scene;
		}
	}

	private class SceneListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event instanceof RemoteActionEvent )
			{
				SceneItem scene = ( SceneItem )event.getSource();
				sendSceneCommand( scene, SceneCommunication.ACTION_EVENT, event.getKey(), scene.getId() );
			}
			else if( event.getSource() instanceof SceneItemImpl )
			{
				SceneItemImpl scene = ( SceneItemImpl )event.getSource();
				if( !scenes.contains( scene ) )
				{
					return;
				}
				if( DELETED.equals( event.getKey() ) )
				{
					for( int i = 0; i < getConfig().sizeOfSceneArray(); i++ )
					{
						if( scene.getId().equals( getConfig().getSceneArray( i ).getId() ) )
						{
							for( AgentItem agent : getAgentsAssignedTo( scene ) )
								unassignScene( scene, agent );
							getConfig().removeScene( i );
							break;
						}
					}
				}
				else if( RELEASED.equals( event.getKey() ) )
					detachScene( scene );
				else if( LABEL.equals( event.getKey() ) )
					sendSceneCommand( scene, SceneCommunication.LABEL, scene.getLabel() );
				if( event instanceof CollectionEvent )
				{
					CollectionEvent cEvent = ( CollectionEvent )event;
					if( CONNECTIONS.equals( cEvent.getKey() ) )
					{
						Connection connection = ( Connection )cEvent.getElement();
						String command = cEvent.getEvent() == CollectionEvent.Event.ADDED ? SceneCommunication.CONNECT
								: SceneCommunication.DISCONNECT;
						sendSceneCommand( scene, command, connection.getOutputTerminal().getId(), connection
								.getInputTerminal().getId() );
					}
					else if( COMPONENTS.equals( cEvent.getKey() ) )
					{
						ComponentItem component = ( ComponentItem )cEvent.getElement();
						if( cEvent.getEvent() == CollectionEvent.Event.ADDED )
						{
							propertySynchronizer.syncProperties( component, sceneEndpoints.get( scene ) );
							component.addEventListener( RemoteActionEvent.class, sceneComponentListener );
							sendSceneCommand( scene, SceneCommunication.ADD_COMPONENT,
									conversionService.convert( component, String.class ) );
						}
						else
						{
							sendSceneCommand( scene, SceneCommunication.REMOVE_COMPONENT, component.getId() );
						}
					}
				}
			}
		}
	}

	private class WorkspaceListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event instanceof CollectionEvent )
			{
				CollectionEvent cEvent = ( CollectionEvent )event;
				if( WorkspaceItem.AGENTS.equals( event.getKey() ) )
				{
					AgentItem agent = ( AgentItem )cEvent.getElement();
					if( CollectionEvent.Event.ADDED == cEvent.getEvent() )
						agentListener.attach( agent );
					else
						agentListener.detach( agent );
				}
			}
			else if( event instanceof ActionEvent )
			{
				fireEvent( event );
			}
		}
	}

	private class SceneComponentListener implements EventHandler<RemoteActionEvent>
	{
		@Override
		public void handleEvent( RemoteActionEvent event )
		{
			ComponentItem component = ( ComponentItem )event.getSource();
			sendSceneCommand( ( SceneItem )component.getCanvas(), SceneCommunication.ACTION_EVENT, event.getKey(),
					component.getId() );
		}
	}

	@Override
	public boolean isLoadingError()
	{
		for( SceneItem scene : getChildren() )
		{
			if( scene.isLoadingError() )
				return true;
		}
		return super.isLoadingError();
	}

	@Override
	public void cancelScenes( boolean linkedOnly )
	{
		for( SceneItem s : getChildren() )
		{
			if( !linkedOnly || s.isFollowProject() )
			{
				s.cancelComponents();
			}
		}
	}

	@Override
	@Nonnull
	public CanvasItem getCanvas()
	{
		return this;
	}
}
