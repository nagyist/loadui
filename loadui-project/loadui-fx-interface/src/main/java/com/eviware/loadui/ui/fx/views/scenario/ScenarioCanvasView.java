package com.eviware.loadui.ui.fx.views.scenario;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.ui.fx.views.canvas.CanvasView;
import com.eviware.loadui.ui.fx.views.project.ProjectPlaybackPanel;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SceneBuilder;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;

import java.awt.image.BufferedImage;

/**
 * @author renato
 */
public class ScenarioCanvasView extends CanvasView
{
	private final SceneItem scenario;

	public ScenarioCanvasView( SceneItem scenario, ProjectPlaybackPanel playbackPanel,
							  ReadOnlyBooleanProperty toBindToLinkButtonVisibleProperty )
	{
		super( scenario );
		this.scenario = scenario;

		ScenarioToolbar toolbar = new ScenarioToolbar( scenario );

		ToggleButton linkButton = playbackPanel.addLinkButton( scenario );
		linkButton.visibleProperty().bind( toBindToLinkButtonVisibleProperty );

		StackPane.setAlignment( toolbar, Pos.TOP_CENTER );
		StackPane.setMargin( toolbar, new Insets( -60, 0, 0, 0 ) );
		StackPane.setMargin( this, new Insets( 60, 0, 0, 0 ) );
		getChildren().add( toolbar );
	}

	@Override
	public void release()
	{
		snapshotScene();
		super.release();
	}

	private void snapshotScene()
	{
		Region gridRegion = RegionBuilder.create().styleClass( "grid" ).style( "-fx-background-repeat: repeat;" )
				.build();

		//Hack for setting CSS resources within an OSGi framework
		String gridUrl = CanvasView.class.getResource( "grid-box.png" ).toExternalForm();
		gridRegion.setStyle( "-fx-background-image: url('" + gridUrl + "');" );

		StackPane completeCanvas = StackPaneBuilder.create().children( gridRegion ).build();
		SceneBuilder.create().root( completeCanvas )
				.width( getWidth() )
				.height( getHeight() )
				.build();

		WritableImage fxImage = completeCanvas.snapshot( null, null );
		BufferedImage bimg = SwingFXUtils.fromFXImage( fxImage, null );
		bimg = UIUtils.scaleImage( bimg, 332, 175 );
		String base64 = NodeUtils.toBase64Image( bimg );

		scenario.setAttribute( "miniature_fx2", base64 );
	}

}
