package com.sap.cloud.c4c.ticket.duplicate.finder.connectivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.c4c.ticket.duplicate.finder.connectivity.headers.BasicAuthenticationHeaderProvider;
import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

public class HTTPConnector {
	
	private static final String INFO_REQUEST_METHOD = "HTTP GET request to URL {}";
	private static final String DEBUG_RESPONSE_MSG = "Response from requesting {} is {} {}";
	private static final String DEBUG_RESPONSE_CONTENT_TYPE_MSG = "Response content type from requesting {} is {}";
	private static final String DEBUG_RESPONSE_CONTENT_MSG = "Response content from requesting {} is {}";
	private static final String ERROR_NOT_CONFIGURED_DESTINATION_URL = "Request URL in Destination {0} is not configured. Make sure to have the destination configured.";
	private static final String INFO_DESTINATION_REQUEST = "HTTP Request from destination {} with base URL {} and relative path {}";
	private static final String ERROR_DESTINATION_NOT_FOUND = "Destination {0} is not found. Make sure to have the destination configured.";

	private static final String CONNECTIVITY_CONFIGURATION_LOOKUP_NAME = "java:comp/env/connectivityConfiguration";
	private static final String DESTINATION_AUTHENTICATION_PROPERTY = "Authentication";
	private static final String BASIC_AUTHENTICATION_PROPERTY = "BasicAuthentication";
	private static final String PATH_SUFFIX = "/";
	private static final String GET_METHOD = "GET";
	private static final String DESTINATION_URL = "URL";

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPConnector.class);

	private final String destinationName;
	private ConnectivityConfiguration localConnectivityConfiguration;

	private HTTPResponseValidator responseValidator;

	public HTTPConnector(String destinationName) {
		this.destinationName = destinationName;
		this.responseValidator = new DefaultHTTPResponseValidator();
	}

	public SimpleHttpResponse executeGET(String path) throws InvalidResponseException, IOException {
		DestinationConfiguration destinationConfiguration = lookupDestinationConfiguration();
		URL requestURL = getRequestURL(destinationConfiguration, path);
		LOGGER.info(INFO_REQUEST_METHOD, requestURL.toString());
		HttpURLConnection urlConnection = openConnection(destinationConfiguration, requestURL);
		return executeMethodGET(urlConnection);
	}

	private HttpURLConnection openConnection(DestinationConfiguration destinationConfiguration, URL requestURL)
			throws IOException, ProtocolException {
		HttpURLConnection urlConnection = (HttpURLConnection) requestURL.openConnection();
		urlConnection.addRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
		injectAuthenticationHeaders(urlConnection, destinationConfiguration);
		urlConnection.setRequestMethod(GET_METHOD);
		return urlConnection;
	}

	private SimpleHttpResponse executeMethodGET(HttpURLConnection urlConnection) throws IOException, InvalidResponseException {
		SimpleHttpResponse httpResponse = new SimpleHttpResponse(urlConnection.getURL().toString(), urlConnection.getResponseCode(),
				urlConnection.getResponseMessage());
		httpResponse.setContentType(urlConnection.getContentType());
		httpResponse.setContent(IOUtils.toString(urlConnection.getInputStream()));
		logResponse(httpResponse);
		validateResponse(httpResponse);
		return httpResponse;
	}

	private void logResponse(SimpleHttpResponse httpResponse) {
		String requestPath = httpResponse.getRequestPath();
		LOGGER.debug(DEBUG_RESPONSE_MSG, requestPath, httpResponse.getResponseCode(), httpResponse.getResponseMessage());
		LOGGER.debug(DEBUG_RESPONSE_CONTENT_TYPE_MSG, requestPath, httpResponse.getContentType());
		LOGGER.debug(DEBUG_RESPONSE_CONTENT_MSG, requestPath, httpResponse.getContent());
	}

	private void validateResponse(SimpleHttpResponse httpResponse) throws InvalidResponseException {
		if (responseValidator != null) {
			responseValidator.validateHTTPResponse(httpResponse);
		}
	}

	private void injectAuthenticationHeaders(HttpURLConnection urlConnection,
			DestinationConfiguration destinationConfiguration) throws IOException {
		List<AuthenticationHeader> authenticationHeaders = getAuthenticationHeaders(destinationConfiguration);
		for (AuthenticationHeader authenticationHeader : authenticationHeaders) {
			urlConnection.addRequestProperty(authenticationHeader.getName(), authenticationHeader.getValue());
		}
	}

	private URL getRequestURL(DestinationConfiguration destinationConfiguration, String path) throws IOException {
		String requestBaseURLString = destinationConfiguration.getProperty(DESTINATION_URL);
		if (StringUtils.isEmpty(requestBaseURLString)) {
			String errorMessage = MessageFormat.format(ERROR_NOT_CONFIGURED_DESTINATION_URL, destinationName);
			LOGGER.error(errorMessage);
			throw new IOException(errorMessage);
		}
		if (!requestBaseURLString.endsWith(PATH_SUFFIX)) {
			requestBaseURLString += PATH_SUFFIX;
		}
		LOGGER.info(INFO_DESTINATION_REQUEST, destinationName,	requestBaseURLString, path);
		URL baseURL = new URL(requestBaseURLString);
		return new URL(baseURL, path);
	}

	private List<AuthenticationHeader> getAuthenticationHeaders(DestinationConfiguration destinationConfiguration)
			throws IOException {
		String authenticationType = destinationConfiguration.getProperty(DESTINATION_AUTHENTICATION_PROPERTY);
		List<AuthenticationHeader> authenticationHeaders = new ArrayList<>();
		if (BASIC_AUTHENTICATION_PROPERTY.equals(authenticationType)) {
			BasicAuthenticationHeaderProvider headerProvider = new BasicAuthenticationHeaderProvider();
			authenticationHeaders.add(headerProvider.getAuthenticationHeader(destinationConfiguration));
		}
		return authenticationHeaders;
	}

	private DestinationConfiguration lookupDestinationConfiguration() throws IOException {
		ConnectivityConfiguration connectivityConfiguration = lookupConnectivityConfiguration();

		DestinationConfiguration destinationConfiguration = connectivityConfiguration.getConfiguration(destinationName);
		if (destinationConfiguration == null) {
			String errorMessage = MessageFormat.format(ERROR_DESTINATION_NOT_FOUND, destinationName);
			LOGGER.error(errorMessage);
			throw new IOException(errorMessage);
		}
		return destinationConfiguration;
	}

	private synchronized ConnectivityConfiguration lookupConnectivityConfiguration() throws IOException {
		try {
			if (this.localConnectivityConfiguration == null) {
				Context ctx = new InitialContext();
				this.localConnectivityConfiguration = (ConnectivityConfiguration) ctx.lookup(CONNECTIVITY_CONFIGURATION_LOOKUP_NAME);
			}
			return this.localConnectivityConfiguration;
		} catch (NamingException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

}
