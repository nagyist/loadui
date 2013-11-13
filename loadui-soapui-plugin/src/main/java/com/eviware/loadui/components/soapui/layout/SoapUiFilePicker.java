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

import com.eviware.loadui.api.property.Property;
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
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private final Property<String> projectFileProperty;

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

	private final ObjectProperty<String> selectedProperty = new ObjectPropertyBase<String>()
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

	private final ChangeListener<String> selectedListener = new ChangeListener<String>()
	{
		@Override
		public void changed( ObservableValue<? extends String> observableValue, String oldValue, String newValue )
		{
			setFieldsWith( newValue );
			if( projectFileProperty != null )
				projectFileProperty.setValue( newValue );
		}
	};

	private final ChangeListener<Boolean> isRelativeListener = new ChangeListener<Boolean>()
	{
		@Override
		public void changed( ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean isNowRelative )
		{
			textLabel.setMaxWidth( isNowRelative ? 300 : 0 );
			textField.setPrefWidth( isNowRelative ? 200 : 500 );
			HBox.setMargin( textField, new Insets( 4, 4, 4, isNowRelative ? 2 : 0 ) );

			String newSelectedValue = null;

			if( selectedProperty.get() != null )
			{
				newSelectedValue = isNowRelative ?
						fileResolver.abs2rel( baseDirForRelativePaths, new File( selectedProperty.get() ) ) :
						fileResolver.rel2abs( baseDirForRelativePaths, selectedProperty.get() );
			}

			setFieldsWith( newSelectedValue );
			resolveFileUpdatingSelectedIfAcceptable();
		}
	};

	public SoapUiFilePicker( final String title, final String extensionFilterDescription,
									 final String extensionFilterRegex,
									 final FilePickerDialogFactory filePickerDialogFactory,
									 final File baseDirForRelativePaths,
									 final Property<String> projectFileProperty,
									 final Property<Boolean> externalIsRelativePathProperty )
	{
		Preconditions.checkArgument( baseDirForRelativePaths.isAbsolute() );
		this.baseDirForRelativePaths = baseDirForRelativePaths;
		this.projectFileProperty = projectFileProperty;

		setId( "soapui-file-picker" );
		setSpacing( 4 );

		HBox firstLine = new HBox( 0 );

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

		if( externalIsRelativePathProperty != null )
			isRelativePathProperty.bindBidirectional( Properties.convert( externalIsRelativePathProperty ) );
		isRelativePathProperty.addListener( isRelativeListener );

		final Button browse = ButtonBuilder
				.create().minWidth( 100 ).id( "soapui-file-picker-browse" )
				.text( "Browse..." )
				.onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent _ )
					{
						File selectedFile = filePickerDialogFactory.showOpenDialog(
								title, extensionFilterDescription, extensionFilterRegex );
						if( selectedFile != null && fileResolver.isAcceptable( selectedFile ) )
						{
							setAbsolutePath( selectedFile.getAbsolutePath() );
							projectFileProperty.setValue( selectedProperty.getValue() );
						}
					}
				} )
				.build();

		useRelPath = new CheckBox( "Use relative path" );
		useRelPath.setId( "use-rel-path" );
		useRelPath.selectedProperty().bindBidirectional( isRelativePathProperty );

		HBox.setMargin( textLabel, new Insets( 7, 0, 4, 0 ) );
		HBox.setMargin( textField, new Insets( 4, 4, 4, 2 ) );
		HBox.setMargin( browse, new Insets( 4 ) );

		VBox.setMargin( firstLine, new Insets( 4, 4, 4, 0 ) );
		firstLine.getChildren().setAll( textLabel, textField, browse );
		getChildren().setAll( firstLine, useRelPath );

		if( projectFileProperty != null && projectFileProperty.getValue() != null )
			selectedProperty.setValue( projectFileProperty.getValue() );
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
		useRelPath.selectedProperty().unbindBidirectional( isRelativePathProperty );
		isRelativePathProperty.removeListener( isRelativeListener );
		selectedProperty.removeListener( selectedListener );
	}

	public ObjectProperty<String> selectedProperty()
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
		String text = textField.getText();
		if( text.trim().isEmpty() )
			return false;

		if( !isRelativePathProperty.get() && !fileResolver.isAbsolute( textField.getText() ) )
		{
			text = File.separator + text;
		}
		File resolvedFile = fileResolver.resolveFromText( isRelativePathProperty.get(),
				baseDirForRelativePaths, text );
		log.info( "SoapUI project file resolved to {}", resolvedFile );

		if( fileResolver.isAcceptable( resolvedFile ) )
		{
			selectedProperty.setValue( text );
			return true;
		}
		log.debug( "The resolved file is not an acceptable SoapUI project file" );
		return false;
	}

	public void setAbsolutePath( String absolutePath )
	{
		if( absolutePath != null )
		{
			log.info( "Setting selected property with absolute file path: {}", absolutePath );
			if( !fileResolver.isAbsolute( absolutePath ) )
				throw new RuntimeException( "Expected absolute path but got a relative one: " + absolutePath );

			File resolvedFile = fileResolver.resolveFromText( isRelativePathProperty.get(),
					baseDirForRelativePaths, absolutePath );
			setFieldsWith( resolvedFile.getPath() );
			resolveFileUpdatingSelectedIfAcceptable();
		}
	}

	public void setIsRelativePath( boolean isRelativePath )
	{
		isRelativePathProperty.setValue( isRelativePath );
	}

	private void setFieldsWith( String filePath )
	{
		String basePath = baseDirForRelativePaths.getAbsolutePath();
		File file = null;

		if( isRelativePathProperty.get() )
		{
			textLabel.setText( basePath + File.separator );

			if( filePath != null )
			{
				String text = !fileResolver.isAbsolute( filePath ) ?
						filePath :
						fileResolver.abs2rel( baseDirForRelativePaths, new File( filePath ) );
				textField.setText( text );
				file = new File( baseDirForRelativePaths, text );
			}
		}
		else
		{
			textLabel.setText( "" );
			if( filePath != null )
			{
				textField.setText( filePath );
				file = new File( filePath );
			}
		}
		if( file != null )
			updatTextFieldStyle( fileResolver.isAcceptable( file ) );
	}

	private void updatTextFieldStyle( boolean isAcceptableFile )
	{
		textField.setStyle( "-fx-text-fill: " + ( isAcceptableFile ? "black;" : "red;" ) );
	}

	public static class FileResolver
	{

		File resolveFromText( boolean toRelativePath, File baseDir, String fileName )
		{
			File file = new File( fileName );
			if( isAbsolute( fileName ) )
			{
				if( toRelativePath )
					return new File( abs2rel( baseDir, file ) );
				else
					return file;
			}
			else
			{
				return new File( baseDir, fileName );
			}
		}

		public boolean isAbsolute( String path )
		{
			return new File( path ).isAbsolute() || path.startsWith( File.separator );
		}

		public String rel2abs( File baseDir, String relativePath )
		{
			return new File( baseDir, relativePath ).getAbsolutePath();
		}

		public String abs2rel( File baseDir, File absolutePath )
		{
			return baseDir.getAbsoluteFile().toPath()
					.relativize( absolutePath.getAbsoluteFile().toPath() ).toString();
		}

		public boolean isAcceptable( File resolvedFile )
		{
			return resolvedFile.exists() && resolvedFile.isFile();
		}

	}

}
