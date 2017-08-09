package com.sap.cloud.c4c.ticket.duplicate.finder.application.notification;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.c4c.ticket.duplicate.finder.application.TicketStorage;
import com.sap.cloud.c4c.ticket.duplicate.finder.application.model.Ticket;
import com.sap.cloud.c4c.ticket.duplicate.finder.c4c.client.C4CTicket;
import com.sap.cloud.c4c.ticket.duplicate.finder.c4c.client.C4CTicketNotFoundException;
import com.sap.cloud.c4c.ticket.duplicate.finder.c4c.client.C4CTicketService;
import com.sap.cloud.c4c.ticket.duplicate.finder.connectivity.InvalidResponseException;
import com.sap.cloud.c4c.ticket.duplicate.finder.index.IndexException;
import com.sap.cloud.c4c.ticket.duplicate.finder.index.IndexService;

@Path("notifications")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EventNotificationService {
	
	private static final String ERROR_ADDING_TICKET_TO_INDEXER = "Adding ticket to indexer failed.";
	private static final String ERROR_NOTIFICATION_REQUEST = "Notification request failed.";
	private static final String ERROR_TICKET_NOT_FOUND = "Ticket with objectId={} was not found";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EventNotificationService.class);
	
	@POST
	public void receiveNotificationEvent(EventNotification eventNotification){
		retrieveTicket(eventNotification.getBusinessObjectId());
	}

    @GET
    public Response checkConection(){
        return Response.status(Response.Status.OK).build();
    }
	
	private void retrieveTicket(String objectId){
		try {
			C4CTicket c4cTicket = C4CTicketService.retrieveC4CTicketByObjectID(objectId);
			Ticket ticket = Ticket.convertC4CTicket(c4cTicket);
			TicketStorage.save(ticket);
			IndexService.add(ticket);
		} catch (InvalidResponseException | IOException e) {
			LOGGER.error(ERROR_NOTIFICATION_REQUEST, e);
		} catch (IndexException e) {
			LOGGER.error(ERROR_ADDING_TICKET_TO_INDEXER, e);
		} catch (C4CTicketNotFoundException e) {
			LOGGER.error(ERROR_TICKET_NOT_FOUND, objectId);
		}
	}

}
