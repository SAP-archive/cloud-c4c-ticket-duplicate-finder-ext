package com.sap.cloud.c4c.ticket.duplicate.finder.application;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.c4c.ticket.duplicate.finder.application.model.Ticket;
import com.sap.cloud.c4c.ticket.duplicate.finder.c4c.client.C4CTicketService;
import com.sap.cloud.c4c.ticket.duplicate.finder.index.IndexException;
import com.sap.cloud.c4c.ticket.duplicate.finder.index.IndexService;

public class TicketLoader {
    
    private static final String INFO_FETCH_STARTED = "Tickets fetching started.";   
    private static final String INFO_FETCH_ENDED = "Tickets fetching ended.";
    private static final String ERROR_ADDING_TICKET_TO_INDEX = "Adding ticket to indexer failed.";

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketLoader.class);
    
    public static Collection<Ticket> getTickets(int numberOfTickets) throws Exception {
        LOGGER.info(INFO_FETCH_STARTED);

        C4CTicketService.retrieveLastCreatedC4CTickets(numberOfTickets).stream()
                .map(Ticket::convertC4CTicket)
                .filter(ticket -> !(TicketStorage.contains(ticket.getId())))
                .forEach(ticket -> {
                    TicketStorage.save(ticket);
                    try {
                        IndexService.add(ticket);
                    } catch (IndexException e) {
                        LOGGER.error(ERROR_ADDING_TICKET_TO_INDEX, e);
                    }
                });

        LOGGER.info(INFO_FETCH_ENDED);
        return TicketStorage.findAll();
    }
}
