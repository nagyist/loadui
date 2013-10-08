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

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.control.fields.Validatable;
import com.eviware.loadui.ui.fx.util.TreeUtils;
import com.eviware.loadui.util.StringUtils;
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


public class AssertableTree extends TreeView<Labeled> implements Validatable
{
	protected static final Logger log = LoggerFactory.getLogger( AssertableTree.class );

	public static BooleanProperty isValidProperty = new SimpleBooleanProperty( false );

	// Used to prevent unwanted chain reactions when forcing TreeItems to collapse.
	public final AtomicBoolean isForceCollapsing = new AtomicBoolean( false );

	public static AssertableTree forHolder( StatisticHolder holder )
	{
		TreeItem<Labeled> treeRoot = new TreeItem<Labeled>( holder );
		return new AssertableTree( holder, treeRoot );
	}

	AssertableTree( StatisticHolder holder, TreeItem<Labeled> root )
	{
		super( root );

		setShowRoot( false );
		getSelectionModel().setSelectionMode( SelectionMode.SINGLE );
		getStyleClass().add( "assertable-tree" );


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
		Object selected = getSelectionModel().getSelectedItem().getValue();
		return ( ( TreeUtils.LabeledKeyValue<String, StatisticWrapper<Number>> )selected ).getValue();
	}

	private void addVariablesToTree( StatisticHolder holder, TreeItem<Labeled> root )
	{
		log.debug( "Adding variables to tree, StatisticHolder: " + holder );
		TreeCreator creator = ( root.getValue() instanceof CanvasItem || root.getValue() instanceof ComponentItem ) ? new AssertableComponentTreeCreator()
				: new AssertableMonitorTreeCreator();
		creator.createTree( holder, root );
	}

	private abstract class TreeCreator
	{
		abstract void createTree( StatisticHolder holder, TreeItem<Labeled> root );

		TreeItem<Labeled> treeNode( Labeled value, TreeItem<Labeled> parent )
		{
			TreeItem<Labeled> treeNode = new TreeItem<>( value );

			parent.getChildren().add( treeNode );
			return treeNode;
		}
	}

	private class AssertableComponentTreeCreator extends TreeCreator
	{
		@Override
		public void createTree( StatisticHolder holder, TreeItem<Labeled> root )
		{
			for( String variableName : holder.getStatisticVariableNames() )
			{
				StatisticVariable variable = holder.getStatisticVariable( variableName );
				TreeItem<Labeled> rootNode = treeNode( variable, root );
				createSubItems( variable, rootNode );
			}
		}

		private void createSubItems( StatisticVariable variable, TreeItem<Labeled> parent )
		{
			for( String statisticName : variable.getStatisticNames() )
			{
				Statistic statistic = variable.getStatistic( statisticName, StatisticVariable.MAIN_SOURCE );
				String statisticLabel = StringUtils.capitalizeEachWord( StringUtils.replaceUnderscoreWithSpace( statistic.getLabel() ) );

				TreeUtils.LabeledKeyValue<String, StatisticWrapper> assertable =
						new TreeUtils.LabeledKeyValue<String, StatisticWrapper>(
								statisticLabel,
								new StatisticWrapper( statistic )
						);

				treeNode( assertable, parent );
			}
		}
	}

	private class AssertableMonitorTreeCreator extends TreeCreator
	{
		@Override
		public void createTree( StatisticHolder holder, TreeItem<Labeled> root )
		{

			for( String variableName : holder.getStatisticVariableNames() )
			{
				StatisticVariable variable = holder.getStatisticVariable( variableName );
				final TreeItem<Labeled> rootNode = treeNode( variable, root );
				createBranches( variable, rootNode );
			}

		}

		private void createBranches( StatisticVariable variable, final TreeItem<Labeled> parent )
		{
			for( String statisticName : variable.getStatisticNames() )
			{
				Map<String, TreeItem<Labeled>> branchBySource = new HashMap<>();
				Map<String, TreeItem<Labeled>> branchByLabel = new HashMap<>();

				for( String source : variable.getSources() )
				{
					Statistic statistic = variable.getStatistic( statisticName, source );
					TreeItem<Labeled> assertable = branchByLabel.get( statistic.getLabel() );
					final StatisticWrapper wrapper = new StatisticWrapper( statistic );

					if( assertable == null )
					{
						assertable = treeNode( new TreeUtils.LabeledKeyValue( wrapper.getLabel(), wrapper ), parent );
						branchByLabel.put( statistic.getLabel(), assertable );
					}
					branchBySource.put( source, assertable );

					if( !source.equals( StatisticVariable.MAIN_SOURCE ) )
					{
						// creating leafs
						treeNode( new TreeUtils.LabeledKeyValue( source, wrapper ), assertable );
					}
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
}
