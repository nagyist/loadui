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
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Objects;
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

import java.io.File;

/**
 * a file picker dialog used by the SoapUI Runner
 */

public class FilePicker extends HBox
{
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

	public FilePicker( final String title, final ExtensionFilter filter )
	{
		setSpacing( 4 );

		final TextField textField = TextFieldBuilder
				.create()
				.editable( false )
				.build();

		selectedProperty.addListener( new ChangeListener<File>()
		{
			@Override
			public void changed( ObservableValue<? extends File> arg0, File oldFile, File newFile )
			{
				textField.setText( Objects.firstNonNull( newFile, "" ).toString() );
			}
		} );

		final FilePickerDialogFactory filePickerDialogFactory = BeanInjector.getBean( FilePickerDialogFactory.class );

		final Button browse = ButtonBuilder
				.create()
				.text( "Browse..." )
				.build();

		browse.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent arg0 )
			{
				setSelected( filePickerDialogFactory.showOpenDialog( title, filter ) );
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
	}
}
