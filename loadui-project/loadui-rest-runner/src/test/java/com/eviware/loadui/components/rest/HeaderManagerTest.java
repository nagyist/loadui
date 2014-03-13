package com.eviware.loadui.components.rest;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HeaderManagerTest
{
	@Test
	public void shouldExtractHeaders()
	{
		String headerBlob = "Content-Type: text/xml; charset=utf-8" + System.lineSeparator() +
				"Content-Length: 123" + System.lineSeparator() +
				"Multiple-values1: 1, 2, 3" + System.lineSeparator() +
				"Multiple-values2: a" + System.lineSeparator() +
				"Multiple-values2: b";

		Multimap<String, String> expectedHeaders = LinkedHashMultimap.create();
		expectedHeaders.put( "Content-Type", "text/xml; charset=utf-8" );
		expectedHeaders.put( "Content-Length", "123" );
		expectedHeaders.put( "Multiple-values1", "1, 2, 3" );
		expectedHeaders.put( "Multiple-values2", "a" );
		expectedHeaders.put( "Multiple-values2", "b" );

		Multimap<String, String> headers = HeaderManager.extractHeaders( headerBlob );

		assertThat( headers, is( expectedHeaders ) );
	}
}
