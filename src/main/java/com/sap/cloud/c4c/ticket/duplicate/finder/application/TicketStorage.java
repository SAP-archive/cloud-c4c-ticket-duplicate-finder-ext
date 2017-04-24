package com.sap.cloud.c4c.ticket.duplicate.finder.application;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sap.cloud.c4c.ticket.duplicate.finder.application.model.Ticket;

public class TicketStorage {

	private static Map<String, Ticket> tickets = new HashMap<>();

	public static Ticket save(Ticket ticket) {
		tickets.put(ticket.getId(), ticket);
		return ticket;
	}

	public static Collection<Ticket> findAll() {
		return tickets.values();
	}

	public static Ticket findById(String id) {
		return tickets.get(id);
	}
	
	public static boolean contains(String id){
		return tickets.containsKey(id);
	}
}
