package com.sap.cloud.c4c.ticket.duplicate.finder.application;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.sap.cloud.c4c.ticket.duplicate.finder.application.notification.EventNotificationService;
import com.sap.cloud.c4c.ticket.duplicate.finder.application.resources.AppInfoResource;
import com.sap.cloud.c4c.ticket.duplicate.finder.application.resources.TicketResource;

public class ApplicationConfiguration extends Application {

	public Set<Class<?>> getClasses() {
		Set<Class<?>> services = new HashSet<>();
		services.add(JacksonJaxbJsonProvider.class);
		services.add(TicketResource.class);
		services.add(AppInfoResource.class);
		services.add(EventNotificationService.class);
		return services;
	}
}