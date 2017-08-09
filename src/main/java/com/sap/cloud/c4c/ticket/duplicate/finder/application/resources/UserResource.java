package com.sap.cloud.c4c.ticket.duplicate.finder.application.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.c4c.ticket.duplicate.finder.application.user.User;
import com.sap.cloud.c4c.ticket.duplicate.finder.application.user.UserService;
import com.sap.cloud.c4c.ticket.duplicate.finder.application.user.UserServiceException;

@Path("user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

	@Context
	private HttpServletRequest httpRequest;

	private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

	@GET
	@Path("current")
	public Response getUserAttributes() {

		if (httpRequest.getUserPrincipal() == null) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(new InfoMessage(Response.Status.INTERNAL_SERVER_ERROR, "There is no logged in user."))
					.build();
		}

		try {
			User loggedInUser = UserService.getLoggedInUser();
			return Response.status(Response.Status.OK).entity(loggedInUser).build();
		} catch (UserServiceException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(new InfoMessage(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage())).build();
		}
	}

	@POST
	@Path("logout")
	public Response logout() {

		try {
			UserService.logoutUser(httpRequest);
			return Response.status(Response.Status.OK).build();
		} catch (UserServiceException e) {
			LOGGER.error(e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
