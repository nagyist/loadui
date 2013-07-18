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
package com.eviware.loadui.ui.fx.views.syslog;

import com.eviware.loadui.ui.fx.api.Inspector;
import javafx.beans.property.ReadOnlyProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.AnchorPaneBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Pane;

public class SystemLogInspector implements Inspector
{
	private final AnchorPane panel;
	private final SystemLogView systemLog = new SystemLogView();

	public SystemLogInspector()
	{
		Button copyButton = ButtonBuilder.create().text( "Copy All" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent actionEvent )
			{
				systemLog.copyAllRowsToClipboard();
			}
		} ).build();
		Button clearButton = ButtonBuilder.create().text( "Clear" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent actionEvent )
			{
				systemLog.clear();
			}
		} ).build();
		Pane buttonPane = HBoxBuilder.create().children( copyButton, clearButton ).spacing( 6 ).build();
		panel = AnchorPaneBuilder.create().padding( new Insets( 9 ) ).styleClass( "inspector" ).children( systemLog, buttonPane )
				.build();
		AnchorPane.setTopAnchor( systemLog, 0.0 );
		AnchorPane.setRightAnchor( systemLog, 0.0 );
		AnchorPane.setBottomAnchor( systemLog, 31.0 );
		AnchorPane.setLeftAnchor( systemLog, 0.0 );
		AnchorPane.setBottomAnchor( buttonPane, 0.0 );
		AnchorPane.setRightAnchor( buttonPane, 0.0 );
	}

	@Override
	public void initialize( ReadOnlyProperty<Scene> sceneProperty )
	{
		systemLog.initialize();
	}

	@Override
	public String getName()
	{
		return "System Log";
	}

	@Override
	public String getPerspectiveRegex()
	{
		return null;
	}

	@Override
	public Node getPanel()
	{
		return panel;
	}

	@Override
	public void onShow()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onHide()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getHelpUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
