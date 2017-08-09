package com.sap.cloud.c4c.ticket.duplicate.finder.application.user;

import java.util.Set;

import com.sap.security.um.user.UnsupportedUserAttributeException;

public class User {
	public String firstname;
	public String lastname;
	public String email;
	public Set<String> roles;

	public User(String firstname, String lastname, String email, Set<String> roles) {
		super();
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.roles = roles;
	}

	public static User fromLoggedInUser(com.sap.security.um.user.User user) throws UnsupportedUserAttributeException {
		String firstname = user.getAttribute("firstname");
		String lastname = user.getAttribute("lastname");
		String email = user.getAttribute("email");
		Set<String> roles = user.getRoles();
		return new User(firstname, lastname, email, roles);
	}
}
