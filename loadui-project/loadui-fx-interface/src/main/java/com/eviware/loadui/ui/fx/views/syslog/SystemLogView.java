package com.eviware.loadui.ui.fx.views.syslog;

import com.google.common.base.Strings;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;

public class SystemLogView extends ListView<LoggingEventWrapper>
{
	public static final int MAX_NUMBER_OF_ROWS = 200;

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger( SystemLogView.class );

	public SystemLogView()
	{
		Logger.getLogger( "com.eviware.loadui" ).addAppender( new SystemLogAppender() );
		Logger.getLogger( "sys.err" ).addAppender( new SysErrorAppender() );
		System.setErr(new PrintStream(new LoggingOutputStream( Logger.getLogger( "sys.err" ) ,Level.ERROR),true));
		getStyleClass().add( "system-log" );
	}

	public void copyAllRowsToClipboard()
	{
		String allRowsAsText = extractContentToString();
		copyToClipboard( allRowsAsText );
		log.warn( "Copied all rows to system clipboard" );
	}

	private void copyToClipboard( String text )
	{
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();
		content.putString( text );
		clipboard.setContent( content );
	}

	private String extractContentToString()
	{
		StringBuilder sb = new StringBuilder();
		for( LoggingEventWrapper eventWrapper : getItems() )
		{
			sb.append( eventWrapper.toString() ).append( System.getProperty("line.separator") );
		}
		return sb.toString();
	}

	public void clear()
	{
		getItems().clear();
	}

	public void initialize()
	{
		MenuItem copyItem = new MenuItem( "Copy Selected" );
		copyItem.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				copyToClipboard( getSelectionModel().getSelectedItem().toString() );
			}
		}
		);
		MenuItem copyAllItem = new MenuItem( "Copy All" );
		copyAllItem.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				copyAllRowsToClipboard();
			}
		}
		);
		MenuItem clearItem = new MenuItem( "Clear" );
		clearItem.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				clear();
			}
		}
		);
		setContextMenu( ContextMenuBuilder.create().items( copyItem, copyAllItem, clearItem ).build() );
	}

	private void limitNumberOfRows()
	{
		while( getItems().size() > MAX_NUMBER_OF_ROWS )
			getItems().remove( 0 );
	}

	class SystemLogAppender extends AppenderSkeleton
	{
		SystemLogAppender()
		{
			setThreshold( Level.toLevel( System.getProperty( "loadui.log.level", "INFO" ) ) );
		}

		@Override
		protected void append( final LoggingEvent event )
		{
			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{

					LoggingEventWrapper loggingEventWrapper = new LoggingEventWrapper( event );

					if( event.getThrowableInformation() != null )
					{
						StringBuilder stackTrace = new StringBuilder();
						Throwable t = event.getThrowableInformation().getThrowable();
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter( sw );
						t.printStackTrace( pw );
						StringTokenizer st = new StringTokenizer( sw.toString(), "\r\n" );
						while( st.hasMoreElements() )
							stackTrace.append( "   " ).append( st.nextElement() );

						loggingEventWrapper.addAdditionalInfo( sw.toString() );
					}

					getItems().add( loggingEventWrapper );

					limitNumberOfRows();
				}
			} );
		}

		@Override
		public void close()
		{
			// Nothing to do.
		}

		@Override
		public boolean requiresLayout()
		{
			return false;
		}
	}

	class SysErrorAppender extends AppenderSkeleton
	{
		@Override
		protected void append( final LoggingEvent event )
		{
			if( event.getMessage().toString().replace( "\r\n", "" ).equals( "" ) )
			{
				return;
			}

			Platform.runLater( new Runnable()
			{
				@Override
				public void run()
				{

					LoggingEventWrapper loggingEventWrapper = new LoggingEventWrapper( event );
					getItems().add( loggingEventWrapper );
					limitNumberOfRows();
				}
			} );
		}

		@Override
		public void close()
		{
			// Nothing to do.
		}

		@Override
		public boolean requiresLayout()
		{
			return false;
		}
	}
}
