/**
 * 
 */
package org.gbif.provider.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.FullTextSearchManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A Lucene based version of the full text manager
 * @author tim
 */
public class FullTextSearchManagerLucene implements FullTextSearchManager {
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired
	protected AppConfig cfg;
	protected String indexDirectoryName = "lucene";
	
	/**
	 * @see org.gbif.provider.service.FullTextSearchManager#buildDataResourceIndexes(org.gbif.provider.model.DataResource)
	 */
	public void buildDataResourceIndexes(DataResource resource) {
		IndexWriter writer = null;
		try {
			// this is just a quick test
			String indexDir = AppConfig.getResourceDataDir(resource.getId()).getAbsolutePath()+indexDirectoryName;
			writer = new IndexWriter(indexDir, new StandardAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
			
			// IGNORE the header row
			File data = cfg.getDumpFile(resource.getId(), resource.getCoreMapping().getExtension());
			log.info("Building core mapping text index for resource[" + resource.getId() + "]");
			buildIndex(writer, data);
			for (ViewMappingBase view : resource.getAllMappings()) {
				log.info("Building extension[" + view.getExtension().getName() + "] index for resource[" + resource.getId() + "]");
				File extensionFile = cfg.getDumpFile(resource.getId(),view.getExtension());
				buildIndex(writer, extensionFile);
			}
			writer.optimize();
			
		} catch (Exception e) {
			log.error("Error building index: " + e.getMessage(), e);
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
				log.error("Error closing index writer: " + e.getMessage(), e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Builds (appends) to the actual index
	 */
	private void buildIndex(IndexWriter writer, File data)
			throws FileNotFoundException, IOException, CorruptIndexException {
		CSVReader reader = new CSVReader(new BufferedReader(new FileReader(data)));
		String [] line;
		line = reader.readNext();
		
		// Each row becomes a Lucene document
		int count = 0;
		while ((line = reader.readNext()) != null) {
			// turn the row back into a " " separated String
			StringBuffer sb = new StringBuffer();
			for (String s : line) {
				sb.append(s + " ");
			}
			
			Document doc = new Document();
			Field id = new Field("id", line[0], Field.Store.YES, Field.Index.NOT_ANALYZED);
			doc.add(id);
			doc.add(new Field("data", sb.toString(), Field.Store.NO, Field.Index.ANALYZED));
			writer.addDocument(doc);
			
			if (log.isDebugEnabled() && count%100==0) {
				log.debug("Indexed[" + count + "], last id[" + id+ "], last data[" + sb.toString() + "]");
			}
			count++;
		}
	}
	
	/**
	 * Does the seaching
	 * 
	 * Note: this could be sped up if the searcher is cached, and not opened and closed on each query
	 * This would be done much the same as the DB datasource per resource that must exist
	 * 
	 * @param resourceId To search within
	 * @param q The query
	 * @return List of IDs for core records
	 */
	public List<Long> search(Long resourceId, String q) {
		String indexDir = AppConfig.getResourceDataDir(resourceId).getAbsolutePath()+indexDirectoryName;
		IndexReader reader = null;
		Searcher searcher = null;
		List<Long> results = new LinkedList<Long>();
		try {
			reader = IndexReader.open(indexDir);
			searcher = new IndexSearcher(reader);

			// do a term query
			Term term = new Term("data", q);
			Query query = new WildcardQuery(term);
			
			TopDocCollector collector = new TopDocCollector(10);
		    searcher.search(query, collector);
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    int numTotalHits = collector.getTotalHits();
			log.debug("Search term[" + q + "] found[" + numTotalHits + "] records");
		    
		    for (ScoreDoc scoreDoc : hits) {
		    	Document doc = searcher.doc(scoreDoc.doc);
				String id = doc.get("id");
			    results.add(Long.parseLong(id));
		    }		    
		    
		    
		} catch (Exception e) {
			log.error("Error with FullTextSearch[" + e.getMessage()+"] - returning empty results rather than passing error to user", e);
			
		} finally {
			try {
				searcher.close();
			} catch (IOException e) {
				log.error("Error closing Lucene searcher: " + e.getMessage(), e);
			}
			try {
				reader.close();
			} catch (IOException e) {
				log.error("Error closing Lucene searcher: " + e.getMessage(), e);
			}
		}
		return results;
	}	
	
	/**
	 * @see org.gbif.provider.service.FullTextSearchManager#buildResourceIndexes()
	 */
	public void buildResourceIndexes() {
		// TODO Auto-generated method stub
	}

	public void setIndexDirectoryName(String indexDirectoryName) {
		this.indexDirectoryName = indexDirectoryName;
	}
}
