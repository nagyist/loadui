/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.components.soapui.layout;

import com.eviware.loadui.api.ui.dialog.FilePickerDialogFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * a file picker dialog used by the SoapUI Runner
 */

public class SoapUiFilePicker extends VBox
{
	private long updateTextDelay = 250L;
	private final File baseDirForRelativePaths;
	final TextField textField;
	final Label textLabel;
	private final Timer updateTextTimer = new Timer( "soapui-file-picker-update-text-timer" );
	private UpdateTextTimerTask updateTextTask;
	FileResolver fileResolver = new FileResolver();

	private class UpdateTextTimerTask extends TimerTask
	{
		@Override
		public void run()
		{
			try
			{
				updateTextTask = null;
				boolean hasUpdated = resolveFileUpdatingSelectedIfAcceptable();
				updatTextFieldStyle( hasUpdated );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	private final ObjectProperty<File> selectedProperty = new ObjectPropertyBase<File>()
	{
		@Override
		public Object getBean()
		{
			return SoapUiFilePicker.this;
		}

		@Override
		public String getName()
		{
			return "selected";
		}
	};

	private final BooleanProperty isRelativePathProperty = new BooleanPropertyBase()
	{

		@Override
		public Object getBean()
		{
			return SoapUiFilePicker.this;
		}

		@Override
		public String getName()
		{
			return "isRelativePath";
		}
	};

	public SoapUiFilePicker( final String title, final String extensionFilterDescription,
									 final String extensionFilterRegex,
									 final FilePickerDialogFactory filePickerDialogFactory,
									 final File baseDirForRelativePaths )
	{
		this.baseDirForRelativePaths = baseDirForRelativePaths;
		setId( "soapui-file-picker" );
		setSpacing( 4 );

		HBox firstLine = new HBox( 4 );

		textLabel = new Label();
		textLabel.maxWidth( 300 );

		textField = TextFieldBuilder
				.create().prefWidth( 200 )
				.onKeyTyped( new EventHandler<KeyEvent>()
				{
					@Override
					public void handle( KeyEvent keyEvent )
					{
						onFileTextUpdated( textField.getText() );
					}
				} ).build();
		HBox.setHgrow( textField, Priority.ALWAYS );

		selectedProperty.addListener( new ChangeListener<File>()
		{
			@Override
			public void changed( ObservableValue<? extends File> observableValue, File oldValue, File newValue )
			{
				setFieldsWith( newValue );
			}
		} );
		isRelativePathProperty.addListener( new ChangeListener<Boolean>()
		{
			@Override
			public void changed( ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean isNowRelative )
			{
				if( selectedProperty.get() != null )
				{
					textLabel.setMaxWidth( isNowRelative ? 300 : 0 );
					textField.setPrefWidth( isNowRelative ? 200 : 500 );
					if( !isNowRelative )
						resolveFileUpdatingSelectedIfAcceptable();
					setFieldsWith( selectedProperty.get() );
				}
			}
		} );

		final Button browse = ButtonBuilder
				.create().minWidth( 100 ).id( "soapui-file-picker-browse" )
				.text( "Browse..." )
				.build();

		browse.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				File selectedFile = filePickerDialogFactory.showOpenDialog(
						title, extensionFilterDescription, extensionFilterRegex );
				if( isAcceptable( selectedFile ) )
					setSelected( selectedFile );
			}
		} );

		CheckBox useRelPath = new CheckBox( "Use relative path" );
		useRelPath.setId( "use-rel-path" );
		useRelPath.selectedProperty().bindBidirectional( isRelativePathProperty );

		firstLine.getChildren().setAll( textLabel, textField, browse );
		getChildren().setAll( firstLine, useRelPath );
	}

	void onFileTextUpdated( String text )
	{
		if( !text.equals( textField.getText() ) )
			textField.setText( text );
		cancelUpdateTextTask();
		updateTextTask = new UpdateTextTimerTask();
		updateTextTimer.schedule( updateTextTask, updateTextDelay );
	}

	void onHide()
	{
		//TODO
		cancelUpdateTextTask();
		updateTextTimer.cancel();

	}

	public ObjectProperty<File> selectedProperty()
	{
		return selectedProperty;
	}

	void setUpdateTextDelay( long delay )
	{
		updateTextDelay = delay;
	}

	private void cancelUpdateTextTask()
	{
		if( updateTextTask != null )
			updateTextTask.cancel();
	}

	private boolean resolveFileUpdatingSelectedIfAcceptable()
	{
		File resolvedFile = fileResolver.resolveFromText( isRelativePathProperty.get(),
				baseDirForRelativePaths, textField.getText() );
		if( isAcceptable( resolvedFile ) )
		{
			selectedProperty.setValue( resolvedFile );
			return true;
		}
		return false;
	}

	public void setSelected( File file )
	{
		if( file != null )
		{
			selectedProperty.set( file );
		}
	}

	public BooleanProperty getIsRelativePathProperty()
	{
		return isRelativePathProperty;
	}

	public void setIsRelativePath( boolean isRelativePath )
	{
		isRelativePathProperty.setValue( isRelativePath );
	}

	private void setFieldsWith( @Nonnull File file )
	{
		String filePath = file.getAbsolutePath();
		String basePath = baseDirForRelativePaths.getAbsolutePath();

		if( isRelativePathProperty.get() && filePath.startsWith( basePath ) )
		{
			textLabel.setText( basePath + File.separator );
			textField.setText( filePath.replace( basePath + File.separator, "" ) );
		}
		else
		{
			isRelativePathProperty.setValue( false );
			textLabel.setText( "" );
			textField.setText( file.getAbsolutePath() );
		}
		updatTextFieldStyle( isAcceptable( file ) );
	}

	private void updatTextFieldStyle( boolean isAcceptableFile )
	{
		textField.setStyle( "-fx-text-fill: " + ( isAcceptableFile ? "black;" : "red;" ) );
	}

	private boolean isAcceptable( File resolvedFile )
	{
		return resolvedFile.exists() && resolvedFile.isFile();
	}

	static class FileResolver
	{

		File resolveFromText( boolean isRelativePath, File baseDir, String fileName )
		{
			return isRelativePath ?
					new File( baseDir, fileName ) :
					new File( fileName );
		}

		String rel2abs( File baseDir, File relativePath )
		{
			return "";
		}

		String abs2rel( File baseDir, File absolutePath )
		{
			return "";
		}

	}

}
