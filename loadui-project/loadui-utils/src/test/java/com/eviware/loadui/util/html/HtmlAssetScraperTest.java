package com.eviware.loadui.util.html;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Set;

import static com.eviware.loadui.util.html.HtmlAssetScraper.ATOM_TYPE;
import static com.eviware.loadui.util.html.HtmlAssetScraper.RSS_TYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HtmlAssetScraperTest
{
	@Test
	public void shouldFindAssets() throws Exception
	{
		File htmlFile = new File( HtmlAssetScraperTest.class.getResource( "/scrape-me.html" ).toURI() );
		String htmlString = Files.toString( htmlFile, Charset.defaultCharset() );

		Set<URI> found = HtmlAssetScraper.create()
				.ignoreTypes( RSS_TYPE, ATOM_TYPE )
				.scrapeHtml( htmlString );

		Set<URI> expected = ImmutableSet.of(
				URI.create( "http://www.example.org/res/style.css" ),
				URI.create( "http://www.absolute-link.org/image.jpg" ),
				URI.create( "http://www.example.org/relative-image.png" ),
				URI.create( "http://www.example.org/res/prettify.js" ),
				URI.create( "http://www.absolute-link.org/jquery.js" )
		);
		assertThat( found, is( expected ) );
	}
}
