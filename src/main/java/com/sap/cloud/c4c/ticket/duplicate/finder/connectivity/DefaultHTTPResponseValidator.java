package com.sap.cloud.c4c.ticket.duplicate.finder.connectivity;

import java.text.MessageFormat;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.common.util.StringUtils;

public class DefaultHTTPResponseValidator implements HTTPResponseValidator {
	
	private static final String ERROR_SERVICE_RETURN = " Service returned [{0}] [{1}].";
	private static final String ERROR_PATH_NOT_FOUND = "Requesting path [{0}] was not found.";
	private static final String ERROR_WRONG_CREDENTIALS ="Missing or incorrect credentials for path [{0}].";
	private static final String ERROR_UNAUTHORIZED = "Unauthorized request to path [{0}].";
	private static final String ERROR_UNEXPECTED_RESPONSE = "Requesting path [{0}] returns unexpected response.";
	private static final String ERROR_NOT_FOUND_CONTENT_TYPE = "Response content type not found when requesting path [{0}]";
	private static final String ERROR_INVALID_CONTENT_TYPE = "Invalid response content type [{0}] when requesting path [{0}]";

    @Override
    public void validateHTTPResponse(SimpleHttpResponse httpResponse) throws InvalidResponseException {
        validateStatusCode(httpResponse);
        validateContentType(httpResponse);
    }

    private void validateStatusCode(SimpleHttpResponse httpResponse) throws InvalidResponseException {
        final int statusCode = httpResponse.getResponseCode();
        if (statusCode == HttpServletResponse.SC_OK) {
            return;
        }

        String errMessage = getErrorMessageByStatusCode(httpResponse.getRequestPath(), statusCode);
        errMessage += MessageFormat.format(ERROR_SERVICE_RETURN, statusCode, httpResponse.getResponseMessage());

        throw new InvalidResponseException(errMessage);
    }

	private String getErrorMessageByStatusCode(String requestPath, final int statusCode) {
		String errMessage;
		switch (statusCode) {
        case HttpServletResponse.SC_NOT_FOUND:
            errMessage = MessageFormat.format(ERROR_PATH_NOT_FOUND, requestPath);
            break;
        case HttpServletResponse.SC_UNAUTHORIZED:
            errMessage = MessageFormat.format(ERROR_WRONG_CREDENTIALS, requestPath);
            break;
        case HttpServletResponse.SC_FORBIDDEN:
            errMessage = MessageFormat.format(ERROR_UNAUTHORIZED, requestPath);
            break;
        default:
            errMessage = MessageFormat.format(ERROR_UNEXPECTED_RESPONSE, requestPath);
        }
		return errMessage;
	}

    private void validateContentType(SimpleHttpResponse httpResponse) throws InvalidResponseException {
    	String requestPath = httpResponse.getRequestPath();
    	String responseContentType = httpResponse.getContentType();
        if (StringUtils.isEmpty(responseContentType)) {
            throw new InvalidResponseException(MessageFormat.format(ERROR_NOT_FOUND_CONTENT_TYPE, requestPath));
        }
        if (!responseContentType.contains(MediaType.APPLICATION_JSON)) {
            throw new InvalidResponseException(MessageFormat.format(ERROR_INVALID_CONTENT_TYPE, responseContentType, requestPath));
        }
    }

}
