package com.eviware.loadui.cmd.interaction;

import com.eviware.loadui.api.ui.dialog.FilePickerDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: osten
 * Date: 8/13/13
 * Time: 1:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class CmdFilePickerDialog implements FilePickerDialog
{
	Logger log = LoggerFactory.getLogger( CmdFilePickerDialog.class );

	public CmdFilePickerDialog( String buttonText, String stageTitle, String filePickerTitle, ExtensionFilter filter ){
		 log.warn( "Instantiated constructor, this should not happen. " );
	}

	@Override
	public File getFile()
	{
		log.warn( "trying to return file from an empty implementation. " );
		throw new UnsupportedOperationException();
	}

	@Override
	public void show()
	{
		log.warn( "Trying to show dialog from an empty implementation" );
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOnConfirm( Runnable action )
	{
		log.warn( "There is no button that can confirm this dialog as it is an empty implementation" );
		throw new UnsupportedOperationException();
	}

	@Override
	public void hide()
	{
		log.warn( "There is nothing to hide in an empty implementation" );
		throw new UnsupportedOperationException();
	}

}
