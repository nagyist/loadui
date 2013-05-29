package com.eviware.loadui.test.ui.fx.tablelog;

import javafx.scene.Node;

import java.util.Set;

import static com.eviware.loadui.ui.fx.util.test.TestFX.findAll;

public class TableLogTestSupport
{
	public static Set<Node> tableRows()
	{
		return findAll(".component-view .table-row-cell");
	}
}
