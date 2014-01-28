package com.eviware.loadui.launcher;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.launcher.server.ServerStarter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoadUIStarter
{

	public static void main( String[] args )
	{
		printLoadUIASCIILogo();
		System.out.println( "Starting with arguments: " + Arrays.toString( args ) );

		for( String arg : args )
		{
			if( arg.equals( "--cmd=true" ) )
			{
				launchCmdRunner( args, arg );
				return;
			}
			if( arg.equals( "--server=true" ) )
			{
				launchServer( args, arg );
				return;
			}
		}

		launchFxApp( args );
	}

	private static void launchServer( String[] args, String arg )
	{
		new ServerStarter().launch( removeItem( args, arg ) );
	}

	private static void launchCmdRunner( String[] args, String arg )
	{
		new CommandLineStarter().launch( removeItem( args, arg ) );
	}

	private static void launchFxApp( String[] args )
	{
		new FxAppStarter().launch( args );
	}

	private static void printLoadUIASCIILogo()
	{
		System.out.println(
				"                                                 \n" +
						"                    .:::.                        \n" +
						"                  .==:::::.                      \n" +
						"                .====:::::::.                    \n" +
						"              .======:::::::::.                  \n" +
						"            .========:::::::::::.                \n" +
						"          .:=========:::::::::::::.              \n" +
						"        .:::=========:::::::::::::::.            \n" +
						"       :::::=========:::::::::::::::::           \n" +
						"       :::::=========:::::::::::::::::           \n" +
						"        ':::=========:::::::::::::::'            \n" +
						"          ':======================'              \n" +
						"            '==================='                \n" +
						"              '==============='                  \n" +
						"                '==========='                    \n" +
						"                  ':::::::'                      \n" +
						"                    ':::'                        \n" +
						"                                                 \n" +
						"                                                 \n" +
						" ::                       ::  ::   ::  ::::      \n" +
						" ::                       ::  ::   ::   ::       \n" +
						" ::      ::::   ::::: ::::::  ::   ::   ::       \n" +
						" ::     ::  :: ::  :: ::  ::  ::   ::   ::       \n" +
						" ::::::  ::::   ::: : ::::::   :::::   ::::      \n" +
						"\n" +
						"     	        LoadUI " + LoadUI.version() + "\n\n"
		);
	}

	private static String[] removeItem( String[] args, String arg )
	{
		List<String> argList = new ArrayList<>( Arrays.asList( args ) );
		argList.remove( arg );
		return argList.toArray( new String[argList.size()] );
	}

}
