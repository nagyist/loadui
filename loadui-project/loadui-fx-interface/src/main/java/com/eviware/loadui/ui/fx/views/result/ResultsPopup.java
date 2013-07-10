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

import com.eviware.loadui.ui.fx.api.analysis.ExecutionsInfo;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionsInfo.Data;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.Closeable;

import static com.eviware.loadui.ui.fx.util.EventUtils.forwardIntentsFrom;
import static javafx.beans.binding.Bindings.bindContent;

public class ResultsPopup extends Stage implements Callback<Data, Void>, Closeable
{
	public ResultsPopup( Node owner, ExecutionsInfo executionsInfo )
	{
		setWindowProperties();
		initOwner( owner.getScene().getWindow() );

		Scene scene = SceneBuilder.create().root( new VBox() ).build();
		setScene( scene );
		final Scene ownerScene = owner.getScene();
		bindContent( scene.getStylesheets(), ownerScene.getStylesheets() );

		forwardIntentsFrom( this ).to( owner );

		executionsInfo.runWhenReady( this );

	}

	private void setWindowProperties()
	{
		setResizable( false );
		initStyle( StageStyle.UTILITY );
		initModality( Modality.APPLICATION_MODAL );

		setTitle( "Test Runs" );
	}

	@Override
	public Void call( Data data )
	{
		final ResultView resultView = new ResultView( data.getRecentExecutions(), data.getArchivedExecutions(), this );
		resultView.setStyle( "-fx-padding: 0;" );
		getScene().setRoot( resultView );

		return null;
	}
}
