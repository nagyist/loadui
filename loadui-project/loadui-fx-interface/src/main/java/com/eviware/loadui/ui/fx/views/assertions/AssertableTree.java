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
package com.eviware.loadui.ui.fx.views.assertions;

import com.eviware.loadui.api.model.*;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.control.fields.Validatable;
import com.eviware.loadui.ui.fx.util.TreeUtils;
import com.eviware.loadui.util.StringUtils;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.eviware.loadui.ui.fx.util.TreeUtils.dummyItem;

public class AssertableTree extends TreeView<Labeled> implements Validatable
{
	public static final String AGENT_TOTAL = "Total";

	protected static final Logger log = LoggerFactory.getLogger( AssertableTree.class );

	public static BooleanProperty isValidProperty = new SimpleBooleanProperty( false );

	private final ImmutableCollection<AgentItem> agents;

	// Used to prevent unwanted chain reactions when forcing TreeItems to collapse. 
	public final AtomicBoolean isForceCollapsing = new AtomicBoolean( false );

	public static AssertableTree forHolder( StatisticHolder holder )
	{
		TreeItem<Labeled> holderItem = new TreeItem<Labeled>( holder );
		return new AssertableTree( holder, holderItem );
	}

	AssertableTree( StatisticHolder holder, TreeItem<Labeled> root )
	{
		super( root );

		setShowRoot( false );
      getSelectionModel().setSelectionMode( SelectionMode.SINGLE );
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
			}
		} );


		setCellFactory( new Callback<TreeView<Labeled>, TreeCell<Labeled>>()
		{
			@Override
			public TreeCell<Labeled> call( TreeView<Labeled> treeView )
			{
				return new LabeledTreeCell();
			}
		} );
	}

	public StatisticWrapper<Number> getSelectedAssertion()
	{
		TreeUtils.LabeledStringValue<String, StatisticWrapper<Number>> assertable =
				( TreeUtils.LabeledStringValue<String, StatisticWrapper<Number>> )getSelectionModel().getSelectedItem().getValue();

		return assertable.getValue();
	}

	private void addVariablesToTree( StatisticHolder holder, TreeItem<Labeled> root )
	{
		log.debug( "Adding variables to tree, StatisticHolder: " + holder );
		TreeCreator creator = ( root.getValue() instanceof CanvasItem || root.getValue() instanceof ComponentItem ) ? new ComponentTreeCreator()
				: new MonitorTreeCreator();
		creator.createTree( holder, root );
	}


	private static abstract class TreeCreator
	{
		abstract void createTree( StatisticHolder holder, TreeItem<Labeled> root );

		TreeItem<Labeled> treeItem( Labeled value, TreeItem<Labeled> parent )
		{
			TreeItem<Labeled> item = new TreeItem<>( value );
			parent.getChildren().add( item );
			return item;
		}
	}

	private class ComponentTreeCreator extends TreeCreator
	{
		@Override
		public void createTree( StatisticHolder holder, TreeItem<Labeled> root )
		{
			for( String variableName : holder.getStatisticVariableNames() )
			{
				StatisticVariable variable = holder.getStatisticVariable( variableName );
				boolean forceAgentStatistics = holder instanceof SceneItem;
				TreeItem<Labeled> variableItem = treeItem( variable, root );
				boolean mayBeInAgents = forceAgentStatistics
						|| !( variable.getStatisticHolder().getCanvas() instanceof ProjectItem );
				createSubItems( variable, variableItem, mayBeInAgents );
			}
		}

		private void createSubItems( StatisticVariable variable, TreeItem<Labeled> variableItem, boolean mayBeInAgents )
		{
			for( String statisticName : variable.getStatisticNames() )
			{
				Statistic<Number> statistic = ( Statistic<Number> )variable.getStatistic( statisticName, StatisticVariable.MAIN_SOURCE );

				TreeUtils.LabeledStringValue <String, StatisticWrapper<Number>> assertable = new TreeUtils.LabeledStringValue<String, StatisticWrapper<Number>>(
						StringUtils.replaceUnderscoreWithSpace( statistic.getLabel() ),
						new StatisticWrapper<Number>( statistic )
				);

				treeItem( assertable, variableItem );
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
				final TreeItem<Labeled> variableItem = treeItem( variable, root );
				createSubItems( variable, variableItem );
			}

		}

		private void createSubItems( StatisticVariable variable, final TreeItem<Labeled> variableItem )
		{
			for( String statisticName : variable.getStatisticNames() )
			{
				Map<String, TreeItem<Labeled>> statsBySource = new HashMap<>();
				Map<String, TreeItem<Labeled>> statsByLabel = new HashMap<>();

				for( String source : variable.getSources() )
				{
					Statistic<Number> statistic = ( Statistic<Number> )variable.getStatistic( statisticName, source );
					TreeItem<Labeled> statItem = statsByLabel.get( statistic.getLabel() );

					if( statItem == null )
					{
						statItem = treeItem( new StatisticWrapper<Number>( statistic ), variableItem );
						statsByLabel.put( statistic.getLabel(), statItem );
					}
					statsBySource.put( source, statItem );
				}

				for( String source : variable.getSources() )
					if( !source.equals( StatisticVariable.MAIN_SOURCE ) )
					{
						treeItem( new TreeUtils.LabeledStringValue( source, statsBySource.get( source ).getValue() ), statsBySource.get( source ) );
					}
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

	private class ExpandedTreeItemsLimiter implements ChangeListener<Boolean>
	{
		private final TreeItem<Labeled> variableItem;
		private final TreeItem<Labeled> holderItem;

		ExpandedTreeItemsLimiter( TreeItem<Labeled> variableItem, TreeItem<Labeled> holderItem )
		{
			this.variableItem = variableItem;
			this.holderItem = holderItem;
		}

		@Override
		public void changed( ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue )
		{
			if( newValue.booleanValue() )
			{
				for( TreeItem<Labeled> item : holderItem.getChildren() )
				{
					if( item != variableItem )
					{
						isForceCollapsing.set( true );
						item.setExpanded( false );
						isForceCollapsing.set( false );
					}
				}
			}
		}
	}
}
