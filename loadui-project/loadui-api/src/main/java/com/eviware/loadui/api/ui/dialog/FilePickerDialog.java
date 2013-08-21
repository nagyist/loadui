package com.eviware.loadui.api.ui.dialog;

import java.io.File;

/**
 * User: Osten
 * Date: 8/13/13
 * Time: 10:13 AM
 * This interface governs a FilePickerDialog that provides the user with a choice for a File.
 */
public interface FilePickerDialog
{
	/**
	 * Defines what kind of file the filechooser should be exclusively filtering for.
	 */
	public enum ExtensionFilter { SOAPUI_EXECUTABLE, XML, EXECUTABLE, NO_FILTER };

	/**
	 * Get the selected file.
	 */
	public File getFile();

	/**
	 *	Show the Dialog
	 */
	public void show();

	/**
	 * Supply a runnable to be run when the confirm-button is fired.
	 * @param action
	 */
	public void setOnConfirm( Runnable action );

	/**
	 * Hide the dialog.
	 */
	public void hide();
}
