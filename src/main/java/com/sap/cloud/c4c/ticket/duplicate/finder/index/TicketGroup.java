package com.sap.cloud.c4c.ticket.duplicate.finder.index;

import java.util.ArrayList;
import java.util.List;

public class TicketGroup {

	private List<String> tickets = new ArrayList<>();
	
	public TicketGroup(List<String> tickets) {
		this.tickets = tickets;
	}

	public List<String> getTickets() {
		return tickets;
	}

	public void setTickets(List<String> tickets) {
		this.tickets = tickets;
	}

}
