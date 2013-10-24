package com.eviware.loadui.cmd.interaction;

import com.eviware.loadui.api.ui.dialog.FilePickerDialog;
import com.eviware.loadui.api.ui.dialog.FilePickerDialogFactory;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CmdFilePickerDialogFactory implements FilePickerDialogFactory
{

	Logger log = LoggerFactory.getLogger( CmdFilePickerDialogFactory.class );

	@Override
	public FilePickerDialog createPickerDialog( String buttonText, String stageTitle, String filePickerTitle, FilePickerDialog.ExtensionFilter filter )
	{
		log.warn( "Instantiated CMDFilePickerDialogFactory " );
		return new CmdFilePickerDialog( buttonText, stageTitle, filePickerTitle, filter );
	}

	@Override
	public File showOpenDialog( String title, FileChooser.ExtensionFilter filter )
	{
		log.warn( "Unsupported in headless" );
		return null;
	}
}