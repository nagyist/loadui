/* 
 * Copyright 2010 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.fx.statistics.chart;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Stack;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollView;
import javafx.scene.control.ScrollBarPolicy;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.util.Math;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.statistics.toolbar.StatisticsToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.ChartToolbarItem;
import com.eviware.loadui.fx.statistics.toolbar.items.ComponentToolbarItem;

import com.eviware.loadui.api.model.Releasable;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.ExecutionListener;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.util.BeanInjector;
import java.util.EventObject;

/**
 * A Page displaying ChartGroupHolders, which allows adding, removing, and reordering of its children.
 *
 * @author dain.nilsson
 */
public class ChartPage extends BaseNode, Resizable, Releasable {
	def listener = new ChartPageListener();
	def executionListener = new ExecutionManagerListener();
	
	def timeline = Timeline {
		repeatCount: Timeline.INDEFINITE
		keyFrames: [
			KeyFrame {
				time: 250ms
				action: function():Void {
					for( holder in innerContent )
						holder.update();
				}
			}
		]
	}
	
	def executionManager = BeanInjector.getBean( ExecutionManager.class ) on replace {
		executionManager.addExecutionListener( executionListener );
		if( executionManager.getState() == ExecutionManager.State.STARTED ) {
			timeline.playFromStart();
		}
	}
	
	public-init var statisticPage:StatisticPage on replace {
		statisticPage.addEventListener( BaseEvent.class, listener );
		innerContent = for( chartGroup in statisticPage.getChildren() ) ChartGroupHolder {
			chartGroup: chartGroup as ChartGroup
			layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.SOMETIMES }
		};
	}
	
	var innerContent:ChartGroupHolder[];
	
	override var layoutInfo = LayoutInfo {
		hfill: true
		vfill: true
		hgrow: Priority.ALWAYS
		vgrow: Priority.ALWAYS
	}
	
	var vbox:VBox;
	def resizable:ScrollView = ScrollView {
		width: bind width
		height: bind height
		hbarPolicy: ScrollBarPolicy.NEVER
		vbarPolicy: ScrollBarPolicy.ALWAYS
		fitToWidth: true
		node: Stack {
			nodeVPos: VPos.TOP
			content: [
				DropBase {
					layoutInfo: LayoutInfo { height: bind Math.max( height, vbox.height ), width: bind width }
				}, vbox = VBox {
					spacing: 5
					padding: Insets { left: 5, top: 5, right: 5, bottom: 25 }
					layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, vgrow: Priority.NEVER, vfill: false }
					content: bind innerContent
				}
			]
		}
	}
	
	override function release():Void {
		timeline.stop();
		executionManager.removeExecutionListener( executionListener );
		for( holder in innerContent )
			holder.release();
	}
	
	override function create():Node {
		resizable
	}
	
	override function getPrefHeight( width:Number ):Number {
		resizable.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Number ):Number {
		resizable.getPrefWidth( height )
	}
}

class ExecutionManagerListener extends ExecutionListener {
	var resetOnStart = false;
   override function executionStarted(state:ExecutionManager.State) {
   	println("---started");
   	if( resetOnStart )
   		for( holder in innerContent )
   			holder.reset();
   	timeline.playFromStart();
   }

	override function executionPaused(state:ExecutionManager.State) {
	   println("---paused");
	   timeline.stop();
	}

	override function executionStopped(state:ExecutionManager.State) {
	    println("---stopped");
	    timeline.stop();
	    resetOnStart = true;
	}
}

class ChartPageListener extends EventHandler {
	override function handleEvent( e:EventObject ):Void {
		def event = e as BaseEvent;
		if( StatisticPage.CHILDREN == event.getKey() ) {
			def cEvent = event as CollectionEvent;
			FxUtils.runInFxThread( function():Void {
				if( cEvent.getEvent() == CollectionEvent.Event.ADDED ) {
					insert ChartGroupHolder {
						chartGroup: cEvent.getElement() as ChartGroup
						layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.SOMETIMES }
					} into innerContent;
				} else {
					for( chartGroupHolder in innerContent [x|x.chartGroup == cEvent.getElement()] ) {
						delete chartGroupHolder from innerContent;
						chartGroupHolder.release();
					}
				}
			} );
		}
	}
}

class DropBase extends BaseNode, Resizable, Droppable {
	def dropBaseNode = Region {
		style: "-fx-background-color: white;"
		width: bind width
		height: bind height
	}
	
	override var accept = function( draggable:Draggable ):Boolean {
		draggable instanceof StatisticsToolbarItem
	}
	
	override var onDrop = function( draggable:Draggable ):Void {
		if( draggable instanceof ChartToolbarItem ) {
			statisticPage.createChartGroup( (draggable as ChartToolbarItem).type, "Chart Group {statisticPage.getChildCount()+1}" )
		} else if( draggable instanceof ComponentToolbarItem ) {
			def sh = (draggable as ComponentToolbarItem).component;
			def chartGroup = statisticPage.createChartGroup( com.eviware.loadui.api.statistics.model.chart.LineChartView.class.getName(), "Chart Group {statisticPage.getChildCount()+1}" );
			chartGroup.createChart( sh );
		}
	}
	
	override function create():Node {
		dropBaseNode
	}
	
	override function getPrefHeight( width:Number ):Number {
		dropBaseNode.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Number ):Number {
		dropBaseNode.getPrefWidth( height )
	}
}