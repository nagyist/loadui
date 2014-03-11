package com.eviware.loadui.components.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HeaderUtilsTest
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

		Multimap<String, String> headers = HeaderUtils.extractHeaders( headerBlob );

		assertThat( headers, is( expectedHeaders ) );
	}
}
