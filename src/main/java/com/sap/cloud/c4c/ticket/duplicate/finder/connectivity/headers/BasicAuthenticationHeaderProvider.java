package com.sap.cloud.c4c.ticket.duplicate.finder.connectivity.headers;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import javax.ws.rs.core.HttpHeaders;
import javax.xml.bind.DatatypeConverter;

import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

public class BasicAuthenticationHeaderProvider {

    private static final String BASIC_AUTHENTICATION_PREFIX = "Basic {0}";
    private static final String SEPARATOR = ":";
    private static final String PASSWORD_PROPERTY = "Password";
    private static final String USER_PROPERTY = "User";

    public AuthenticationHeader getAuthenticationHeader(DestinationConfiguration destinationConfiguration) {
        StringBuilder userPass = new StringBuilder();
        userPass.append(destinationConfiguration.getProperty(USER_PROPERTY));
        userPass.append(SEPARATOR);
        userPass.append(destinationConfiguration.getProperty(PASSWORD_PROPERTY));
        String encodedPassword = DatatypeConverter.printBase64Binary(userPass.toString().getBytes(StandardCharsets.UTF_8));
        return new AuthenticationHeaderImpl(HttpHeaders.AUTHORIZATION, MessageFormat.format(BASIC_AUTHENTICATION_PREFIX, encodedPassword));
    }
}
