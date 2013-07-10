package com.eviware.loadui.ui.fx.util;

import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.Stage;

public class EventUtils
{

	public static IntentForwardingBuilder forwardIntentsFrom( final Stage sourceStage )
	{
		return new IntentForwardingBuilder( sourceStage );
	}

	public static class IntentForwardingBuilder
	{
		private Stage sourceStage;

		IntentForwardingBuilder( Stage sourceStage )
		{
			this.sourceStage = sourceStage;
		}

		public void to( final Node target )
		{
			sourceStage.addEventHandler( IntentEvent.ANY, new EventHandler<IntentEvent<?>>()
			{
				@Override
				public void handle( IntentEvent<?> event )
				{
					target.fireEvent( event );
				}
			} );
		}
	}

}
