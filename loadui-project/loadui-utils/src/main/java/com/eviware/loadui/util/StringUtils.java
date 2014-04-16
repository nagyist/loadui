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
package com.eviware.loadui.util;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility class for Strings.
 *
 * @author dain.nilsson
 */
public class StringUtils
{
	public static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

	/**
	 * Checks of the given String is null or empty.
	 *
	 * @param string
	 * @return
	 */
	public static boolean isNullOrEmpty( String string )
	{
		return string == null || string.equals( "" );
	}

	/**
	 * Capitalizes the first character of a String. If the String is null, null
	 * is returned.
	 *
	 * @param string
	 * @return
	 */
	public static String capitalize( String string )
	{
		return isNullOrEmpty( string ) ? string : string.substring( 0, 1 ).toUpperCase()
				+ string.substring( 1 ).toLowerCase();
	}

	/**
	 * Capitalizes The First Character Of Each Word In The String.
	 *
	 * @param string
	 * @return
	 */
	public static String capitalizeEachWord( String string )
	{
		if( isNullOrEmpty( string ) )
			return string;

		StringBuilder stringBuilder = new StringBuilder();
		for( String word : string.split( " " ) )
			stringBuilder.append( " " ).append( capitalize( word ) );
		stringBuilder.deleteCharAt( 0 );

		return stringBuilder.toString();
	}

	/**
	 * Shortens a String if it is longer than the given maxLength, ending an
	 * abbreviated String with "...".
	 *
	 * @param string
	 * @param maxLength
	 * @return
	 */
	public static String abbreviate( String string, int maxLength )
	{
		return string.length() <= maxLength ? string : string.substring( 0, maxLength - 3 ) + "...";
	}

	/**
	 * Shortens a String if it is longer than the given maxLength, keeping the
	 * start and end intact, placing "..." in the middle of the String where
	 * content was removed.
	 *
	 * @param string
	 * @param maxLength
	 * @return
	 */
	public static String abbreviateMiddle( String string, int maxLength )
	{
		int splitIndex = ( maxLength - 2 ) / 2;
		return string.length() <= maxLength ? string : ( string.substring( 0, splitIndex ) + "..." + string
				.substring( string.length() - ( maxLength - 3 - splitIndex ) ) );
	}

	/**
	 * Replaces all underscores with spaces
	 *
	 * @param original
	 * @return the string with all underscores replaced as spaces
	 */
	public static String replaceAllUnderscoreWithSpace( String original )
	{
		return original.replace( '_', ' ' );
	}

	/**
	 * Converts line separators in a String to the system default (defined in the
	 * System Property "line.separator").
	 *
	 * @param string
	 * @return
	 */
	public static String fixLineSeparators( String string )
	{
		return string == null ? null : string.replaceAll( "\\r\\n|\\r|\\n", LINE_SEPARATOR );
	}

	/**
	 * Converts a Collection (or array, or varargs) of Strings into a single
	 * String, joined by the system default line separator characters.
	 *
	 * @param lines
	 * @return
	 */
	public static String multiline( String... lines )
	{
		if( lines.length == 0 )
			return null;
		if( lines.length == 1 )
			return lines[0];

		StringBuilder sb = new StringBuilder( lines[0] );
		for( int i = 1; i < lines.length; i++ )
			sb.append( LINE_SEPARATOR ).append( lines[i] );

		return sb.toString();
	}

	/**
	 * Serializes a collection of Strings into a single string, which can later
	 * be deserialized to give a List of the original values. Order is preserved.
	 *
	 * @param strings
	 * @return
	 */
	public static String serialize( Collection<String> strings )
	{
		StringBuilder sb = new StringBuilder();
		for( String item : strings )
			sb.append( item.length() ).append( ":" ).append( item );

		return sb.toString();
	}

	/**
	 * Serializes several Strings into a single string, which can later be
	 * deserialized to give a List of the original values. Order is preserved.
	 *
	 * @param strings
	 * @return
	 */
	public static String serialize( String... strings )
	{
		StringBuilder sb = new StringBuilder();
		for( String item : strings )
			sb.append( item.length() ).append( ":" ).append( item );

		return sb.toString();
	}

	/**
	 * Deserializes a serialized String back into a List of the original values.
	 *
	 * @param serialized
	 * @return
	 */
	public static List<String> deserialize( String serialized )
	{
		List<String> strings = new ArrayList<>();
		String remaining = serialized;
		String[] parts = remaining.split( ":", 2 );
		while( parts.length == 2 )
		{
			int length = Integer.parseInt( parts[0] );
			strings.add( parts[1].substring( 0, length ) );
			remaining = parts[1].substring( length );
			parts = remaining.split( ":", 2 );
		}
		return strings;
	}


	public static String toHhMmSs( long seconds )
	{
		DecimalFormat f = new DecimalFormat( "00" );
		long h = seconds / 3600;
		long m = ( seconds % 3600 ) / 60;
		long s = seconds % 60;
		return f.format( h ) + ":" + f.format( m ) + ":" + f.format( s );
	}

	public static String padLeft( String str, int length )
	{
		if( length <= 0 ) length = 1;
		return String.format( "%1$" + length + "s", str );
	}

	public static String padRight( String str, int length )
	{
		if( length <= 0 ) length = 1;
		return String.format( "%1$-" + length + "s", str );
	}

	public static String toCssName( @Nonnull String label )
	{
		return label.toLowerCase().replace( " ", "-" );
	}
}
