package com.eviware.loadui.util.messaging;

import com.eviware.loadui.api.messaging.ConnectionListener;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author renato
 */
public abstract class MessageEndPointBase implements MessageEndpoint
{

	final static Logger log = LoggerFactory.getLogger( MessageEndPointBase.class );

	public static final String SERVICE_INIT = "/service/init";
	public static final String SERVICE_CLOSE = "/service/close";
	protected static final Message CLOSE_MESSAGE = new Message( SERVICE_CLOSE, null );

	protected final LinkedBlockingQueue<Message> outMessageQueue = Queues.newLinkedBlockingQueue();
	private final Set<ConnectionListener> connectionListeners = Sets.newCopyOnWriteArraySet();
	protected final ChannelRoutingSupport routingSupport;
	private Thread messageReceiverThread;
	private Thread messageSenderThread;
	private boolean isInit = false;


	protected MessageEndPointBase( ChannelRoutingSupport routingSupport )
	{
		this.routingSupport = routingSupport;
	}

	@Override
	public void sendMessage( String channel, Object data )
	{
		outMessageQueue.add( new Message( channel, data ) );
	}

	@Override
	public void addMessageListener( String channel, MessageListener listener )
	{
		routingSupport.addMessageListener( channel, listener );
	}

	@Override
	public void removeMessageListener( MessageListener listener )
	{
		routingSupport.removeMessageListener( listener );
	}

	@Override
	public void addConnectionListener( ConnectionListener listener )
	{
		connectionListeners.add( listener );
	}

	@Override
	public void removeConnectionListener( ConnectionListener listener )
	{
		connectionListeners.remove( listener );
	}

	public ObjectOutput provideObjectOutputStream( Socket socket ) throws ExceptionInInitializerError
	{
		try
		{
			return new ObjectOutputStream( socket.getOutputStream() );
		}
		catch( IOException e )
		{
			throw new ExceptionInInitializerError( e );
		}
	}

	public ObjectInput provideObjectInputStream( Socket socket ) throws ExceptionInInitializerError
	{
		try
		{
			return new ObjectInputStream( socket.getInputStream() );
		}
		catch( IOException e )
		{
			throw new ExceptionInInitializerError( e );
		}
	}

	protected Message waitForNextMessage( ObjectInput oi )
	{
		try
		{
			Message msg = readMessageFrom( oi );
			log.debug( "Read message on channel '{}': '{}'", msg.channel, msg.data );
			return msg;
		}
		catch( InterruptedException ie )
		{
			log.warn( "MessageReceiver interrupted while waiting for a new message" );
		}
		catch( Exception e )
		{
			log.warn( "Problem trying to read message, ignoring error", e );
		}
		return null;
	}

	public Message readMessageFrom( ObjectInput objectInput )
			throws IOException, ClassNotFoundException, InterruptedException
	{
		return new Message( objectInput.readUTF(), objectInput.readObject() );
	}

	public void writeMessage( ObjectOutput oo, Message message )
	{
		try
		{
			oo.writeUTF( message.channel );
			oo.writeObject( message.data );
			oo.flush();
		}
		catch( IOException e )
		{
			log.error( "Sending of message '{}' failed due to {}", message.data, e );
		}
	}

	protected boolean shouldContinueListening( Message message )
	{
		return message != null && !CLOSE_MESSAGE.channel.equals( message.channel );
	}

	protected void informConnectionListenersConnectionStatusIs( boolean isUp )
	{
		for( ConnectionListener listener : connectionListeners )
		{
			try
			{
				listener.handleConnectionChange( this, isUp );
			}
			catch( Exception e )
			{
				log.warn( "Connection listener threw Exception", e );
			}
		}
	}

	private void requestClose()
	{
		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				close();
			}
		} ).start();
	}

	protected void startMessageSenderAndReceiverWith( Socket socket )
	{
		messageReceiverThread = new Thread( new MessageReceiver( socket ) );
		messageSenderThread = new Thread( new MessageSender( socket ) );
		isInit = true;
		messageReceiverThread.start();
		messageSenderThread.start();
	}

	protected void interruptMessageThreadsAndWaitForThemToDie()
	{
		if( !isInit )
			return;

		messageReceiverThread.interrupt();
		messageSenderThread.interrupt();

		try
		{
			final int maxWait = 500;
			messageReceiverThread.join( maxWait );
			messageSenderThread.join( maxWait );
		}
		catch( InterruptedException e )
		{
			log.debug( "Problem waiting for threads to die" );
		}
		finally
		{
			isInit = false;
		}
	}

	protected abstract void onMessageSenderLoopStarted();

	protected abstract boolean initializeMessageReceiver( ObjectInput objectInput );


	protected class MessageReceiver implements Runnable
	{
		private final Socket socket;

		public MessageReceiver( Socket socket )
		{
			this.socket = socket;
		}

		@Override
		public void run()
		{
			try( ObjectInput oi = provideObjectInputStream( socket ) )
			{
				log.debug( "Starting MessageReceiver loop" );
				boolean initOk = initializeMessageReceiver( oi );

				if( initOk )
				{
					Message incomingMsg;
					while( shouldContinueListening( incomingMsg = waitForNextMessage( oi ) ) )
					{
						routingSupport.fireMessage( incomingMsg.channel, MessageEndPointBase.this, incomingMsg.data );
					}
					log.info( "Received signal to close connection to {}", socket.getLocalSocketAddress() );
				}
				else
				{
					log.warn( "Could not initialize the MessageReceiver!" );
				}

			}
			catch( IOException e )
			{
				log.error( "Problem closing connection", e );
			}
			catch( ExceptionInInitializerError e )
			{
				log.error( "Could not create or initialize InputStream for MessageReceiver" );
			}
			finally
			{
				informConnectionListenersConnectionStatusIs( false );
				requestClose();
			}
		}

	}

	protected class MessageSender implements Runnable
	{

		private final Socket socket;

		public MessageSender( Socket socket )
		{
			this.socket = socket;
		}

		@Override
		public void run()
		{
			Message message;
			try( ObjectOutput oo = provideObjectOutputStream( socket ) )
			{
				onMessageSenderLoopStarted();

				informConnectionListenersConnectionStatusIs( true );

				log.debug( "Starting MessageSender loop" );
				do
				{
					message = outMessageQueue.take();
					log.debug( "Sending out message: '{}: {}'", message.channel, message.data );
					writeMessage( oo, message );
				}
				while( message != CLOSE_MESSAGE );
			}
			catch( InterruptedException e )
			{
				log.error( "MessageSender loop interrupted, aborting connection", e );
			}
			catch( ExceptionInInitializerError e )
			{
				log.error( "Could not create or initialize OutputStream for MessageSender" );
			}
			catch( IOException e )
			{
				log.info( "Problem closing OutputStream: {}", e.getMessage() );
			}
			finally
			{
				requestClose();
			}
		}

	}

	protected static class Message
	{
		public final String channel;
		public final Object data;

		public Message( String channel, Object data )
		{
			this.channel = channel;
			this.data = data;
		}
	}

}
