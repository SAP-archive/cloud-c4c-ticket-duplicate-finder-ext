package com.sap.cloud.c4c.ticket.duplicate.finder.application.user;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.security.auth.login.LoginContextFactory;
import com.sap.security.um.service.UserManagementAccessor;
import com.sap.security.um.user.PersistenceException;
import com.sap.security.um.user.UnsupportedUserAttributeException;
import com.sap.security.um.user.UserProvider;

public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	public static User getLoggedInUser() throws UserServiceException {
		UserProvider userProvider;
		try {
			userProvider = UserManagementAccessor.getUserProvider();
			User currentUser = User.fromLoggedInUser(userProvider.getCurrentUser());
			return currentUser;
		} catch (PersistenceException e) {
			String errorMessage = "User not authorized.";
			LOGGER.error(errorMessage, e);
			throw new UserServiceException(errorMessage, e);
		} catch (UnsupportedUserAttributeException e) {
			String errorMessage = "User attributes: firstname, lastname and email are required.";
			LOGGER.error(errorMessage, e);
			throw new UserServiceException(errorMessage, e);
		}

	}

	public static void logoutUser(HttpServletRequest httpRequest) throws UserServiceException {
		if (httpRequest.getRemoteUser() != null) {
			try {
				LoginContext loginContext = LoginContextFactory.createLoginContext();
				loginContext.logout();
			} catch (LoginException e) {
				String errorMessage = "Unsuccessful logout.";
				LOGGER.error(errorMessage, e);
				throw new UserServiceException(errorMessage, e);
			}
		}
	}
}
