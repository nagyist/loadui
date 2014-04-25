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
package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.beans.property.SimpleObjectProperty;
import javafx.util.StringConverter;

import javax.annotation.Nonnull;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;

import com.eviware.loadui.util.statistics.ZoomLevel;

final class MillisToTickMark extends StringConverter<Number>
{
	private final SimpleObjectProperty<ZoomLevel> zoomLevelProperty;
	private final PeriodFormatter timeFormatter;

	MillisToTickMark( SimpleObjectProperty<ZoomLevel> zoomLevelProperty, PeriodFormatter timeFormatter )
	{
		this.zoomLevelProperty = zoomLevelProperty;
		this.timeFormatter = timeFormatter;
	}

	@Override
	public String toString( @Nonnull final Number n )
	{
		long value = n.longValue();

		ZoomLevel zoomLevel = zoomLevelProperty.get();
		ZoomLevel parentZoomLevel = zoomLevel.zoomOut();
		long parentInterval = parentZoomLevel.getInterval();
		if( value / 1000 % parentInterval >= zoomLevel.getInterval() )
		{
			return Long.toString( value / 1000 / zoomLevel.getInterval()
					% ( parentZoomLevel.getInterval() / zoomLevel.getInterval() ) );
		}
		return prettyPrintTime( n );
	}

	private String prettyPrintTime( final Number n )
	{
		Period period = new Period( n.longValue() );

		period = trimPeriod( period );

		String res = timeFormatter.print( period );
		String[] timeUnits = res.split( " " );
		return timeUnits[timeUnits.length - 1];
	}

	private Period trimPeriod( Period period )
	{
		period = period.normalizedStandard();
		switch( zoomLevelProperty.get() )
		{
		case WEEKS :
			period = period.withWeeks( 0 );
		case DAYS :
			period = period.withDays( 0 );
		case HOURS :
			period = period.withHours( 0 );
		case MINUTES :
			period = period.withMinutes( 0 );
		default :
			break;
		}
		return period;
	}

	private Number fromString( String s, ZoomLevel fromZoomLevel )
	{
		try
		{
			return Long.parseLong( s ) * fromZoomLevel.getInterval() * 1000;
		}
		catch( NumberFormatException e )
		{
			return timeFormatter.parsePeriod( s ).toStandardDuration().getMillis();
		}
	}

	public String changeZoomLevel( String s, ZoomLevel fromZoomLevel )
	{
		return toString( fromString( s, fromZoomLevel ) );
	}

	@Override
	public Number fromString( String _ )
	{
		throw new UnsupportedOperationException();
	}

	public String generatePositionString( final long millis )
	{
		return timeFormatter.print( trimPeriod( new Period( millis ) ) );
	}
}
