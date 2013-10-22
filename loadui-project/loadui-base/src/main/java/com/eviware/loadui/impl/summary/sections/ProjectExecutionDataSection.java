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
package com.eviware.loadui.impl.summary.sections;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.summary.MutableSummary;
import com.eviware.loadui.impl.model.canvas.SceneItemImpl;
import com.eviware.loadui.impl.model.canvas.project.ProjectItemImpl;
import com.eviware.loadui.impl.summary.MutableSectionImpl;
import com.eviware.loadui.impl.summary.sections.tablemodels.TestCaseDataTableModel;
import com.eviware.loadui.util.summary.CalendarUtils;

import javax.swing.table.TableModel;

import static com.eviware.loadui.impl.summary.sections.tablemodels.TestCaseDataTableModel.TestCaseDataModel;

final public class ProjectExecutionDataSection extends MutableSectionImpl
{

	private final ProjectItemImpl project;
	private final MutableSummary summary;

	public ProjectExecutionDataSection( ProjectItemImpl projectItemImpl, MutableSummary summary )
	{

		super( "Execution Data" );
		project = projectItemImpl;
		this.summary = summary;
		addValue( "Duration", getExecutionTime() );
		addValue( "Start Time", getStartTime() );
		addValue( "End Time", getEndTime() );
		addValue( "Total number of requests", getTotalNumberOfRequests() );
		addValue( "Total number of failed requests", getTotalNumberOfFailedRequests() );
		addValue( "Total number of assertions", getTotalNumberOfAssertions() );
		addValue( "Total number of failed assertions", getTotalNumberOfFailedAssertions() );

		addTable( "Scenario Data", getTestcaseDataTable() );
	}

	public TableModel getTestcaseDataTable()
	{
		TestCaseDataTableModel model = new TestCaseDataTableModel();
		for( SceneItemImpl testcase : project.getChildren() )
		{
			if( summary.getStartTime() != null && summary.getEndTime() != null )
				model.add( new TestCaseDataModel( testcase, summary ) );
		}
		return model;
	}

	public String getEndTime()
	{
		return CalendarUtils.formatAbsoluteTime( summary.getEndTime() );
	}

	public String getExecutionTime()
	{
		return CalendarUtils.formatInterval( summary.getStartTime(), summary.getEndTime() );
	}

	public String getStartTime()
	{
		return CalendarUtils.formatAbsoluteTime( summary.getStartTime() );
	}

	public String getTotalNumberOfAssertions()
	{
		return String.valueOf( project.getCounter( CanvasItem.ASSERTION_COUNTER ).get() );
	}

	public String getTotalNumberOfFailedAssertions()
	{
		return String.valueOf( project.getCounter( CanvasItem.ASSERTION_FAILURE_COUNTER ).get() );
	}

	public String getTotalNumberOfRequests()
	{
		return String.valueOf( project.getCounter( CanvasItem.SAMPLE_COUNTER ).get() );
	}

	public String getTotalNumberOfFailedRequests()
	{
		return String.valueOf( project.getCounter( CanvasItem.REQUEST_FAILURE_COUNTER ).get() );
	}
}
