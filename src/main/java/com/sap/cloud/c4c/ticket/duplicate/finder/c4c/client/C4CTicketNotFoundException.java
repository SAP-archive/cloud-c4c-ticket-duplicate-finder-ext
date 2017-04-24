package com.sap.cloud.c4c.ticket.duplicate.finder.c4c.client;

public class C4CTicketNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public C4CTicketNotFoundException(String message) {
		super(message);
	}

	public C4CTicketNotFoundException() {
		super();
	}
}
