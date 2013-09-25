package com.eviware.loadui.util.messaging;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author renato
 */
public abstract class OpenableMessageEndPoint extends MessageEndPointBase
{
	private final Executor openCloseExecutor = Executors.newSingleThreadExecutor();

	private final Runnable opener = new Runnable()
	{
		@Override
		public void run()
		{
			doOpen();
		}
	};

	private final Runnable closer = new Runnable()
	{
		@Override
		public void run()
		{
			doClose();
		}
	};

	protected OpenableMessageEndPoint( ChannelRoutingSupport routingSupport )
	{
		super( routingSupport );
	}

	@Override
	public void open()
	{
		openCloseExecutor.execute( opener );
	}

	@Override
	public void close()
	{
		openCloseExecutor.execute( closer );
	}

	protected abstract void doOpen();

	protected abstract void doClose();

}
