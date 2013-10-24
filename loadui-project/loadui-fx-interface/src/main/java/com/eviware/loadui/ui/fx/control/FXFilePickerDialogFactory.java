package com.eviware.loadui.ui.fx.control;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.ui.dialog.FilePickerDialog;
import com.eviware.loadui.api.ui.dialog.FilePickerDialogFactory;
import com.eviware.loadui.ui.fx.filechooser.LoadUIFileChooser;
import com.eviware.loadui.ui.fx.filechooser.LoadUIFileChooserBuilder;
import com.eviware.loadui.util.BeanInjector;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: osten
 * Date: 8/13/13
 * Time: 2:50 PM
 */
public class FXFilePickerDialogFactory implements FilePickerDialogFactory
{
	Logger log = LoggerFactory.getLogger( FXFilePickerDialogFactory.class );
	WorkspaceProvider workspaceProvider;


	public FXFilePickerDialogFactory( WorkspaceProvider workspaceProvider )
	{
		this.workspaceProvider = workspaceProvider;
	}


	@Override
	public FilePickerDialog createPickerDialog( String buttonText, String stageTitle, String filePickerTitle, FilePickerDialog.ExtensionFilter filter )
	{
		WorkspaceItem workspace = workspaceProvider.getWorkspace();
		return new FXFilePickerDialog( stageTitle, filePickerTitle, filter, workspace );
	}

	@Override
	public File showOpenDialog( String title, FileChooser.ExtensionFilter filter )
	{
		WorkspaceItem workspace = workspaceProvider.getWorkspace();
		LoadUIFileChooser fileChooser = LoadUIFileChooserBuilder
				.usingWorkspace( workspace )
				.extensionFilters( filter )
				.title( title )
				.build();

		return fileChooser.showOpenDialog( BeanInjector.getBean( Stage.class ).getScene().getWindow() );
	}
}
