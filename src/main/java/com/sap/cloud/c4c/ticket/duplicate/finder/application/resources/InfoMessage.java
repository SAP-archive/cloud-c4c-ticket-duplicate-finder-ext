package com.sap.cloud.c4c.ticket.duplicate.finder.application.resources;

import javax.ws.rs.core.Response.Status;

public class InfoMessage {

	private Status status;
	private String message;

	public InfoMessage(Status status, String message) {
		super();
		this.status = status;
		this.message = message;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
