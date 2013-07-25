package com.eviware.loadui.util;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.traits.Labeled;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

public class CanvasItemNameGenerator
{
	private static Function<Labeled, String> getLabel = new Function<Labeled, String>()
	{
		@Nullable
		@Override
		public String apply( @Nullable Labeled component )
		{
			return component.getLabel();
		}
	};

	public static String generateScenarioName( ProjectItem project )
	{
		Iterable<String> scenarioNames = transform( project.getChildren(), getLabel );

		return findAvailableName( "Scenario", scenarioNames );
	}

	public static String generateComponentName( CanvasItem canvas, String componentBaseName )
	{
		List<CanvasItem> canvasList = new LinkedList<>();
		canvasList.add( canvas.getProject() );
		canvasList.addAll( canvas.getProject().getChildren() );

		Iterable<String> componentNames = getComponentNames( canvasList );

		return findAvailableName( componentBaseName, componentNames );
	}

	static String findAvailableName( String baseName, Iterable<String> occupiedNames )
	{
		int componentNumber = 1;
		while( true )
		{
			String suggestedName = baseName + " " + componentNumber;
			if( !Iterables.contains( occupiedNames, suggestedName ) )
			{
				return suggestedName;
			}
			componentNumber++;
		}
	}

	static Iterable<String> getComponentNames( List<CanvasItem> canvasList )
	{
		Function<CanvasItem, Collection<? extends ComponentItem>> getComponents = new Function<CanvasItem, Collection<? extends ComponentItem>>()
		{
			@Nullable
			@Override
			public Collection<? extends ComponentItem> apply( @Nullable CanvasItem canvas )
			{
				return canvas.getComponents();
			}
		};

		return transform( concat( transform( canvasList, getComponents ) ), getLabel );
	}
}
