package com.sap.cloud.c4c.ticket.duplicate.finder.connectivity;

import java.text.MessageFormat;

public class SimpleHttpResponse {
	
	private static final String STRING_REPRESENTATION = "SimpleHttpResponse [requestPath={0}, responseCode={1}, responseMessage={2}, contentType={3}, content={4}]";
	
    private final String requestPath;
	private final int responseCode;
	private final String responseMessage;
	private String contentType;
	private String content;
	
	public SimpleHttpResponse(String requestPath, int responseCode, String responseMessage) {
	    this.requestPath = requestPath;
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

    public String getRequestPath() {
        return requestPath;
    }

	@Override
	public String toString() {
		return MessageFormat.format(STRING_REPRESENTATION, requestPath, responseCode, responseMessage, contentType, content);
	}
	
}
