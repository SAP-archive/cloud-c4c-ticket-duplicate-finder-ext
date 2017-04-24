package com.sap.cloud.c4c.ticket.duplicate.finder.application;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class InitializeListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		TicketLoader.loadTickets();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Do nothing
	}

}