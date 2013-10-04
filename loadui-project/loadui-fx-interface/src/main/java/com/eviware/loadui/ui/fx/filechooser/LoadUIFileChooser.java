package com.eviware.loadui.ui.fx.filechooser;

import com.eviware.loadui.api.model.WorkspaceItem;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.List;

/**
 * Delegation of FileChooser which also includes directory management and saving file with extension if a single one is provided.
 * Author: maximilian.skog
 * Date: 2013-10-02
 * Time: 15:16
 */
@SuppressWarnings( "unused" )
public class LoadUIFileChooser
{
	private FileChooser fileChooser;
	private WorkspaceItem workspace;

	LoadUIFileChooser( WorkspaceItem workspace )
	{
		if( workspace == null )
		{
			throw new NullPointerException( "Workspace is null" );
		}

		this.workspace = workspace;

		fileChooser = new FileChooser();
	}

	public void setTitle( String s )
	{
		fileChooser.setTitle( s );
	}

	File getInitialDirectory()
	{
		return fileChooser.getInitialDirectory();
	}

	ObservableList<FileChooser.ExtensionFilter> getExtensionFilters()
	{
		return fileChooser.getExtensionFilters();
	}

	public StringProperty titleProperty()
	{
		return fileChooser.titleProperty();
	}

	public File showSaveDialog( Window window )
	{
		String attributeIdentifier = createIdentifier();

		fileChooser.setInitialDirectory( workspace.getLatestDirectory( attributeIdentifier ) );
		File file = fileChooser.showSaveDialog( window );

		if( file != null )
		{
			workspace.setLatestDirectory( attributeIdentifier, file.getParentFile() );

			if( getExtensionFilters().size() == 1 && getExtensionFilters().size() == 1 )
			{
				file = checkExtension( file );
			}

		}
		return file;
	}


	public List<File> showOpenMultipleDialog( Window window )
	{
		String attributeIdentifier = createIdentifier();

		fileChooser.setInitialDirectory( workspace.getLatestDirectory( attributeIdentifier ) );
		List<File> files = fileChooser.showOpenMultipleDialog( window );

		if( !files.isEmpty() )
		{
			workspace.setLatestDirectory( attributeIdentifier, files.get( 0 ).getParentFile() );
		}

		return files;
	}

	public File showOpenDialog( Window window )
	{
		String attributeIdentifier = createIdentifier();

		fileChooser.setInitialDirectory( workspace.getLatestDirectory( attributeIdentifier ) );
		File file = fileChooser.showOpenDialog( window );

		if( file != null )
		{
			workspace.setLatestDirectory( attributeIdentifier, file.getParentFile() );
		}

		return file;
	}

	public String getTitle()
	{
		return fileChooser.getTitle();
	}

	private String createIdentifier()
	{
		String identifier = "dir.";

		identifier += getExtensionFilters().get( 0 ).getDescription().replaceAll( " ", "." );

		return identifier;
	}

	private File checkExtension( File file )
	{
				/*
				 * this code might be unnecessary when they release javafx 8.0
				 * and may make FileChooser be able to save with extension.
				 * Confirmed to be fixed in JavaFx 8 at: https://javafx-jira.kenai.com/browse/RT-18836
				 */
		String extension = getExtensionFilters().get( 0 ).getExtensions().get( 0 ).substring( 1 );

		if( !file.getName().endsWith( extension ) )
		{
			file = createUniqueFileWithExtension( file.getAbsolutePath(), extension );
		}

		return file;
	}

	private File createUniqueFileWithExtension( String AbsolutePathToFile, String extension )
	{
		File file = new File( AbsolutePathToFile + extension );

		for( int i = 2; file.exists(); i++ )
		{
			file = new File( AbsolutePathToFile + "(" + i + ")" + extension );
		}

		return file;
	}
}
