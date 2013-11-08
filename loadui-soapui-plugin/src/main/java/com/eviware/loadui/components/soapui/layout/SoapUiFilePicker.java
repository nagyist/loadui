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
import com.google.common.base.Preconditions;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * a file picker dialog used by the SoapUI Runner
 */

public class SoapUiFilePicker extends VBox
{
	static Logger log = LoggerFactory.getLogger( SoapUiFilePicker.class );

	private long updateTextDelay = 250L;
	private final File baseDirForRelativePaths;
	public final TextField textField;
	final Label textLabel;
	private final Timer updateTextTimer = new Timer( "soapui-file-picker-update-text-timer" );
	private UpdateTextTimerTask updateTextTask;
	FileResolver fileResolver = new FileResolver();
	private final CheckBox useRelPath;

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

	private final ChangeListener<File> selectedListener = new ChangeListener<File>()
	{
		@Override
		public void changed( ObservableValue<? extends File> observableValue, File oldValue, File newValue )
		{
			setFieldsWith( newValue );
		}
	};

	private final ChangeListener<Boolean> isRelativeListener = new ChangeListener<Boolean>()
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
	};

	public SoapUiFilePicker( final String title, final String extensionFilterDescription,
									 final String extensionFilterRegex,
									 final FilePickerDialogFactory filePickerDialogFactory,
									 final File baseDirForRelativePaths )
	{
		Preconditions.checkArgument( baseDirForRelativePaths.isAbsolute() );
		this.baseDirForRelativePaths = baseDirForRelativePaths;

		setId( "soapui-file-picker" );
		setSpacing( 4 );

		HBox firstLine = new HBox( 4 );

		textLabel = new Label();
		textLabel.maxWidth( 300 );

		textField = TextFieldBuilder
				.create().prefWidth( 200 )
				.onKeyPressed( new EventHandler<KeyEvent>()
				{
					@Override
					public void handle( KeyEvent keyEvent )
					{
						onFileTextUpdated( textField.getText() );
					}
				} ).build();
		HBox.setHgrow( textField, Priority.ALWAYS );

		selectedProperty.addListener( selectedListener );
		isRelativePathProperty.addListener( isRelativeListener );

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

		useRelPath = new CheckBox( "Use relative path" );
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
		cancelUpdateTextTask();
		updateTextTimer.cancel();
		isRelativePathProperty.unbindBidirectional( useRelPath.selectedProperty() );
		isRelativePathProperty.removeListener( isRelativeListener );
		selectedProperty.removeListener( selectedListener );
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
		log.info( "SoapUI project file resolved to {}", resolvedFile );

		if( isAcceptable( resolvedFile ) )
		{
			selectedProperty.setValue( resolvedFile );
			return true;
		}
		log.debug( "The resolved file is not an acceptable SoapUI project file" );
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
		String basePath = baseDirForRelativePaths.getAbsolutePath();

		if( isRelativePathProperty.get() )
		{
			textLabel.setText( basePath + File.separator );
			String text = fileResolver.abs2rel( baseDirForRelativePaths, file );
			textField.setText( text );
		}
		else
		{
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
			File file = new File( fileName );
			if( file.isAbsolute() )
			{
				if( isRelativePath )
					return new File( abs2rel( baseDir, file ) );
				else
					return file;
			}
			else
			{
				if( isRelativePath )
					return new File( baseDir, fileName );
				else
					return new File( rel2abs( baseDir, file ) );
			}
		}

		String rel2abs( File baseDir, File relativePath )
		{
			return new File( baseDir, relativePath.getPath() ).getAbsolutePath();
		}

		String abs2rel( File baseDir, File absolutePath )
		{
			return baseDir.getAbsoluteFile().toPath()
					.relativize( absolutePath.getAbsoluteFile().toPath() ).toString();
		}

	}

}
