package com.eviware.loadui.api.model;

import java.io.File;

/**
 * Created by osten on 1/16/14.
 */
public interface ProjectBuilder
{
	/**
	 * Creates a new Builder.
	 * @return
	 */
	public ProjectBuilder create();

	/**
	 * Creates chains of components for the project
	 * @return
	 */
	public ProjectBuilder components( ComponentItem... components);

	public ProjectBuilder where( File where );

	public ProjectBuilder label( String name );

	/**
	 * Saves and imports the project to the default workspace.
	 * @return
	 */
	public ProjectBuilder save();


	public ProjectItem build();
}
