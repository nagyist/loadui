package com.eviware.loadui.util.html;

import com.google.common.collect.ImmutableSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class HtmlAssetScraper
{
	public static String RSS_TYPE = "application/rss+xml";
	public static String ATOM_TYPE = "application/atom+xml";

	private ImmutableSet<String> domainsAllowed = ImmutableSet.of();
	private ImmutableSet<String> typesToIgnore = ImmutableSet.of();

	private HtmlAssetScraper()
	{
	}

	public static HtmlAssetScraper create()
	{
		return new HtmlAssetScraper();
	}

	public HtmlAssetScraper filterDomains( Iterable<String> domainsAllowed )
	{
		Set<String> domainsAllowedLowerCase = new HashSet<>();
		for( String domain : domainsAllowed )
			domainsAllowedLowerCase.add( domain.toLowerCase() );
		this.domainsAllowed = ImmutableSet.copyOf( domainsAllowedLowerCase );
		return this;
	}

	public HtmlAssetScraper filterDomains( String... domainsAllowed )
	{
		this.domainsAllowed = ImmutableSet.copyOf( domainsAllowed );
		return this;
	}

	public HtmlAssetScraper ignoreTypes( Iterable<String> typesToIgnore )
	{
		this.typesToIgnore = ImmutableSet.copyOf( typesToIgnore );
		return this;
	}

	public HtmlAssetScraper ignoreTypes( String... typesToIgnore )
	{
		this.typesToIgnore = ImmutableSet.copyOf( typesToIgnore );
		return this;
	}

	public Set<URI> scrapeUrl( String url ) throws IOException
	{
		Document doc = Jsoup.connect( url ).get();
		return parseDocument( doc );
	}

	public Set<URI> scrapeHtml( String html ) throws IOException
	{
		Document doc = Jsoup.parse( html );
		return parseDocument( doc );
	}

	private Set<URI> parseDocument( Document doc )
	{
		Elements media = doc.select( "[src]" );
		Elements imports = doc.select( "link[href]" );

		Set<URI> alreadySeen = new HashSet<>();
		for( Element src : media )
		{
			addIfDomainMatches( src.attr( "abs:src" ), alreadySeen );
		}

		for( Element link : imports )
		{
			String type = link.attr( "type" );
			if( !shouldIgnore( type ) )
				addIfDomainMatches( link.attr( "abs:href" ), alreadySeen );
		}

		return alreadySeen;
	}

	private boolean shouldIgnore( String type )
	{
		return typesToIgnore.contains( type );
	}

	private void addIfDomainMatches( String urlString, Set<URI> alreadySeen )
	{
		URI uri;
		try
		{
			uri = new URI( urlString );
		}
		catch( URISyntaxException e )
		{
			System.out.println( "Malformed URI found in HTML: " + urlString );
			return;
		}

		if( domainAllowed( uri ) && !alreadySeen.contains( uri ) )
		{
			alreadySeen.add( uri );
		}
	}

	private boolean domainAllowed( URI uri )
	{
		return domainsAllowed.isEmpty() || domainsAllowed.contains( domainNameOf( uri ) );
	}

	private static String domainNameOf( URI uri )
	{
		String domain = uri.getHost();
		return domain.startsWith( "www." ) ? domain.substring( 4 ) : domain;
	}
}