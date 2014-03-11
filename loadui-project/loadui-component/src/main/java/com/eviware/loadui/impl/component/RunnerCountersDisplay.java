package com.eviware.loadui.impl.component;

import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.impl.component.categories.RunnerBase;
import com.eviware.loadui.impl.layout.FormattedStringLayoutComponent;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.impl.layout.LayoutContainerImpl;
import com.eviware.loadui.util.layout.DelayedFormattedString;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.concurrent.Callable;

import static com.eviware.loadui.impl.layout.LayoutComponentImpl.CONSTRAINTS;
import static com.eviware.loadui.impl.layout.LayoutContainerImpl.LAYOUT_CONSTRAINTS;
import static com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl.LABEL;

/**
 * This is the standard counter display that is shown in Runner component layouts.
 */
public class RunnerCountersDisplay
{
	private static FormattedStringLayoutComponent display( String label, DelayedFormattedString formattedString )
	{
		Map<String, Object> args = ImmutableMap.of(
				LABEL, label,
				CONSTRAINTS, "w 50!",
				"fString", formattedString
		);

		return new FormattedStringLayoutComponent( args );
	}

	public static LayoutContainer forRunner( final ComponentItem runner )
	{
		LayoutContainer wrapperBox = new LayoutContainerImpl( ImmutableMap.<String, Object> builder()
				.put( LAYOUT_CONSTRAINTS, "wrap, ins 0" )
				.build() );
		LayoutContainer metricsDisplay = new LayoutContainerImpl( ImmutableMap.<String, Object> builder()
				.put( LAYOUT_CONSTRAINTS, "wrap 3, align right" )
				.put( "widget", "display" )
				.build() );

		metricsDisplay.add( display( "Requests", new DelayedFormattedString( "%d", 0 ) {
			@Override
			public void update()
			{
				if( hasBehavior( runner ) )
					setValue( String.valueOf( behavior( runner ).getCurrentlyRunning()
							+ behavior( runner ).getSampleCounter().get() ) );
			}
		} ) );

		metricsDisplay.add( display( "Running", new DelayedFormattedString( "%d", 0 ) {
			@Override
			public void update()
			{
				if( hasBehavior( runner ) )
					setValue( String.valueOf( behavior( runner ).getCurrentlyRunning() ) );
			}
		} ) );

		metricsDisplay.add( display( "Completed", new DelayedFormattedString( "%d", 0 ) {
			@Override
			public void update()
			{
				if( hasBehavior( runner ) )
					setValue( String.valueOf( behavior( runner ).getSampleCounter().get() ) );
			}
		} ) );

		metricsDisplay.add( display( "Queued", new DelayedFormattedString( "%d", 0 ) {
			@Override
			public void update()
			{
				if( hasBehavior( runner ) )
					setValue( String.valueOf( behavior( runner ).getQueueSize() ) );
			}
		} ) );

		metricsDisplay.add( display( "Discarded", new DelayedFormattedString( "%d", 0 ) {
			@Override
			public void update()
			{
				if( hasBehavior( runner ) )
					setValue( String.valueOf( behavior( runner ).getDiscardCounter().get() ) );
			}
		} ) );

		metricsDisplay.add( display( "Failed", new DelayedFormattedString( "%d", 0 ) {
			@Override
			public void update()
			{
				if( hasBehavior( runner ) )
					setValue( String.valueOf( behavior( runner ).getFailureCounter().get() ) );
			}
		} ) );

		wrapperBox.add( metricsDisplay );
		return wrapperBox;
	}

	private static boolean hasBehavior( ComponentItem runner )
	{
		return behavior( runner ) != null;
	}

	private static RunnerBase behavior( ComponentItem runner )
	{
		return ( RunnerBase )runner.getBehavior();
	}
}
