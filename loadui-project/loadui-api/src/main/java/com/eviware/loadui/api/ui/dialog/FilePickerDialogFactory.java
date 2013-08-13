package com.eviware.loadui.api.ui.dialog;

/**
 * Created with IntelliJ IDEA.
 * User: osten
 * Date: 8/13/13
 * Time: 11:08 AM
 * To change this template use File | Settings | File Templates.
 */
public interface FilePickerDialogFactory
{
   public FilePickerDialog createDialog( String buttonText, String stageTitle, String filePickerTitle, FilePickerDialog.ExtensionFilter filter);
}
