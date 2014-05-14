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

import java.util.Set;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuButtonBuilder;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.MenuItemsProvider;
import com.eviware.loadui.ui.fx.MenuItemsProvider.HasMenuItems;
import com.eviware.loadui.ui.fx.MenuItemsProvider.Options;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class AssertionView extends VBox implements Deletable
{
	protected static final Logger log = LoggerFactory.getLogger( AssertionView.class );

	private final AssertionItem<?> assertion;
	private final AssertionInspectorView assertionInspView;

	private final Runnable deleteAction = new Runnable()
	{
		@Override
		public void run()
		{
			delete();
		}
	};

	private final Runnable renameAction = new Runnable()
	{

		@Override
		public void run()
		{
			String attemptedName = assertion.getLabel();
			log.debug( "Assertion renamed to '" + attemptedName + "'" );
			Set<String> assertionLabels = Sets.newHashSet( Lists.transform( assertionInspView.getAssertions(),
					new Function<AssertionView, String>()
					{
						@Override
						@Nullable
						public String apply( @Nullable AssertionView view )
						{
							return view == AssertionView.this ? "" : view.assertion.getLabel();
						}
					} ) );
			int append = 1;
			//FIXME the renaming algorithm here will add a new (n) instead of just increasing the current (n) if any
			while( assertionLabels.contains( assertion.getLabel() ) )
				( ( Labeled.Mutable )assertion ).setLabel( assertion.getLabel() + " (" + ( append++ ) + ")" );
			if( !attemptedName.equals( assertion.getLabel() ) )
				log.debug( "Name was already taken, changed to: " + assertion.getLabel() );
		}

	};

	public AssertionView( final AssertionItem<?> assertion, AssertionInspectorView assertionInspView )
	{
		getStyleClass().add( "assertion-view" );
		this.assertion = assertion;
		this.assertionInspView = assertionInspView;

		HasMenuItems hasMenuItems = MenuItemsProvider.createWith( this, assertion, Options.are().delete( deleteAction ) );

		final MenuButton menuButton = MenuButtonBuilder.create().items( hasMenuItems.items() ).build();
		menuButton.textProperty().bind( Properties.forLabel( assertion ) );
		menuButton.textProperty().addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable _ )
			{
				renameAction.run();
			}
		} );

		final ContextMenu ctxMenu = ContextMenuBuilder.create().items( hasMenuItems.items() ).build();

		setOnContextMenuRequested( new EventHandler<ContextMenuEvent>()
		{
			@Override
			public void handle( ContextMenuEvent event )
			{
				// never show contextMenu when on top of the menuButton
				if( !NodeUtils.isMouseOn( menuButton ) )
				{
					MenuItemsProvider.showContextMenu( menuButton, ctxMenu );
					event.consume();
				}
			}
		} );

		final Label failures = new Label( "0" );
		assertion.addEventListener( BaseEvent.class, new com.eviware.loadui.api.events.EventHandler<BaseEvent>()
		{
			@Override
			public void handleEvent( BaseEvent event )
			{
				if( AssertionItem.FAILURE_COUNT.equals( event.getKey() ) )
				{
					Platform.runLater( new Runnable()
					{
						@Override
						public void run()
						{
							failures.setText( Long.toString( assertion.getFailureCount() ) );
							log.debug( "getFailureCount:" + assertion.getFailureCount() );
						}
					} );
				}
			}
		} );

		HBox display = HBoxBuilder.create().spacing( 20 ).build();
		display.getStyleClass().add( "display" );

		final Label constraintType = new Label( assertion.getConstraint().constraintType() );
		final Label constraintValue = new Label( assertion.getConstraint().value() );
		VBox rangeVbox = VBoxBuilder.create().children( constraintType, constraintValue ).build();

		String tolerance = assertion.getToleranceAllowedOccurrences() == 0 ? "-" : assertion
				.getToleranceAllowedOccurrences() + " times / " + assertion.getTolerancePeriod() + " sec";
		final Label toleranceLabel = new Label( tolerance );
		VBox toleranceVbox = VBoxBuilder.create().children( new Label( "Tolerance" ), toleranceLabel ).build();
		VBox failuresVbox = VBoxBuilder.create().children( new Label( "Failures" ), failures ).build();
		display.getChildren().setAll( rangeVbox, toleranceVbox, failuresVbox );

		String holderName = "";
		if( assertion.getParent() instanceof Labeled )
		{
			holderName = ( ( Labeled )assertion.getParent() ).getLabel();
		}
		Label holderLabel = LabelBuilder.create().minWidth( 200.0 ).text( holderName ).build();

		Label assertionLegend = LabelBuilder.create().text( String.valueOf( assertion.getValue() ) )
				.maxWidth( Double.MAX_VALUE ).build();

		HBox contentPane = HBoxBuilder.create().children( holderLabel, assertionLegend, display )
				.styleClass( "content-pane" ).spacing( 30 ).build();
		HBox.setHgrow( assertionLegend, Priority.ALWAYS );

		getChildren().setAll( menuButton, contentPane );
	}

	@Override
	public void delete()
	{
		log.debug( "Deleting Assertion" );
		//TODO may need to release resources
	}

}
