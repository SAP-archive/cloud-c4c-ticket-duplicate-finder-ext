package com.sap.cloud.c4c.ticket.duplicate.finder.c4c.client;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cloud.c4c.ticket.duplicate.finder.connectivity.HTTPConnector;
import com.sap.cloud.c4c.ticket.duplicate.finder.connectivity.InvalidResponseException;

public class C4CTicketService {
	
	private static final String QUERY_PATH = "{0}%20eq%20%27{1}%27{2}";
	private static final String SELECT_QUERY_PATH = "&$select=ID,ObjectID,Name,ServicePriorityCodeText,ServiceRequestUserLifeCycleStatusCodeText,RequestInitialReceiptdatetimecontent";
	private static final String DESTINATION_NAME = "sap_cloud4customer_odata";
	private static final String FILTER_BY_ID_QUERY_PATH = "ServiceRequestCollection?$filter=ID";
	private static final String FILTER_BY_OBJECT_ID_QUERY_PATH = "ServiceRequestCollection?$filter=ObjectID";
	private static final String OBJECT_ID_JSON_NODE = "ObjectID";
	private static final String ID_JSON_NODE = "ID";
	private static final String SUBJECT_NAME_JSON_NODE = "Name";
	private static final String STATUS_JSON_NODE = "ServiceRequestUserLifeCycleStatusCodeText";
	private static final String PRIORITY_JSON_NODE = "ServicePriorityCodeText";
	private static final String RESULTS_JSON_TREE_NODE = "results";
	private static final String LAST_CREATED_TICKETS_QUERY_PATH = "ServiceRequestCollection?$orderby=RequestInitialReceiptdatetimecontent%20desc&$top={0}{1}";

	public static C4CTicket retrieveC4CTicketByObjectID(String objectId) throws IOException, InvalidResponseException, C4CTicketNotFoundException {
		String ticketDetailsURLQueryPath = MessageFormat.format(QUERY_PATH, FILTER_BY_OBJECT_ID_QUERY_PATH, objectId,
				SELECT_QUERY_PATH);
		return retrieveC4CTicket(ticketDetailsURLQueryPath);
	}

	public static C4CTicket retrieveC4CTicketByID(String ticketId) throws IOException, InvalidResponseException, C4CTicketNotFoundException {
		String ticketDetailsURLQueryPath = MessageFormat.format(QUERY_PATH, FILTER_BY_ID_QUERY_PATH, ticketId,
				SELECT_QUERY_PATH);
		return retrieveC4CTicket(ticketDetailsURLQueryPath);
	}
	
	public static List<C4CTicket> retrieveLastCreatedC4CTickets(int count) throws IOException, InvalidResponseException, ParseException {
		HTTPConnector connector = new HTTPConnector(DESTINATION_NAME);
		String ticketsDatilsURLQueryPath = MessageFormat.format(LAST_CREATED_TICKETS_QUERY_PATH, count, SELECT_QUERY_PATH);
		String content = connector.executeGET(ticketsDatilsURLQueryPath).getContent();
		return parseArray(content);
	}

	private static C4CTicket retrieveC4CTicket(String urlQueryPath) throws InvalidResponseException, IOException, C4CTicketNotFoundException {
		HTTPConnector connector = new HTTPConnector(DESTINATION_NAME);
		String content = connector.executeGET(urlQueryPath).getContent();
		return parse(content);
	}

	private static C4CTicket parse(String content) throws JsonProcessingException, IOException, C4CTicketNotFoundException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jNode = mapper.readTree(content);
		JsonNode resultsArray = jNode.findValue(RESULTS_JSON_TREE_NODE);
		if(resultsArray.isNull() || !resultsArray.elements().hasNext()){
			throw new C4CTicketNotFoundException();
		}
		
		C4CTicket c4cTicket = createC4CTicketObject(jNode);
		return c4cTicket;
	}

	private static List<C4CTicket> parseArray(String content) throws JsonProcessingException, IOException {
		List<C4CTicket> c4cTickets = new ArrayList<>();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode parentNode = mapper.readTree(content).findValue(RESULTS_JSON_TREE_NODE);
		Iterator<JsonNode> iterator = parentNode.elements();
		while (iterator.hasNext()) {
			JsonNode jNode = iterator.next();
			c4cTickets.add(createC4CTicketObject(jNode));
		}
		return c4cTickets;
	}

	private static C4CTicket createC4CTicketObject(JsonNode ticketNode) {
		C4CTicket c4cTicket = new C4CTicket();
		c4cTicket.setId(ticketNode.findValue(ID_JSON_NODE).asText());
		c4cTicket.setObjectId(ticketNode.findValue(OBJECT_ID_JSON_NODE).asText());
		c4cTicket.setSubject(ticketNode.findValue(SUBJECT_NAME_JSON_NODE).asText());
		c4cTicket.setPriority(ticketNode.findValue(PRIORITY_JSON_NODE).asText());
		c4cTicket.setStatus(ticketNode.findValue(STATUS_JSON_NODE).asText());
		return c4cTicket;
	}

}
