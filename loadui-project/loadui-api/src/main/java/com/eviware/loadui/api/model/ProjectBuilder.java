package com.eviware.loadui.api.model;
/*
 * Copyright 2013 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */

import java.io.File;

/**
 * A LoadUI project-builder should be able to create a blank ProjectItemImpl
 * using nothing and can be used externally to create a project with any type of setup.
 */
public interface ProjectBuilder
{
	/**
	 * Creates a builder object to start rolling out canvasItems.
	 * Project-File will be created as a tmp-file.
	 * @return
	 */
	public ProjectBuilder create();

	/**
	 *
	 * @param where will the project be placed, if wanted.
	 * @return
	 */
	public ProjectBuilder create( File where);

	/**
	 * Creates a fixed rate generator.
	 * @param rate the number of requests per second.
	 * @return
	 */
	public ProjectBuilder fixedRate( int rate );

	/**
	 * Creates a webrunner on canvas.
	 * @return
	 */
	public ProjectBuilder webRunner( String url );

	/**
	 * connects the last terminal of the previous added component to the first terminal of the next one.
	 * @return
	 */
	public ProjectBuilder connectTo();

	/**
	 * Connects the A:st terminal of the previous component to the B:st terminal of the next component.
	 */
	public ProjectBuilder connectTo(int a, int b);

	/**
	 * Names the project. Otherwise it will be called by its filename.
	 * @param newLabel The name of the project
	 * @return
	 */
	public ProjectBuilder label( String newLabel );

	/**
	 * returns the built project using the builder object.
	 * @return
	 */
	public ProjectItem build();
}
