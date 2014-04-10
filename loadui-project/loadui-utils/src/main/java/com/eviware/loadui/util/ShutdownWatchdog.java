package com.eviware.loadui.util;

import java.util.concurrent.TimeUnit;

public class ShutdownWatchdog
{
	public static void killJvmIn( final int number, final TimeUnit unit )
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep( unit.toMillis( number ) );
				} catch( InterruptedException e )
				{
					e.printStackTrace();
				}
				System.out.println("Shutdown timed out, forcing close...");
				System.exit(0);
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
}
