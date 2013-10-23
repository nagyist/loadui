package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.CanvasItem;
import com.google.code.tempusfugit.temporal.Condition;

/**
 * @author renato
 */
public class IsCanvasRunning implements Condition
{
	final CanvasItem canvasItem;
	final boolean isRunning;

	public IsCanvasRunning( CanvasItem canvasItem, boolean isRunning )
	{
		this.canvasItem = canvasItem;
		this.isRunning = isRunning;
	}

	@Override
	public boolean isSatisfied()
	{
		return canvasItem.isRunning() == isRunning;
	}
}
