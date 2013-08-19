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
package com.eviware.loadui.ui.fx.views.analysis;

import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.util.TreeUtils.LabeledStringValue;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Selection
{
	private static final Logger log = LoggerFactory.getLogger( Selection.class );

	public final String source;
	public final String statistic;
	public final String variable;
	public final StatisticHolder holder;

	Selection( @Nonnull TreeItem<Labeled> selected, boolean selectedIsSource )
	{
		/* TODO: this needs to be fixed. It seems that the selectedIsSource also applies for JMX (tomcat, weblogic and JBoss).
		 * selectedIsSource is also dependent on if there is an agent in LoadUI (it does not check if it is connected or used in any way) (its wrong).
		 * in this if statement only !(selected.getParent().getParent().getValue() instanceof StatisticHolder) could work fine too.
		 */
		if( selectedIsSource || !( selected.getParent().getParent().getValue() instanceof StatisticHolder ) )
		{
			// selected is agent or tree is having 3 levels ex weblogic, tomcat
			source = ( ( LabeledStringValue )selected.getValue() ).getValue();
			statistic = selected.getParent().getValue().getLabel();
			variable = selected.getParent().getParent().getValue().getLabel();
			holder = ( StatisticHolder )selected.getParent().getParent().getParent().getValue();
		}
		else
		{
			// selected is statistic
			source = null;
			statistic = selected.getValue().getLabel();
			variable = selected.getParent().getValue().getLabel();
			holder = ( StatisticHolder )selected.getParent().getParent().getValue();
		}


		log.debug( "Selection = source:" + source + " statistic: " + statistic + " variable: " + variable + " holder:" + holder.getLabel() );
	}
}
