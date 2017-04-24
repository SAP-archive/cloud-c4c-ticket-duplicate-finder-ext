package com.sap.cloud.c4c.ticket.duplicate.finder.index;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class IndexProvider {

	private static IndexProvider instance = null;
	private static Directory directory;

	private IndexProvider() {
		directory = new RAMDirectory();
	}

	public static IndexProvider getInstance() {
		if (IndexProvider.instance == null) {
			synchronized (IndexProvider.class) {
				if (IndexProvider.instance == null) {
					IndexProvider.instance = new IndexProvider();
				}
			}
		}

		return IndexProvider.instance;
	}

	public Directory getDirectory() {
		return directory;
	}
}