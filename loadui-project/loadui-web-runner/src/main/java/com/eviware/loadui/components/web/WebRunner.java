package com.eviware.loadui.components.web;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.impl.component.categories.RunnerBase;

public class WebRunner extends RunnerBase
{

	public WebRunner( ComponentContext context )
	{
		super( context );
	}

	@Override
	protected TerminalMessage sample( TerminalMessage triggerMessage, Object sampleId ) throws SampleCancelledException
	{
		return triggerMessage;
	}

	@Override
	protected int onCancel()
	{
		//TODO implement
		return 0;
	}

}
