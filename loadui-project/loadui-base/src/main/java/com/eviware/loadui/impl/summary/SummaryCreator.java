package com.eviware.loadui.impl.summary;

import com.eviware.loadui.api.summary.MutableSummary;
import com.eviware.loadui.api.summary.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Date;

/**
 * @author renato
 */
public abstract class SummaryCreator
{
	static final Logger log = LoggerFactory.getLogger( SummaryCreator.class );

	@Nonnull
	public Summary createSummary( @Nonnull Date startTime, @Nonnull Date endTime )
	{
		log.debug( "Creating summary with start time {} and end time {}", startTime, endTime );
		MutableSummary generatedSummary = new MutableSummaryImpl( startTime, endTime );
		appendToSummary( generatedSummary );
		return generatedSummary;
	}

	/**
	 * Called on a CanvasItem to append its summary chapters to a common summary
	 * object.
	 *
	 * @param mutableSummary summary
	 */
	abstract void appendToSummary( MutableSummary mutableSummary );

}
