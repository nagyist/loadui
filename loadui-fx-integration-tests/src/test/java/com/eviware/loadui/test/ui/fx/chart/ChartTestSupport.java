package com.eviware.loadui.test.ui.fx.chart;

import org.loadui.testfx.GuiTest;
import javafx.scene.Node;

import java.util.Set;

public class ChartTestSupport
{
	public static Set<Node> allChartLines()
	{
		return GuiTest.findAll( ".line-segment-view" );
	}
}
