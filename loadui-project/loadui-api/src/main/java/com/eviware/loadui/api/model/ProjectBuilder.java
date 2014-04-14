package com.eviware.loadui.api.model;

import java.io.File;

public interface ProjectBuilder
{
	/**
	 * Creates a new Builder.
	 * @return
	 */
	public ProjectBlueprint create();

	public interface ProjectBlueprint{
		/**
		 * Where should the project file be put.
		 * If not used then the project file will end up in the systems tmp-folder.
		 *
		 * @param where the directory where project files will be saved
		 * @return
		 */
		public ProjectBlueprint where( File where );

		/**
		 * Add additional components to this project by using component blueprints.
		 * @return
		 */
		public ProjectBlueprint components( ComponentBlueprint... components );

		/**
		 * Should the project be imported into the current instance of LoadUI or not.
		 * It is not imported by default, and projectRef that is produced by build is if project is not imported.
		 *
		 * @param bool
		 * @return
		 */
		public ProjectBlueprint importProject( boolean bool );

		/**
		 * The requests limit of the project
		 *
		 * @param requests
		 * @return
		 */
		public ProjectBlueprint requestsLimit( Long requests );

		/**
		 * The time limit of the execution of this project in seconds
		 *
		 * @param seconds
		 * @return
		 */
		public ProjectBlueprint timeLimit( Long seconds );

		/**
		 * The number of assertions that should fail before this project stops.
		 *
		 * @param assertionFailures
		 * @return
		 */
		public ProjectBlueprint assertionLimit( Long assertionFailures );

		/**
		 * Creates a LoadUI Scenario
		 * @param label
		 * @return
		 */
		public ProjectBlueprint scenario( String label );

		/**
		 * Label of the project.
		 * @param name
		 * @return
		 */
		public ProjectBlueprint label( String name );


		public ProjectRef build();

	}
}
