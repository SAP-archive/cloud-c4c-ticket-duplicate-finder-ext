package com.sap.cloud.c4c.ticket.duplicate.finder.index;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.c4c.ticket.duplicate.finder.application.model.Ticket;

public class IndexService {
	
	private static final String WARN_DOCUMENTS_ALREADY_MERGED = "Documents with ticket ids [{0}] and [{1}] are already merged.";
	private static final String ERROR_TICKET_NOT_FOUND = "Ticket with id={0} was not found in the index repository.";
	private static final String ERROR_MERGING_FAILED = "Merging failed.";
	private static final String ERROR_SEARCHING_FAILED = "Searching failed.";
	private static final String ERROR_NULL_ARGUMENT = "Argument should not be null.";
	private static final String ERROR_ADDING_TICKET_FAILED = "Adding ticket failed.";
	
	private static final String ID = "id";
	private static final String SUBJECT = "subject";

	public static final Logger LOGGER = LoggerFactory.getLogger(IndexService.class);
	
	private static final int MAX_COUNT = 10;
	private static StandardAnalyzer analyzer = new StandardAnalyzer();

	public static void add(Ticket ticket) throws IndexException {
		if (ticket == null) {
			throw new IllegalArgumentException(ERROR_NULL_ARGUMENT);
		}

		Directory directory = IndexProvider.getInstance().getDirectory();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		try (IndexWriter indexWriter = new IndexWriter(directory, config)) {
			addTicket(indexWriter, ticket);
		} catch (IOException e) {
			LOGGER.error(ERROR_ADDING_TICKET_FAILED, e);
			throw new IndexException(ERROR_ADDING_TICKET_FAILED, e);
		}
	}
	
	public static List<TicketGroup> search(Ticket ticket) throws IndexException {
		return searchForTicket(ticket, MAX_COUNT);
	}
	
	public static void merge(String firstId, String secondId) throws IndexException {
		try {
			mergeTickets(firstId, secondId);
		} catch (IOException | ParseException e) {
			throw new IndexException(ERROR_MERGING_FAILED, e);
		}
	}

	private static List<TicketGroup> searchForTicket(Ticket ticket, int maxCount) throws IndexException {
		if (ticket == null) {
			throw new IllegalArgumentException(ERROR_NULL_ARGUMENT);
		}

		Directory directory = IndexProvider.getInstance().getDirectory();
		
		try (IndexReader reader = DirectoryReader.open(directory);) {
			Query query = new QueryParser(SUBJECT, analyzer).parse(QueryParser.escape(ticket.getSubject()));
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs docs = searcher.search(query, maxCount);
			ScoreDoc[] foundDocuments = docs.scoreDocs;

			List<TicketGroup> result = new ArrayList<>();
			for(ScoreDoc document: foundDocuments){
				String[] ids = searcher.doc(document.doc).getValues(ID);
				if (!(ids.length == 1 && ids[0].equals(ticket.getId()))){
					result.add(new TicketGroup(Arrays.asList(ids)));
				}
			}
			
			return result;
		} catch (IOException | ParseException e) {
			LOGGER.error(ERROR_SEARCHING_FAILED, e);
			throw new IndexException(ERROR_SEARCHING_FAILED, e);
		}
	}
	
	private static void mergeTickets(String firstId, String secondId) throws IOException, ParseException, IndexException {
		
		if (firstId == null || secondId == null) {
			throw new IllegalArgumentException(ERROR_NULL_ARGUMENT);
		}

		Directory directory = IndexProvider.getInstance().getDirectory();
		try (IndexReader indexReader = DirectoryReader.open(directory);) {
			IndexSearcher searcher = new IndexSearcher(indexReader);

			BooleanQuery firstQuery = getQuery(firstId);
			BooleanQuery secondQuery = getQuery(secondId);
			
			ScoreDoc[] topHitsDocsIdFirst = getTopHitsDoc(firstId, firstQuery, searcher);
			ScoreDoc[] topHitsDocsIdSecond = getTopHitsDoc(secondId, secondQuery, searcher);
			
			if (topHitsDocsIdFirst[0].doc == topHitsDocsIdSecond[0].doc) {
				String message = MessageFormat.format(WARN_DOCUMENTS_ALREADY_MERGED, firstId, secondId);
				LOGGER.warn(message);
				return;
			}
			
			Document newDocument = mergeDocuments(searcher, topHitsDocsIdFirst, topHitsDocsIdSecond);
			
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			try (IndexWriter indexWriter = new IndexWriter(directory, config);) {
				indexWriter.deleteDocuments(firstQuery);
				indexWriter.deleteDocuments(secondQuery);
				indexWriter.addDocument(newDocument);
			}
		}		
	}

	private static Document mergeDocuments(IndexSearcher searcher, ScoreDoc[] topHitsDocsIdFirst,
			ScoreDoc[] topHitsDocsIdSecond) throws IOException {
		Document firstDocument = searcher.doc(topHitsDocsIdFirst[0].doc);
		Document secondDocument = searcher.doc(topHitsDocsIdSecond[0].doc);

		Document newDocument = new Document();			
		concatenateTickets(firstDocument, secondDocument, newDocument, SUBJECT);
		concatenateTickets(firstDocument, secondDocument, newDocument, ID);
		return newDocument;
	}
	
	private static void concatenateTickets(Document doc1, Document doc2, Document newDocument, String name){
		Stream.concat(Arrays.stream(doc1.getValues(name)), Arrays.stream(doc2.getValues(name)))
		.map(getFunction(name)).forEach(field -> newDocument.add(field));
	}
	
	private static Function<? super String, ? extends Field> getFunction(String name){
		Function<? super String, ? extends Field> mapper = null;
		switch (name){
			case ID:
				mapper = id -> new StringField(ID, id, Field.Store.YES);
				break;
			case SUBJECT:
				mapper = subject -> new TextField(SUBJECT, subject, Field.Store.YES);
				break;
		}
		return mapper;
	}

	private static ScoreDoc[] getTopHitsDoc(String ticketid, BooleanQuery query, IndexSearcher searcher) throws IOException, IndexException {
		ScoreDoc[] topHitsDocs = searcher.search(query, MAX_COUNT).scoreDocs;
		if (topHitsDocs.length == 0) {
			throw new IndexException(MessageFormat.format(ERROR_TICKET_NOT_FOUND, ticketid));
		}
		return topHitsDocs;
	}

	private static BooleanQuery getQuery(String ticketId) {
		BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		booleanQueryBuilder.add(new TermQuery(new Term(ID, ticketId)), BooleanClause.Occur.MUST);
		return booleanQueryBuilder.build();
	}

	private static void addTicket(IndexWriter indexWriter, Ticket ticket) throws IOException {
		if (ticket.getSubject() == null || ticket.getId() == null){
			throw new IllegalArgumentException(ERROR_NULL_ARGUMENT);
		}
		
		Document doc = new Document();
		doc.add(new TextField(SUBJECT, ticket.getSubject(), Field.Store.YES));
		doc.add(new StringField(ID, ticket.getId(), Field.Store.YES));

		indexWriter.addDocument(doc);
	}

}
