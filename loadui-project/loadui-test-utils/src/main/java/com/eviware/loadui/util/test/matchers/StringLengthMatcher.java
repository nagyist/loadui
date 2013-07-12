package com.eviware.loadui.util.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * Author: maximilian.skog
 * Date: 2013-07-15
 * Time: 11:41
 */
public class StringLengthMatcher extends TypeSafeMatcher<String>
{
	private final int length;


	private StringLengthMatcher( int length )
	{
		this.length = length;
	}

	@Factory
	public static StringLengthMatcher lenghtGreaterThan( int length )
	{
		return new StringLengthMatcher( length );
	}


	@Override
	public boolean matchesSafely( String s )
	{
		return s.length() > length;
	}

	@Override
	public void describeTo( Description description )
	{
		description.appendText( "should have a lenght greater than " + length );
	}
}
