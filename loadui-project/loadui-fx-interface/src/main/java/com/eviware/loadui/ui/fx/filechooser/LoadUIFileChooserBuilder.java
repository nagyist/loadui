package com.eviware.loadui.ui.fx.filechooser;

import com.eviware.loadui.api.model.WorkspaceItem;
import javafx.stage.FileChooser;

import java.util.Arrays;

/**
 * Author: maximilian.skog
 * Date: 2013-10-02
 * Time: 15:19
 */
public class LoadUIFileChooserBuilder implements LoadUIFileChooserBuilderExtensionFilter
{
	private java.util.Collection<? extends javafx.stage.FileChooser.ExtensionFilter> extensionFilters;
	private WorkspaceItem workspace;

	private java.lang.String title;

	protected LoadUIFileChooserBuilder( WorkspaceItem workspace )
	{
		this.workspace = workspace;
	}

	public static LoadUIFileChooserBuilderExtensionFilter usingWorkspace( WorkspaceItem workspace )
	{
		return new LoadUIFileChooserBuilder( workspace );
	}

	public LoadUIFileChooserBuilder extensionFilters( java.util.Collection<? extends javafx.stage.FileChooser.ExtensionFilter> extensionFilters )
	{
		this.extensionFilters = extensionFilters;

		return this;
	}

	public LoadUIFileChooserBuilder extensionFilters( javafx.stage.FileChooser.ExtensionFilter... extensionFilters )
	{
		this.extensionFilters = Arrays.asList( extensionFilters );

		return this;
	}

	public LoadUIFileChooserBuilder title( java.lang.String s )
	{
		this.title = s;

		return this;
	}

	public LoadUIFileChooser build()
	{
		LoadUIFileChooser fileChooser = new LoadUIFileChooser( workspace );

		if( title != null )
		{
			fileChooser.setTitle( title );
		}

		if( extensionFilters != null )
		{
			for( FileChooser.ExtensionFilter filter : extensionFilters )
			{
				fileChooser.getExtensionFilters().add( filter );
			}
		}


		return fileChooser;
	}

}
