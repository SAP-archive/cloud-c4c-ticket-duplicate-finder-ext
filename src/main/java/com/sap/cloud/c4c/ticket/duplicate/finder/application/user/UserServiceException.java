package com.sap.cloud.c4c.ticket.duplicate.finder.application.user;

public class UserServiceException extends Exception {

	private static final long serialVersionUID = -7142446609618637411L;

	public UserServiceException(String message) {
		super(message);
	}

	public UserServiceException(String message, Throwable e) {
		super(message, e);
	}

}
