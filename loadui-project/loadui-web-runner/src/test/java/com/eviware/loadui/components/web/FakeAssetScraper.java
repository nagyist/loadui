package com.eviware.loadui.components.web;

import com.eviware.loadui.util.html.HtmlAssetScraper;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FakeAssetScraper
{
	public static HtmlAssetScraper returningAssets( String... assets )
	{
		HtmlAssetScraper assetScraper = mock( HtmlAssetScraper.class );
		Set<URI> assetsUri = new HashSet<>();
		for( String asset : assets )
			assetsUri.add( URI.create(asset) );
		try
		{
			when( assetScraper.scrapeUrl( anyString() ) ).thenReturn( assetsUri );
		}
		catch( IOException e )
		{
			throw new RuntimeException( e );
		}
		return assetScraper;
	}

	public static HtmlAssetScraper returningNoAssets()
	{
		return returningAssets();
	}
}
