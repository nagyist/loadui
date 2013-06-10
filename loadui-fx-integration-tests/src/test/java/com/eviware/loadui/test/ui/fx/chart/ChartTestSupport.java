package com.eviware.loadui.test.ui.fx.chart;

import com.eviware.loadui.ui.fx.util.test.TestFX;
import javafx.scene.Node;

import java.util.Set;

public class ChartTestSupport
{
	public static Set<Node> allChartLines()
	{
		return TestFX.findAll("LineSegmentView");
	}
}
