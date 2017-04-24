package com.sap.cloud.c4c.ticket.duplicate.finder.application.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("info")
public class AppInfoResource {
	
	@Context
	UriInfo uri;
	
	@GET
	@Path("api")
	public String getServerName(){
		return uri.getBaseUri().toString();
	}
}
