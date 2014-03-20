package com.eviware.loadui.components.web;

import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.impl.component.categories.RunnerBase;
import com.eviware.loadui.util.component.RunnerTestBase;
import com.eviware.loadui.util.html.HtmlAssetScraper;
import org.junit.Test;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebRunnerTest extends RunnerTestBase
{
	WebRunner runner;
	HtmlAssetScraper mockScraper;
	RequestRunnerProvider mockReqRunnerProvider;
	RequestRunner mockReqRunner;

	@Override
	public WebRunner provideBehavior()
	{
		mockScraper = mock( HtmlAssetScraper.class );
		mockReqRunner = mock( RequestRunner.class );
		mockReqRunnerProvider = mock( RequestRunnerProvider.class );

		when( mockReqRunnerProvider.provideRequestRunner( contextSpy, anyCollection() ) )
				.thenReturn( mockReqRunner );

		runner = new WebRunner( contextSpy, mockScraper, mockReqRunnerProvider );
		return runner;
	}

	@Test( expected = RunnerBase.SampleCancelledException.class )
	public void throwsWhenNoUrlGiven() throws RunnerBase.SampleCancelledException
	{
		runner.sample( mock( TerminalMessage.class ), 0L );
	}

	@Test( expected = RunnerBase.SampleCancelledException.class )
	public void throwsWhenBadUrlGiven() throws RunnerBase.SampleCancelledException, InterruptedException
	{
		setProperty( WebRunner.WEB_PAGE_URL_PROP, "x://bad url" );
		Thread.sleep( 150 );
		runner.sample( mock( TerminalMessage.class ), 0L );
	}

}
