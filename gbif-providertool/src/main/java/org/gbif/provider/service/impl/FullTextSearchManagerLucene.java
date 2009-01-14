/**
 * 
 */
package org.gbif.provider.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.service.FullTextSearchManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.CSVReader;

/**
 * A Lucene based version of the full text manager
 * @author tim
 */
public class FullTextSearchManagerLucene implements FullTextSearchManager {
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see org.gbif.provider.service.FullTextSearchManager#buildDataResourceIndexes(org.gbif.provider.model.DataResource)
	 */
	public void buildDataResourceIndexes(DataResource resource) {
		IndexWriter writer = null;
		try {
			// this is just a quick test
			File data = AppConfig.getResourceDataFile(resource.getId(), "data-darwin_core.txt");
			
			CSVReader reader = new CSVReader(new BufferedReader(new FileReader(data)));
			String [] line;
			// IGNORE the header row
			line = reader.readNext();
			
			String indexDir = AppConfig.getResourceDataDir(resource.getId()).getAbsolutePath()+"/lucene";
			writer = new IndexWriter(indexDir, new StandardAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
						
			// Each row becomes a Lucene document
			int row=1;
			while ((line = reader.readNext()) != null) {
				Document doc = new Document();
				doc.add(new Field("id", line[0], Field.Store.YES, Field.Index.NOT_ANALYZED));
				writer.addDocument(doc);
				if (row%1000 == 0) {
					log.info("Added rows[" + row + "] to index[" + indexDir + "]");
				}
				row++;
			}		
			writer.optimize();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @see org.gbif.provider.service.FullTextSearchManager#buildResourceIndexes()
	 */
	public void buildResourceIndexes() {
		// TODO Auto-generated method stub
	}
}
