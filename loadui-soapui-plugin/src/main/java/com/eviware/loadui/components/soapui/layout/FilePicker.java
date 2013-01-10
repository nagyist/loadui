package com.eviware.loadui.components.soapui.layout;

import java.io.File;

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
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.FileChooserBuilder;
import javafx.stage.Window;

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

	public FilePicker( final Window window, String title, ExtensionFilter filters )
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
		final FileChooser chooser = FileChooserBuilder.create().extensionFilters( filters ).title( title ).build();
		final Button browse = ButtonBuilder.create().text( "Browse..." ).build();
		browse.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent arg0 )
			{
				setSelected( chooser.showOpenDialog( window ) );
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
		selectedProperty.set( file );
	}
}