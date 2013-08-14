package com.eviware.loadui.cmd.interaction;

import com.eviware.loadui.api.ui.dialog.FilePickerDialog;
import com.eviware.loadui.api.ui.dialog.FilePickerDialogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdFilePickerDialogFactory implements FilePickerDialogFactory
{

	Logger log = LoggerFactory.getLogger( CmdFilePickerDialogFactory.class );

	@Override
	public FilePickerDialog createDialog( String buttonText, String stageTitle, String filePickerTitle, FilePickerDialog.ExtensionFilter filter )
	{
		log.warn( "Instantiated CMDFilePickerDialogFactory " );
		return new CmdFilePickerDialog( buttonText, stageTitle, filePickerTitle, filter );
	}
}