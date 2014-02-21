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
package com.eviware.loadui.ui.fx.views.analysis;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.control.fields.ValidatableNode;
import com.eviware.loadui.ui.fx.util.TreeUtils.LabeledKeyValue;
import com.eviware.loadui.ui.fx.views.assertions.LabeledTreeCell;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.eviware.loadui.ui.fx.util.TreeUtils.dummyItem;

public class StatisticTree extends TreeView<Labeled> implements ValidatableNode
{
	public static final String AGENT_TOTAL = "Total";

	protected static final Logger log = LoggerFactory.getLogger( StatisticTree.class );

	private final BooleanProperty isValidProperty = new SimpleBooleanProperty( false );

	// Used to prevent unwanted chain reactions when forcing TreeItems to collapse.
	public final AtomicBoolean isForceCollapsing = new AtomicBoolean( false );

	private final ImmutableCollection<AgentItem> agents;

	public static StatisticTree forHolder( StatisticHolder holder )
	{
		TreeItem<Labeled> root = new TreeItem<Labeled>( holder );
		return new StatisticTree( holder, root );
	}

	private StatisticTree( StatisticHolder holder, TreeItem<Labeled> root )
	{
		super( root );
		setShowRoot( false );

		getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
		getStyleClass().add( "assertable-tree" );

		agents = ImmutableList.copyOf( holder.getCanvas().getProject().getWorkspace().getAgents() );

		addVariablesToTree( holder, root );

		getSelectionModel().selectedItemProperty().addListener( new ChangeListener<TreeItem<Labeled>>()
		{
			@Override
			public void changed( ObservableValue<? extends TreeItem<Labeled>> arg0, TreeItem<Labeled> oldValue,
										TreeItem<Labeled> newValue )
			{
				if( isForceCollapsing.get() )
					return;
				if( newValue == null || newValue.getValue() == null )
				{
					isValidProperty.set( false );
				}
				else
				{
					if( !newValue.isLeaf() )
					{
						newValue.setExpanded( true );
						getSelectionModel().clearSelection();
						isValidProperty.set( false );
					}
					else
					{
						isValidProperty.set( true );
					}
				}
				log.debug( "getSelectionModel().getSelectedItems(): {}", getSelectionModel().getSelectedItems() );
			}
		} );

		setCellFactory( new Callback<TreeView<Labeled>, TreeCell<Labeled>>()
		{
			@Override
			public TreeCell<Labeled> call( TreeView<Labeled> treeView )
			{
                return LabeledTreeCell.newInstance();
			}
		} );
	}

	private void addVariablesToTree( StatisticHolder holder, TreeItem<Labeled> root )
	{
		log.debug( "Adding variables to tree, StatisticHolder: " + holder );
		getTreeCreatorFor( root ).createTree( holder, root );
	}

	private TreeCreator getTreeCreatorFor( TreeItem<Labeled> root )
	{
		Labeled rootValue = root.getValue();
		if( rootValue instanceof ModelItem )
		{
			switch( ( ( ModelItem )rootValue ).getModelItemType() )
			{
				case COMMON_COMPONENT:
				case PROJECT:
				case SCENARIO:
					return new StandardTreeCreator();
				case AGENT:
				case WORKSPACE:
					throw new RuntimeException( "Completely unexpected modelItemType, cannot create StatisticTree: " +
							rootValue );
			}
		}

		return new MonitorTreeCreator();
	}

	private static abstract class TreeCreator
	{
		abstract void createTree( StatisticHolder holder, TreeItem<Labeled> root );

		TreeItem<Labeled> treeNode( Labeled value, TreeItem<Labeled> parent )
		{
			TreeItem<Labeled> treeNode = new TreeItem<>( value );
			parent.getChildren().add( treeNode );
			return treeNode;
		}
	}

	private class StandardTreeCreator extends TreeCreator
	{
		@Override
		public void createTree( StatisticHolder holder, TreeItem<Labeled> root )
		{
			boolean forceAgentStatistics = ( holder instanceof SceneItem );

			for( String variableName : holder.getStatisticVariableNames() )
			{
				StatisticVariable variable = holder.getStatisticVariable( variableName );
				TreeItem<Labeled> rootNode = treeNode( variable, root );
				boolean mayBeInAgents = forceAgentStatistics
						|| !( variable.getStatisticHolder().getCanvas() instanceof ProjectItem );
				createSubItems( variable, rootNode, mayBeInAgents );
			}
		}

		private void createSubItems( StatisticVariable variable, TreeItem<Labeled> variableItem, boolean mayBeInAgents )
		{
			for( String statisticName : variable.getStatisticNames() )
			{
				Statistic<?> statistic = variable.getStatistic( statisticName, StatisticVariable.MAIN_SOURCE );
				TreeItem<Labeled> statisticItem = treeNode( statistic, variableItem );
				if( !agents.isEmpty() && mayBeInAgents )
				{
					statisticItem.getChildren().add( dummyItem( AGENT_TOTAL, StatisticVariable.MAIN_SOURCE ) );
					for( AgentItem agent : agents )
						treeNode( new LabeledKeyValue<>( agent.getLabel(), agent.getLabel() ), statisticItem );
				}
			}
		}
	}

	private class MonitorTreeCreator extends TreeCreator
	{
		@Override
		public void createTree( StatisticHolder holder, TreeItem<Labeled> root )
		{
			for( String variableName : holder.getStatisticVariableNames() )
			{
				StatisticVariable variable = holder.getStatisticVariable( variableName );
				final TreeItem<Labeled> variableItem = treeNode( variable, root );
				createBranches( variable, variableItem );
			}
		}

		private void createBranches( StatisticVariable variable, final TreeItem<Labeled> variableItem )
		{
			for( String statisticName : variable.getStatisticNames() )
			{
				Map<String, TreeItem<Labeled>> itemsBySource = new HashMap<>();
				Map<String, TreeItem<Labeled>> statsByLabel = new HashMap<>();

				for( String source : variable.getSources() )
				{
					Statistic<?> statistic = variable.getStatistic( statisticName, source );
					TreeItem<Labeled> statItem = statsByLabel.get( statistic.getLabel() );
					if( statItem == null )
					{
						statItem = treeNode( statistic, variableItem );
						statsByLabel.put( statistic.getLabel(), statItem );
					}
					itemsBySource.put( source, statItem );
				}

				for( String source : variable.getSources() )
					if( !source.equals( StatisticVariable.MAIN_SOURCE ) )
						treeNode( new LabeledKeyValue<>( source, source ), itemsBySource.get( source ) );
			}
		}

	}

	@Override
	public ReadOnlyBooleanProperty isValidProperty()
	{
		return isValidProperty;
	}

	@Override
	public boolean isValid()
	{
		return isValidProperty.get();
	}

	public List<Selection> getSelections()
	{
		List<TreeItem<Labeled>> selectedItems = getSelectionModel().getSelectedItems();
		List<Selection> selections = new ArrayList<>( selectedItems.size() );
		for( TreeItem<Labeled> item : selectedItems )
		{
			if( item.getChildren().isEmpty() ) // forbid selecting a parent (which is possible in multi-selections)
				selections.add( new Selection( item ) );
		}
		return selections;
	}

}
