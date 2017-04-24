package com.sap.cloud.c4c.ticket.duplicate.finder.application.model;

import java.util.List;

public class Group {

	private List<Ticket> tickets;

	public Group(List<Ticket> tickets) {
		this.tickets = tickets;
	}

	public List<Ticket> getTickets() {
		return tickets;
	}

	public void setTickets(List<Ticket> tickets) {
		this.tickets = tickets;
	}
}
