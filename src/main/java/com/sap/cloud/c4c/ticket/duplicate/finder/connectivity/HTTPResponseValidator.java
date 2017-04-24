package com.sap.cloud.c4c.ticket.duplicate.finder.connectivity;

public interface HTTPResponseValidator {
	
   void validateHTTPResponse(SimpleHttpResponse httpResponse) throws InvalidResponseException;

}
