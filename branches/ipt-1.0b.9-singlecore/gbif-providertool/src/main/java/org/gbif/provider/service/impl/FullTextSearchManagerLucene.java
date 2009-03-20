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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.LockObtainFailedException;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.service.FullTextSearchManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.CSVReader;
import org.gbif.provider.util.XmlContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.xml.sax.SAXException;

/**
 * A Lucene based version of the full text manager
 * @author tim
 */
public class FullTextSearchManagerLucene implements FullTextSearchManager {
	protected final Log log = LogFactory.getLog(getClass());
	private static final String FIELD_ID ="id";
	private static final String FIELD_DATA ="data";
	private static final String FIELD_ACCESS ="acl";
	private static final String PUBLIC_ACCESS ="public";
	private File indexDir;
	private IndexWriter writer;
	private Searcher searcher;
	private XmlContentHandler handler = new XmlContentHandler(); 
    private SAXParser saxParser;
	@Autowired
	protected AppConfig cfg;

	@Autowired
	@Qualifier("resourceManager") 
	private GenericResourceManager<Resource> resourceManager;
	
	protected String indexDirectoryName = "lucene";
	
	
	private void openWriter() throws CorruptIndexException, LockObtainFailedException, IOException{
		if (writer==null){
			if (searcher!=null){
				searcher.close();
				searcher=null;
			}
			writer = new IndexWriter(indexDir, new StandardAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
		}
	}
	private void openSearcher() throws CorruptIndexException, IOException {
		if (searcher==null){
			if (writer!=null){
				writer.optimize();
				writer.close();
				writer=null;
			}
			searcher = new IndexSearcher(IndexReader.open(indexDir));
		}
	}
	public void init() {
		indexDir = new File(cfg.getDataDir(), indexDirectoryName);
		SAXParserFactory factory = SAXParserFactory.newInstance();		 
		try {
			saxParser=factory.newSAXParser();
			openSearcher();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void destroy() {
		if (searcher!=null){
			try {
				searcher.close();
			} catch (IOException e) {
				log.error("Error closing Lucene searcher: " + e.getMessage(), e);
			}
		}
		if (writer!=null){
			try {
				writer.close();
			} catch (IOException e) {
				log.error("Error closing Lucene writer: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * @see org.gbif.provider.service.FullTextSearchManager#buildDataResourceIndex(org.gbif.provider.model.DataResource)
	 */
	public void buildDataResourceIndex(DataResource resource) {
		IndexWriter writer = null;
		try {
			// this is just a quick test
			File indexDir = getResourceIndexDirectory(resource.getId());
			writer = new IndexWriter(indexDir, new StandardAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
			
			// IGNORE the header row
			File data = cfg.getArchiveFile(resource.getId(), resource.getCoreMapping().getExtension());
			log.info("Building core mapping text index for resource[" + resource.getId() + "]");
			buildIndex(writer, data);
			for (ExtensionMapping view : resource.getExtensionMappings()) {
				log.info("Building extension[" + view.getExtension().getName() + "] index for resource[" + resource.getId() + "]");
				File extensionFile = cfg.getArchiveFile(resource.getId(),view.getExtension());
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
			Field id = new Field(FIELD_ID, line[0], Field.Store.YES, Field.Index.NOT_ANALYZED);
			doc.add(id);
			doc.add(new Field(FIELD_DATA, sb.toString(), Field.Store.NO, Field.Index.ANALYZED));
			writer.addDocument(doc);
			
			if (log.isDebugEnabled() && count%1000==0) {
				log.debug("Indexed[" + count + "], last id[" + id+ "], last data[" + sb.toString() + "]");
			}
			count++;
		}
	}
	
	public void buildResourceIndex(Long resourceId){
		Resource resource = resourceManager.get(resourceId);
		if (resource==null){
			throw new IllegalArgumentException("Resource "+resourceId+" doesn't exist");
		}
	    try {
	    	File eml = cfg.getEmlFile(resourceId);
			log.info("Building resource metadata text index for resource[" + resourceId + "]");
			saxParser.parse(eml, handler);
			// make sure lucene writer is open. Reuse existing one if kept open
	    	openWriter();
			// if resource was indexed before remove the existing index document
			Query query = new TermQuery(new Term(FIELD_ID, resourceId.toString()));
			writer.deleteDocuments(query);
			// create new index document
			Document doc = new Document();
			doc.add(new Field(FIELD_ID, resourceId.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
			doc.add(new Field(FIELD_DATA, handler.getContent(), Field.Store.NO, Field.Index.ANALYZED));
			// store publication status
			String acl = resource.getCreator().getId().toString(); 
			if (resource.isPublished()){
				acl += " "+PUBLIC_ACCESS;
			}
			doc.add(new Field(FIELD_ACCESS, acl, Field.Store.NO, Field.Index.ANALYZED));
			writer.addDocument(doc);
		} catch (Exception e) {
			log.error("Error indexing metadata for resource "+resourceId, e);
		} finally {
			try {
				writer.commit();
				writer.optimize();
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Does the seaching
	 * 
	 * @param resourceId To search within
	 * @param q The query
	 * @return List of IDs for core records
	 */
	public List<Long> search(Long resourceId, String q) {
		File indexDir = getResourceIndexDirectory(resourceId);
		IndexReader reader = null;
		Searcher searcher = null;
		List<Long> results = new LinkedList<Long>();
		try {
			reader = IndexReader.open(indexDir);
			searcher = new IndexSearcher(reader);

			// do a term query
			Term term = normalizeQueryString(q);
			BooleanQuery query = new BooleanQuery();
			Query wild = new WildcardQuery(term); 
			query.add(wild,  BooleanClause.Occur.MUST);
			query.add(new TermQuery(new Term(FIELD_ACCESS, PUBLIC_ACCESS)),  BooleanClause.Occur.MUST);
			
			TopDocCollector collector = new TopDocCollector(10);
		    searcher.search(wild, collector);
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    int numTotalHits = collector.getTotalHits();
			log.debug("Search term[" + q + "] found[" + numTotalHits + "] records");
		    
		    for (ScoreDoc scoreDoc : hits) {
		    	Document doc = searcher.doc(scoreDoc.doc);
				String id = doc.get(FIELD_ID);
			    results.add(Long.parseLong(id));
		    }		    		    
		    
		} catch (Exception e) {
			log.error("Error with FullTextSearch[" + e.getMessage()+"] - returning empty results rather than passing error to user", e);
			
		} finally {
			try {
				if (searcher!=null){
					searcher.close();
				}
			} catch (IOException e) {
				log.error("Error closing Lucene searcher: " + e.getMessage(), e);
			}
			try {
				if (reader!=null){
					reader.close();
				}
			} catch (IOException e) {
				log.error("Error closing Lucene searcher: " + e.getMessage(), e);
			}
		}
		return results;
	}	
	
	/**
	 * @see org.gbif.provider.service.FullTextSearchManager#buildResourceIndex()
	 */
	public void buildResourceIndex() {
		// go through all resources and analyze the metadata xml files
		// to avoid indexing xml tags, use an uber simple SAX parser that just extracts all element & attribute content	    
	    List<Long> resourceIDs = resourceManager.getAllIds();
	    for (Long rid : resourceIDs){
			buildResourceIndex(rid);
	    }
	}
	public List<Long> search(String q, Long userId) {
		List<Long> results = new LinkedList<Long>();
		try {
			// make sure lucene searcher is open. Reuse existing one if kept open
	    	openSearcher();
			// do a term query
			BooleanQuery query = new BooleanQuery();
			query.add(new WildcardQuery(normalizeQueryString(q)),  BooleanClause.Occur.MUST);
			// match only public or user owned documents
			query.add(new TermQuery(new Term(FIELD_ACCESS, PUBLIC_ACCESS)),  BooleanClause.Occur.SHOULD);
			query.add(new TermQuery(new Term(FIELD_ACCESS, userId.toString())),  BooleanClause.Occur.SHOULD);
			query.setMinimumNumberShouldMatch(1);
			
			TopDocCollector collector = new TopDocCollector(10);
		    searcher.search(query, collector);
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    int numTotalHits = collector.getTotalHits();
			log.debug("Search term[" + q + "] found[" + numTotalHits + "] records");
		    
		    for (ScoreDoc scoreDoc : hits) {
		    	Document doc = searcher.doc(scoreDoc.doc);
				String id = doc.get(FIELD_ID);
			    results.add(Long.parseLong(id));
		    }		    
		    		    
		} catch (Exception e) {
			log.error("Error with FullTextSearch[" + e.getMessage()+"] - returning empty results rather than passing error to user", e);
		}
		return results;	}

	/* (non-Javadoc)
	 * Does the seaching across all published resources
	 * @see org.gbif.provider.service.FullTextSearchManager#search(java.lang.String)
	 */
	public List<Long> search(String q) {
		List<Long> results = new LinkedList<Long>();
		try {
			// make sure lucene searcher is open. Reuse existing one if kept open
	    	openSearcher();
			// do a term query
			BooleanQuery query = new BooleanQuery();
			Query wild = new WildcardQuery(normalizeQueryString(q));
			query.add(wild,  BooleanClause.Occur.MUST);
			// match only public documents
			query.add(new TermQuery(new Term(FIELD_ACCESS, PUBLIC_ACCESS)),  BooleanClause.Occur.MUST);
			
			TopDocCollector collector = new TopDocCollector(10);
		    searcher.search(wild, collector);
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    int numTotalHits = collector.getTotalHits();
			log.debug("Search term[" + q + "] found[" + numTotalHits + "] records");
		    
		    for (ScoreDoc scoreDoc : hits) {
		    	Document doc = searcher.doc(scoreDoc.doc);
				String id = doc.get(FIELD_ID);
			    results.add(Long.parseLong(id));
		    }		    
		    		    
		} catch (Exception e) {
			log.error("Error with FullTextSearch[" + e.getMessage()+"] - returning empty results rather than passing error to user", e);
			
		}
		return results;
	}
	private Term normalizeQueryString(String q){
		// Lucene indexes on lower case it seems
		q = q.toLowerCase();
//		if (!q.endsWith("*")) {
//			q = q + "*";
//		}
		return new Term(FIELD_DATA, q);
	}

	private File getResourceIndexDirectory(Long resourceId){
		return cfg.getResourceDataFile(resourceId, indexDirectoryName);
	}
	public void setIndexDirectoryName(String indexDirectoryName) {
		this.indexDirectoryName = indexDirectoryName;
	}

}
