package com.eviware.loadui.ui.fx.views.workspace;

import org.loadui.testfx.categories.TestFX;
import org.loadui.testfx.FXTestUtils;
import org.loadui.testfx.GuiTest;
import com.google.common.util.concurrent.SettableFuture;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.SceneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.TimeUnit;

import static com.eviware.loadui.util.NewVersionChecker.VersionInfo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Category( TestFX.class )
public class NewVersionDialogTest extends GuiTest
{
	private static Stage stage;
	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();

	public static class NewVersionDialogTestApp extends Application
	{
		@Override
		public void start( final Stage primaryStage ) throws Exception
		{
			primaryStage.setOnShown( new EventHandler<WindowEvent>()
			{
				@Override
				public void handle( WindowEvent windowEvent )
				{
					new NewVersionDialog( primaryStage.getScene().getRoot(), new VersionInfo("3.0.0","http://dl.eviware.com/version-update/versiontracker/notespage-versiontracker-loadui.html","http://www.loadui.org",null)).show();
				}
			} );

			primaryStage.setScene( SceneBuilder.create().root( new VBox() ).build() );
			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		FXTestUtils.launchApp( NewVersionDialogTestApp.class );
		stage = stageFuture.get( 10, TimeUnit.SECONDS );
		FXTestUtils.bringToFront( stage );
	}

	@Test
	public void test()
	{
		WebView webview = find(".web-view");
		waitUntil( webview.getEngine().getTitle(), is("Update loadUI Core" ) );
	}
}
