package com.eviware.loadui.test.ui.fx.tablelog;

import javafx.scene.Node;

import java.util.Set;

import static com.eviware.loadui.ui.fx.util.test.TestFX.findAll;
import static org.junit.Assert.assertTrue;

class TableLogTestSupport
{
	static Set<Node> tableRows()
	{
		return findAll(".component-view .table-row-cell");
	}


	static void testRunStopsWithinLimit( long startT, long limit )
	{
		assertTrue( System.currentTimeMillis() - startT < limit );
	}
}
