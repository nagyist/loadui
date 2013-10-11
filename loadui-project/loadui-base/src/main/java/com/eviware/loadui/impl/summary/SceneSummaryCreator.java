package com.eviware.loadui.impl.summary;

import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.summary.MutableSummary;
import com.eviware.loadui.impl.model.canvas.SceneItemImpl;
import com.eviware.loadui.impl.summary.sections.*;

/**
 * @author renato
 */
public class SceneSummaryCreator extends SummaryCreator
{
	private final SceneItemImpl sceneItem;

	public SceneSummaryCreator( SceneItemImpl sceneItem )
	{
		this.sceneItem = sceneItem;
	}

	@Override
	public void appendToSummary( MutableSummary mutableSummary )
	{
		MutableChapterImpl chap = ( MutableChapterImpl )mutableSummary.addChapter( sceneItem.getLabel() );
		chap.addSection( new TestCaseDataSummarySection( sceneItem, mutableSummary ) );
		chap.addSection( new TestCaseExecutionDataSection( sceneItem, mutableSummary ) );
		chap.addSection( new TestCaseExecutionMetricsSection( sceneItem ) );
		chap.addSection( new TestCaseExecutionNotablesSection( sceneItem ) );
		chap.addSection( new TestCaseDataSection( sceneItem ) );
		chap.setDescription( sceneItem.getDescription() );

		for( ComponentItem component : sceneItem.getComponents() )
			component.generateSummary( chap );

	}

}
