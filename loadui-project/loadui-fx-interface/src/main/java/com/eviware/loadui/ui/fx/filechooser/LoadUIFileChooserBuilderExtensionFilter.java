package com.eviware.loadui.ui.fx.filechooser;

/**
 * Author: maximilian.skog
 * Date: 2013-10-04
 * Time: 11:57
 */
public interface LoadUIFileChooserBuilderExtensionFilter
{
	public LoadUIFileChooserBuilder extensionFilters( java.util.Collection<? extends javafx.stage.FileChooser.ExtensionFilter> extensionFilters );

	public LoadUIFileChooserBuilder extensionFilters( javafx.stage.FileChooser.ExtensionFilter... extensionFilters );

}
