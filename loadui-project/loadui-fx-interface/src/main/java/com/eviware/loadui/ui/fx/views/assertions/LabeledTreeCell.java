/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.views.assertions;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.util.statistics.StatisticNameFormatter;

import javafx.event.EventHandler;
import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseEvent;

public class LabeledTreeCell extends TreeCell<Labeled> {
    public static LabeledTreeCell newInstance() {
        final LabeledTreeCell cell = new LabeledTreeCell();
        cell.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() > 1)
                    cell.fireEvent(IntentEvent.create(IntentEvent.INTENT_SAVE, ConfirmationDialog.class));
            }
        });
        return cell;
    }

    private LabeledTreeCell() {

    }

    @Override
    public void updateItem(Labeled item, boolean empty) 
	 {
        super.updateItem(item, empty);

        if (empty) 
		  {
            setText(null);
        } 
		  else 
		  {
			  if( item instanceof Statistic<?> || item instanceof StatisticWrapper )
			  {
				  setText( StatisticNameFormatter.format( item.getLabel() ) );
				  setId( UIUtils.toCssId( StatisticNameFormatter.format( item.getLabel() ) ) );

			  }
			  else
			  {
				  setText( item.getLabel() );
				  setId( UIUtils.toCssId ( item.getLabel() ) );
			  }
		  }
    }
}
