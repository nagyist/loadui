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
package com.eviware.loadui.ui.fx.util;

import javafx.scene.control.TreeItem;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.traits.Labeled;

public class TreeUtils
{
	public static TreeItem<Labeled> dummyItem( final String label )
	{
		return new TreeItem<Labeled>( new LabeledKeyValue( label, label ) );
	}

	public static TreeItem<Labeled> dummyItem( final String label, @Nonnull final String value )
	{
		return new TreeItem<Labeled>( new LabeledKeyValue( label, value ) );
	}

	public static class LabeledKeyValue<Key extends String, Value extends Object> implements Labeled
	{
		private final Key label;
		private final Value value;

		public LabeledKeyValue( @Nonnull final Key label, final Value value )
		{
			this.label = label;
			this.value = value;
		}

		@Override
		public String getLabel()
		{
			return label.toString();
		}

		public Value getValue()
		{
			return value;
		}
	}
}
