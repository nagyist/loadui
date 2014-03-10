package com.eviware.loadui.components.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.TEXT_HTML;

/**
 * @author renato
 */
@Path( "tests" )
public class ServerForTests
{
	public static final String BASIC_GET_RESPONSE = "Hello Tester";

	@GET
	@Produces( TEXT_HTML )
	public String get()
	{
		return BASIC_GET_RESPONSE;
	}


}
