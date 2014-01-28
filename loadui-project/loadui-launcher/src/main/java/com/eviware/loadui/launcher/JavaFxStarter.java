package com.eviware.loadui.launcher;

import com.eviware.loadui.LoadUI;
import javafx.application.Application;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.logging.Logger;

public abstract class JavaFxStarter
{

	static final Logger log = Logger.getLogger( JavaFxStarter.class.getName() );

	public JavaFxStarter()
	{
		startJavaFxEnv();
	}

	public void launch( String[] args )
	{
		Application.launch( applicationClass(), args );
	}

	protected abstract Class<? extends Application> applicationClass();

	private static void startJavaFxEnv()
	{
		ensureFontsAvailableForJavaFX();
	}

	private static void ensureFontsAvailableForJavaFX()
	{
		if( !isJavaFXFontsAvailable() )
		{
			log.info( "Preparing fonts for JavaFX2" );
			if( installTrueTypeFont() )
			{
				log.info( "Restarting JRE for changes to take effect." );
				LoadUI.restart();
			}
			else
			{
				System.err.println( "Failed to install TrueType fonts\nLoadUI depends on JavaFX that depends on TrueType fonts to work." );
				System.exit( -1 );
			}
		}
	}

	private static boolean isJavaFXFontsAvailable()
	{
		try
		{
			//If a Unix-based System is lacking TrueType fonts for JavaFX this causes a NullPointerException
			//This is because Oracle took a decision not to support Type-1 Post-script Fonts for JavaFX.
			//Many components give a call to this method, so we need to support it by installing fonts.
			Font.getDefault();
			return true;
		}
		catch( NullPointerException e )
		{
			return false;
		}
	}

	private static boolean installTrueTypeFont()
	{
		String workingDir = LoadUI.getWorkingDir().getAbsolutePath();
		workingDir = workingDir.substring( 0, workingDir.length() - 1 );
		String userHome = System.getProperty( "user.home" );

		try
		{
			ProcessBuilder pb = new ProcessBuilder();

			Process currentProcess = pb.command( "/bin/mkdir", "-p", userHome + "/.fonts" ).start();
			currentProcess.waitFor();

			currentProcess = pb.command( "/bin/cp", "-rf", workingDir + "jre/lib/fonts", userHome + "/.fonts" ).start();
			currentProcess.waitFor();

			currentProcess = pb.command( "/usr/bin/fc-cache" ).start();
			currentProcess.waitFor();

			return true;
		}
		catch( InterruptedException | IOException e )
		{
			log.warning( "Unable to install LoadUI fonts on local system\n" );
			e.printStackTrace();
			return false;
		}
	}

}
