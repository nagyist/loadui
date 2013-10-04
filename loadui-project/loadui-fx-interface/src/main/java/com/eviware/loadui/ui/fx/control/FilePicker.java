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
package com.eviware.loadui.ui.fx.control;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.filechooser.LoadUIFileChooser;
import com.eviware.loadui.ui.fx.filechooser.LoadUIFileChooserBuilder;
import com.eviware.loadui.ui.fx.input.SelectableImpl;
import com.google.common.base.Objects;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A form field that contains a TextField and a Browse button, that opens a
 * file chooser dialog.
 *
 * @author maximilian.skog
 */

public class FilePicker extends HBox
{
	protected static final Logger log = LoggerFactory.getLogger( FilePicker.class );

	private final ObjectProperty<File> selectedProperty = new ObjectPropertyBase<File>()
	{
		@Override
		public Object getBean()
		{
			return FilePicker.this;
		}

		@Override
		public String getName()
		{
			return "selected";
		}
	};

	public FilePicker( final Window window, String title, ExtensionFilter filters, WorkspaceItem workspace )
	{
		final TextField textField = TextFieldBuilder.create().editable( false ).build();
		selectedProperty.addListener( new ChangeListener<File>()
		{
			@Override
			public void changed( ObservableValue<? extends File> arg0, File oldFile, File newFile )
			{
				textField.setText( Objects.firstNonNull( newFile, "" ).toString() );
			}
		} );
		final LoadUIFileChooser chooser = LoadUIFileChooserBuilder
				.usingWorkspace( workspace )
				.extensionFilters( filters )
				.title( title )
				.build();

		final Button browse = ButtonBuilder
				.create()
				.text( "Browse..." )
				.build();

		browse.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent arg0 )
			{
				setSelected( chooser.showOpenDialog( window ) );
			}
		} );

		textField.focusedProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( javafx.beans.Observable _ )
			{
				if( textField.isFocused() )
				{
					SelectableImpl.deselectAll();
				}
			}
		} );

		getChildren().setAll( textField, browse );
	}

	public ObjectProperty<File> selectedProperty()
	{
		return selectedProperty;
	}

	public File getSelected()
	{
		return selectedProperty.get();
	}

	public void setSelected( File file )
	{
		if( file != null )
		{
			selectedProperty.set( file );
		}
		else
		{
			System.out.println( "tried to add a non-file, skipping." );
		}
	}
}
