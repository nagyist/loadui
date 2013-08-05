package com.eviware.loadui.ui.fx.control;


import com.eviware.loadui.util.BeanInjector;
import com.sun.javafx.PlatformUtil;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.LabelBuilder;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: osten
 * Date: 7/31/13
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilePickerDialog extends ConfirmationDialog
{

	private FilePicker picker;

	public FilePickerDialog( Node parent, String dialogTitle, String filePickerTitle, FileChooser.ExtensionFilter extensionFilter )
	{
		super( parent, dialogTitle,
				"Set" );

		Stage stage = ( Stage )BeanInjector.getBean( Stage.class );
		picker = new FilePicker( stage, filePickerTitle, extensionFilter );

		getItems().setAll(
				LabelBuilder.create().
						text( filePickerTitle )
						.build(),
				picker );
	}

	public FilePickerDialog( Node parent, String dialogTitle, String filePickerTitle )
	{
		super( parent, dialogTitle,
				"Set" );

		Stage stage = ( Stage )BeanInjector.getBean( Stage.class );
		picker = new FilePicker( stage, filePickerTitle, new FileChooser.ExtensionFilter( "*" ) );

		getItems().setAll(
				LabelBuilder.create().
						text( filePickerTitle )
						.build(),
				picker );
	}

	public ObjectProperty<File> SelectedFileProperty()
	{
		return picker.selectedProperty();
	}

	public static FileChooser.ExtensionFilter getExtensionFilterForSoapUIExecutableByPlatform()
	{

		if( PlatformUtil.isWindows() )
		{
			return new FileChooser.ExtensionFilter( "SoapUI executable", "soapui*.exe" );
		}
		else
		{
			return new FileChooser.ExtensionFilter( "SoapUI executable", "soapui*.sh" );
		}
	}


	public static FileChooser.ExtensionFilter getExtensionFilterForExecutableByPlatform()
	{
		if( PlatformUtil.isWindows() )
		{
			return new FileChooser.ExtensionFilter( "Executable", "*.exe" );
		}
		else
		{
			return new FileChooser.ExtensionFilter( "Executable", "*.sh" );
		}
	}

	public static FileChooser.ExtensionFilter getExtensionFilterForXML()
	{
		return new FileChooser.ExtensionFilter( "eXtensible Markup Language", "*.xml" );
	}
}
