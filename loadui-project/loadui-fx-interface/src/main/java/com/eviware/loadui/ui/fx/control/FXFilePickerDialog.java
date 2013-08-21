package com.eviware.loadui.ui.fx.control;


import com.eviware.loadui.api.ui.dialog.FilePickerDialog;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.util.BeanInjector;
import com.sun.javafx.PlatformUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.LabelBuilder;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: osten
 * Date: 7/31/13
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class FXFilePickerDialog extends ConfirmationDialog implements FilePickerDialog
{
	private FilePicker picker;

	Logger log = LoggerFactory.getLogger( FXFilePickerDialog.class );

	public FXFilePickerDialog( Stage stage, String dialogTitle, String filePickerTitle, ExtensionFilter filter )
	{
		super( stage.getScene().getRoot(), dialogTitle, "Set" );

		picker = new FilePicker( stage, filePickerTitle, getExtensionFilter( filter ) );

		getItems().setAll(
				LabelBuilder.create().
						text( filePickerTitle )
						.build(),
				picker );
	}

	public FXFilePickerDialog( Stage stage, String dialogTitle, String filePickerTitle )
	{
		super( stage.getScene().getRoot(), dialogTitle,
				"Set" );

		picker = new FilePicker( stage, filePickerTitle, getExtensionFilter( ExtensionFilter.NO_FILTER ) );

		getItems().setAll(
				LabelBuilder.create().
						text( filePickerTitle )
						.build(),
				picker );
	}

	private FileChooser.ExtensionFilter getExtensionFilter( ExtensionFilter filter )
	{

		if( filter == ExtensionFilter.SOAPUI_EXECUTABLE )
		{
			return getExtensionFilterForSoapUIExecutableByPlatform();
		}
		else if( filter == ExtensionFilter.EXECUTABLE )
		{
			return getExtensionFilterForExecutableByPlatform();
		}
		else if( filter == ExtensionFilter.XML )
		{
			return getExtensionFilterForXML();
		}
		else
		{
			return getEmptyExtensionFilter();
		}
	}

	@Override
	public void setOnConfirm( final Runnable action )
	{
		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent actionEvent )
			{
				fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, action ) );
				hide();
			}
		} );
	}

	@Override
	public void hide(){
		super.hide();
	}

	@Override
	public File getFile()
	{
		return picker.getSelected();
	}

	public FileChooser.ExtensionFilter getExtensionFilterForSoapUIExecutableByPlatform()
	{

		if( PlatformUtil.isWindows() )
		{
			return new FileChooser.ExtensionFilter( "SoapUI Executable", "soapui*.exe" );
		}
		else if( PlatformUtil.isMac() )
		{
			return new FileChooser.ExtensionFilter( "SoapUI Executable", "soapUI*.app" );
		}
		else
		{
			return new FileChooser.ExtensionFilter( "SoapUI Executable", "soapui*.sh" );
		}
	}

	public FileChooser.ExtensionFilter getExtensionFilterForExecutableByPlatform()
	{
		if( PlatformUtil.isWindows() )
		{
			return new FileChooser.ExtensionFilter( "Executable", "*.exe" );
		}
		else if( PlatformUtil.isMac() )
		{
			return new FileChooser.ExtensionFilter( "Executable", "*.app" );
		}
		else
		{
			return new FileChooser.ExtensionFilter( "Executable", "*.sh" );
		}
	}

	public FileChooser.ExtensionFilter getExtensionFilterForXML()
	{
		return new FileChooser.ExtensionFilter( "eXtensible Markup Language", "*.xml" );
	}

	public FileChooser.ExtensionFilter getEmptyExtensionFilter()
	{
		return new FileChooser.ExtensionFilter( "Any File", "*" );
	}
}
