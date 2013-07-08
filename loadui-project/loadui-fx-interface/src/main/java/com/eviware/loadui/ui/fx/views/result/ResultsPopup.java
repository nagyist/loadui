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
package com.eviware.loadui.ui.fx.views.result;

import java.io.Closeable;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import com.eviware.loadui.ui.fx.api.analysis.ExecutionsInfo;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionsInfo.Data;
import com.eviware.loadui.ui.fx.control.Dialog;

public class ResultsPopup extends Stage implements Callback<Data, Void>, Closeable
{
	public ResultsPopup( Node owner, ExecutionsInfo executionsInfo )
	{
		setResizable( false );
		initStyle( StageStyle.UTILITY );
		initModality( Modality.APPLICATION_MODAL );
		initOwner(owner.getScene().getWindow());
		setTitle( "Test Runs" );

		Scene scene = SceneBuilder.create().stylesheets( "/com/eviware/loadui/ui/fx/loadui-style.css" ).root(new VBox()).build();
		setScene(scene);

		executionsInfo.runWhenReady( this );
		System.out.println("ResultsPopup constructor ended");
	}

	@Override
	public Void call( Data data )
	{
		final ResultView resultView = new ResultView( data.getRecentExecutions(), data.getArchivedExecutions(), this );
		resultView.setStyle("-fx-padding: 0;");
	 	getScene().setRoot(resultView);

		return null;
	}
}
