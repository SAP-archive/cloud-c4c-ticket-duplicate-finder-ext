package com.sap.cloud.c4c.ticket.duplicate.finder.application;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class InitializeListener implements ServletContextListener {
    
    private static final String ERROR_IN_INITIAL_TICKET_LOAD = "Error in initial ticket load.";
    private static final Logger LOGGER = LoggerFactory.getLogger(InitializeListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            TicketLoader.getTickets(20);
        } catch (Exception e) {
            LOGGER.error(ERROR_IN_INITIAL_TICKET_LOAD,e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Do nothing
    }

}