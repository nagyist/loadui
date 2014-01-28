package com.eviware.loadui.launcher;

import com.eviware.loadui.LoadUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoadUIStarter
{

	public static void main( String[] args )
	{
		printLoadUIASCIILogo();

		for( String arg : args )
		{
			if( arg.contains( "cmd" ) )
			{
				launchCmdRunner( args, arg );
				return;
			}
		}

		launchFxApp( args );
	}

	private static void launchCmdRunner( String[] args, String arg )
	{
		List<String> argList = new ArrayList<>( Arrays.asList( args ) );
		argList.remove( arg );
		String[] newArgs = argList.toArray( new String[argList.size()] );
		new CommandLineStarter().launch( newArgs );
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


}
