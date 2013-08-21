package com.eviware.loadui.ui.fx.util.test;

import static com.google.common.collect.Iterables.get;

import java.util.Collection;

import com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component;

import javafx.scene.Node;
import org.loadui.testfx.GuiTest;

public class ComponentHandle
{
	public final Collection<Node> inputs;
	public final Collection<Node> outputs;
	private final GuiTest controller;
	private final LoadUiRobot robot;

	ComponentHandle( Collection<Node> inputs, Collection<Node> outputs, GuiTest controller, LoadUiRobot robot )
	{
		this.inputs = inputs;
		this.outputs = outputs;
		this.controller = controller;
		this.robot = robot;
	}

	public void to( Component otherComponent )
	{
		ComponentHandle handle = robot.createComponent( otherComponent );
		controller.drag( get( outputs, 0 ) ).to( get( handle.inputs, 0 ) );
	}
}
