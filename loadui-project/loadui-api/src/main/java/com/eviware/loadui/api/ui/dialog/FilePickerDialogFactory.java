package com.eviware.loadui.api.ui.dialog;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: osten
 * Date: 8/13/13
 * Time: 11:08 AM
 */
public interface FilePickerDialogFactory
{
	public FilePickerDialog createPickerDialog( String buttonText, String stageTitle, String filePickerTitle, FilePickerDialog.ExtensionFilter filter );

	public File showOpenDialog( String title, String extensionFilterDescription,
										 String extensionFilterRegex );
}
