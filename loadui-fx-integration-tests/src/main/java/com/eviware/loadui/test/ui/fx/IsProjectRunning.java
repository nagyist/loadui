package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.ProjectItem;
import com.google.code.tempusfugit.temporal.Condition;

/**
 * @author renato
 */
public class IsProjectRunning implements Condition
{
	final ProjectItem projectItem;
	final boolean isRunning;

	public IsProjectRunning( ProjectItem projectItem, boolean isRunning )
	{
		this.projectItem = projectItem;
		this.isRunning = isRunning;
	}

	@Override
	public boolean isSatisfied()
	{
		return projectItem.isRunning() == isRunning;
	}
}
