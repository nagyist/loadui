package com.eviware.loadui.cmd.interaction;

import com.eviware.loadui.api.ui.dialog.FilePickerDialog;
import com.eviware.loadui.api.ui.dialog.FilePickerDialogFactory;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CmdFilePickerDialogFactory implements FilePickerDialogFactory
{

	Logger log = LoggerFactory.getLogger( CmdFilePickerDialogFactory.class );

	@Override
	public FilePickerDialog createPickerDialog( Scene scene, String buttonText, String stageTitle, String filePickerTitle, FilePickerDialog.ExtensionFilter filter )
	{
		log.warn( "Instantiated CMDFilePickerDialogFactory " );
		return new CmdFilePickerDialog( buttonText, stageTitle, filePickerTitle, filter );
	}

	@Override
	public File showOpenDialog( Window window, String title, FileChooser.ExtensionFilter filter )
	{
		log.warn( "Unsupported in headless" );
		return null;
	}
}