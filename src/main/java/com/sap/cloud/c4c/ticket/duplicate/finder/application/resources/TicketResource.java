package com.sap.cloud.c4c.ticket.duplicate.finder.application.resources;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.c4c.ticket.duplicate.finder.application.TicketLoader;
import com.sap.cloud.c4c.ticket.duplicate.finder.application.TicketStorage;
import com.sap.cloud.c4c.ticket.duplicate.finder.application.model.Group;
import com.sap.cloud.c4c.ticket.duplicate.finder.application.model.Ticket;
import com.sap.cloud.c4c.ticket.duplicate.finder.c4c.client.C4CTicket;
import com.sap.cloud.c4c.ticket.duplicate.finder.c4c.client.C4CTicketNotFoundException;
import com.sap.cloud.c4c.ticket.duplicate.finder.c4c.client.C4CTicketService;
import com.sap.cloud.c4c.ticket.duplicate.finder.connectivity.InvalidResponseException;
import com.sap.cloud.c4c.ticket.duplicate.finder.index.IndexException;
import com.sap.cloud.c4c.ticket.duplicate.finder.index.IndexService;
import com.sap.cloud.c4c.ticket.duplicate.finder.index.TicketGroup;

@Path("ticket")
public class TicketResource {

    private static final String ERROR_MERGE_TICKETS = "Merging tickets failed.";
    private static final String ERROR_MISSING_PARAMS = "Missing required form params.";
    private static final String SUCCESSFUL_MERGE_MSG = "Tickets are merged successfully.";
    private static final String ERROR_TICKET_NOT_FOUNT = "Ticket with id={0} not found";
    private static final String ERROR_INTERNAL = "Internal server error.";
    private static final String EMPTY_STRING = "";
    private static final int NUMBER_OF_TICKETS = 20;

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketResource.class);

    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        Collection<Ticket> tickets = TicketStorage.findAll();
        return Response.status(Status.OK).entity(tickets).build();
    }

    @PUT
    @Path("fetch")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerFetchTickets() throws Exception {
        Collection<Ticket> tickets = TicketLoader.getTickets(NUMBER_OF_TICKETS);
        return Response.status(Status.OK).entity(tickets).build();
    }

    @POST
    @Path("merge")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response merge(@FormParam("currentId") String currentId, @FormParam("duplicateId") String duplicateId) {
        Status status = null;
        try {
            IndexService.merge(currentId, duplicateId);
            status = Status.OK;
        } catch (IndexException e) {
            status = Status.INTERNAL_SERVER_ERROR;
        } catch (IllegalArgumentException e) {
            status = Status.BAD_REQUEST;
        }
        return Response.status(status).entity(getMergeInfoMessageByResponseStatus(status)).build();
    }

    @GET
    @Path("search/{ticket_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response search(@PathParam("ticket_id") String ticketId) {
        Response response = null;
        try {
            Ticket ticket = TicketStorage.findById(ticketId);
            if (ticket == null) {
                ticket = loadTicket(ticketId);
            }

            List<Group> groups = IndexService.search(ticket).stream()
                    .map(TicketGroup::getTickets)
                    .map(ids -> ids.stream().map(TicketStorage::findById).collect(Collectors.toList()))
                    .map(tickets -> new Group(tickets))
                    .collect(Collectors.toList());

            response = Response.status(Response.Status.OK).entity(groups).build();
        } catch (C4CTicketNotFoundException | IOException | InvalidResponseException | IndexException e) {
            response = Response.status(Status.NOT_FOUND)
                    .entity(new InfoMessage(Status.NOT_FOUND, MessageFormat.format(ERROR_TICKET_NOT_FOUNT, ticketId)))
                    .build();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            response = Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(new InfoMessage(Status.INTERNAL_SERVER_ERROR, ERROR_INTERNAL)).build();
        }
        return response;
    }

    private InfoMessage getMergeInfoMessageByResponseStatus(Status status) {
        String message = EMPTY_STRING;
        switch (status) {
        case BAD_REQUEST:
            message = ERROR_MISSING_PARAMS;
            break;
        case INTERNAL_SERVER_ERROR:
            message = ERROR_MERGE_TICKETS;
            break;
        case OK:
            message = SUCCESSFUL_MERGE_MSG;
            break;
        default:
        }
        return new InfoMessage(status, message);
    }

    private Ticket loadTicket(String ticketId)
            throws IOException, InvalidResponseException, IndexException, C4CTicketNotFoundException {
        C4CTicket c4cTicket = C4CTicketService.retrieveC4CTicketByID(ticketId);
        Ticket ticket = Ticket.convertC4CTicket(c4cTicket);
        TicketStorage.save(ticket);
        IndexService.add(ticket);
        return ticket;
    }
}