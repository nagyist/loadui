package com.eviware.loadui.ui.fx.views.syslog;

import org.apache.log4j.spi.LoggingEvent;

import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Wrapper for a log4j LoggingEvent which provides a nice String representation of it.
 */
public class LoggingEventWrapper
{
	private String generatedString = null;
	private String additionalInfo = "";
	private LoggingEvent event;

	LoggingEventWrapper( LoggingEvent event )
	{
		checkNotNull( event );
		this.event = event;
	}

	public void addAdditionalInfo( String info )
	{
		this.additionalInfo = info;
	}

	public String toString()
	{
		if( generatedString == null )
		{
			StringBuilder sb = new StringBuilder();
			sb.append( new Date( event.getTimeStamp() ) ).append( ": " ).append( event.getMessage() ).append( "\r\n" );
			generatedString = sb.toString();
		}
		return generatedString;
	}
}
