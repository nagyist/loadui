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
		 * @param where
		 * @return
		 */
		public ProjectBlueprint where( File where );

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
