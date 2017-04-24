package com.sap.cloud.c4c.ticket.duplicate.finder.index;

public class IndexException extends Exception {

	private static final long serialVersionUID = 1L;

	public IndexException(String message) {
		super(message);
	}

	public IndexException(String message, Throwable cause) {
		super(message, cause);
	}
}
