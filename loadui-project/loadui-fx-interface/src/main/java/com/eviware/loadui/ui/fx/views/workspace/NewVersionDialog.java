package com.eviware.loadui.ui.fx.views.workspace;

import com.eviware.loadui.ui.fx.control.ButtonDialog;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.util.NewVersionChecker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;

import javax.annotation.Nonnull;

public class NewVersionDialog extends ButtonDialog
{
	public NewVersionDialog( @Nonnull Node owner, final NewVersionChecker.VersionInfo versionInfo )
	{
		super( owner, "New version available" );

		WebView whatsNew = new WebView();
		whatsNew.getEngine().load( versionInfo.releaseNotes );

		getItems().setAll( whatsNew );

		Button downloadButton = new Button( "Download" );
		downloadButton.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				UIUtils.openInExternalBrowser( versionInfo.downloadUrl );
				close();
			}
		} );

		Button remindLaterButton = new Button( "Remind me later" );
		remindLaterButton.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				close();
			}
		} );

		Button skipVersionButton = new Button( "Skip this version" );
		skipVersionButton.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				versionInfo.skipThisVersion();
				close();
			}
		} );

		getButtons().setAll( downloadButton, remindLaterButton, skipVersionButton );
	}
}
