package com.sap.cloud.c4c.ticket.duplicate.finder.application.model;

import com.sap.cloud.c4c.ticket.duplicate.finder.c4c.client.C4CTicket;

public class Ticket {

	private String id;
	private String objectId;
	private String status;
	private String subject;
	private String priority;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}
	
	public static Ticket convertC4CTicket(C4CTicket c4cTicket){
		Ticket ticket = new Ticket();
		ticket.setId(c4cTicket.getId());
		ticket.setObjectId(c4cTicket.getObjectId());
		ticket.setSubject(c4cTicket.getSubject());
		ticket.setPriority(c4cTicket.getPriority());
		ticket.setStatus(c4cTicket.getStatus());
		return ticket;
	}
}
