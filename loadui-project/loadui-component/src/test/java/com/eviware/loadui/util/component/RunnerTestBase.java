package com.eviware.loadui.util.component;

import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import org.junit.Before;

public abstract class RunnerTestBase extends ComponentTestBase
{
	protected InputTerminal triggerTerminal;
	protected OutputTerminal resultsTerminal;

	public abstract RunnerCategory provideBehavior();

	@Before
	public void runnerTestBaseSetup()
	{
		RunnerCategory runner = (RunnerCategory) behavior;
		triggerTerminal = runner.getTriggerTerminal();
		resultsTerminal = runner.getResultTerminal();
		results = ctu.getMessagesFrom( resultsTerminal );
	}

	protected void triggerAndWait() throws InterruptedException
	{
		ctu.sendSimpleTrigger( triggerTerminal );
		getNextOutputMessage();
	}

}
