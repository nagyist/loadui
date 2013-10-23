package com.eviware.loadui.api.ui.dialog;

import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: osten
 * Date: 8/13/13
 * Time: 11:08 AM
 */
public interface FilePickerDialogFactory
{
	public FilePickerDialog createPickerDialog( Scene scene, String buttonText, String stageTitle, String filePickerTitle, FilePickerDialog.ExtensionFilter filter );

	public File showOpenDialog( Window window, String title, FileChooser.ExtensionFilter filter );
}
