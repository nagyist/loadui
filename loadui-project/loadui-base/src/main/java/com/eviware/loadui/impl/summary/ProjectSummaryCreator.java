package com.eviware.loadui.impl.summary;

import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.summary.MutableSummary;
import com.eviware.loadui.impl.model.canvas.SceneItemImpl;
import com.eviware.loadui.impl.model.canvas.project.ProjectItemImpl;
import com.eviware.loadui.impl.summary.sections.*;

/**
 * @author renato
 */
public class ProjectSummaryCreator extends SummaryCreator
{
	private final ProjectItemImpl projectItem;

	public ProjectSummaryCreator( ProjectItemImpl projectItem )
	{
		this.projectItem = projectItem;
	}

	@Override
	public void appendToSummary( MutableSummary mutableSummary )
	{
		// add a project chapter first
		MutableChapterImpl projectChapter = ( MutableChapterImpl )mutableSummary.addChapter( projectItem.getLabel() );

		// add and generate Scenario chapters if the Scenario has run at least
		// once.
		for( SceneItemImpl scene : projectItem.getChildren() )
		{
			if( mutableSummary.getEndTime() != null && mutableSummary.getStartTime() != null )
				scene.getSummaryCreator().appendToSummary( mutableSummary );
		}

		// fill project chapter
		projectChapter.addSection( new ProjectDataSummarySection( projectItem, mutableSummary ) );
		projectChapter.addSection( new ProjectExecutionDataSection( projectItem, mutableSummary ) );
		projectChapter.addSection( new ProjectExecutionMetricsSection( projectItem ) );
		projectChapter.addSection( new ProjectExecutionNotablesSection( projectItem ) );
		projectChapter.addSection( new ProjectDataSection( projectItem ) );
		projectChapter.setDescription( projectItem.getDescription() );

		for( ComponentItem component : projectItem.getComponents() )
			component.generateSummary( projectChapter );

	}

}
