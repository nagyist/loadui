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
	 * Where should the project file be put.
	 * If not used then the project file will end up in the systems tmp-folder.
	 *
	 * @param where
	 * @return
	 */
	public ProjectBuilder where( File where );

	/**
	 * The requests limit of the project
	 *
	 * @param requests
	 * @return
	 */
	public ProjectBuilder requestsLimit( Long requests );

	/**
	 * The time limit of the execution of this project in seconds
	 *
	 * @param seconds
	 * @return
	 */
	public ProjectBuilder timeLimit( Long seconds );

	/**
	 * The number of assertions that should fail before this project stops.
	 *
	 * @param assertionFailures
	 * @return
	 */
	public ProjectBuilder assertionLimit( Long assertionFailures );

	/**
	 * Label of the project.
	 * @param name
	 * @return
	 */
	public ProjectBuilder label( String name );

	/**
	 * Saves and imports the project to the default workspace.
	 * @return
	 */
	public ProjectBuilder importProject( boolean bool );


	public ProjectRef build();
}
