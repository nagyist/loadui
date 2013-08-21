package com.eviware.loadui.ui.fx.control;

import com.eviware.loadui.api.ui.dialog.FilePickerDialog;
import com.eviware.loadui.api.ui.dialog.FilePickerDialogFactory;
import com.eviware.loadui.util.BeanInjector;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: osten
 * Date: 8/13/13
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class FXFilePickerDialogFactory implements FilePickerDialogFactory
{
	Logger log = LoggerFactory.getLogger(FXFilePickerDialogFactory.class);

	@Override
	public FilePickerDialog createDialog( String buttonText, String stageTitle, String filePickerTitle, FilePickerDialog.ExtensionFilter filter )
	{
		Stage stage = ( Stage )BeanInjector.getBean( Stage.class );
		return new FXFilePickerDialog( stage, stageTitle, filePickerTitle, filter );
	}
}
