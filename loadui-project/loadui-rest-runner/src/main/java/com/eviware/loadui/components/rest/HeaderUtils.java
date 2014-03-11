package com.eviware.loadui.components.rest;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class HeaderUtils
{
	public static Multimap<String, String> extractHeaders( String headersBlob )
	{
		Iterable<String> headersIterable = Splitter.on( System.lineSeparator() )
				.trimResults()
				.omitEmptyStrings()
				.split( headersBlob );

		Multimap<String, String> headers = LinkedHashMultimap.create();

		for( String header : headersIterable )
		{
			Iterable<String> keyValue = Splitter.on( ":" )
					.trimResults()
					.split( header );
			headers.put( Iterables.get( keyValue, 0 ), Iterables.get( keyValue, 1 ) );
		}
		return headers;
	}
}
