/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.service.impl;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.FullTextSearchManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.CSVReader;
import org.gbif.provider.util.XmlContentHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.LockObtainFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.xml.sax.SAXException;

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

/**
 * A Lucene based version of the full text manager.
 * 
 */
public class FullTextSearchManagerLucene implements FullTextSearchManager {
  private static final String FIELD_ID = "id";
  private static final String FIELD_DATA = "data";
  private static final String FIELD_ACCESS = "acl";
  private static final String PUBLIC_ACCESS = "public";
  protected final Log log = LogFactory.getLog(getClass());
  protected String indexDirectoryName = "lucene";

  @Autowired
  protected AppConfig cfg;

  private File indexDir;
  private IndexWriter writer;
  private Searcher searcher;
  private final XmlContentHandler handler = new XmlContentHandler();
  private SAXParser saxParser;
  @Autowired
  @Qualifier("resourceManager")
  private GenericResourceManager<Resource> resourceManager;

  /**
   * @see org.gbif.provider.service.FullTextSearchManager#buildDataResourceIndex(org.gbif.provider.model.DataResource)
   */
  public void buildDataResourceIndex(DataResource resource) {
    IndexWriter w = null;
    try {
      // this is just a quick test
      File dir = getResourceIndexDirectory(resource.getId());
      w = new IndexWriter(dir, new StandardAnalyzer(), true,
          IndexWriter.MaxFieldLength.UNLIMITED);
      // IGNORE the header row
      File data = cfg.getArchiveFile(resource.getId(),
          resource.getCoreMapping().getExtension());
      log.info("Building core mapping text index for resource["
          + resource.getId() + "]");
      buildIndex(w, data);
      for (ExtensionMapping view : resource.getExtensionMappings()) {
        log.info("Building extension[" + view.getExtension().getName()
            + "] index for resource[" + resource.getId() + "]");
        File extensionFile = cfg.getArchiveFile(resource.getId(),
            view.getExtension());
        buildIndex(w, extensionFile);
      }
      w.optimize();
    } catch (Exception e) {
      log.error("Error building index: " + e.getMessage(), e);
    } finally {
      try {
        w.close();
      } catch (Exception e) {
        log.error("Error closing index writer: " + e.getMessage(), e);
        e.printStackTrace();
      }
    }
  }

  /**
   * @see org.gbif.provider.service.FullTextSearchManager#buildResourceIndex()
   */
  public void buildResourceIndex() {
    // go through all resources and analyze the metadata xml files
    // to avoid indexing xml tags, use an uber simple SAX parser that just
    // extracts all element & attribute content
    List<Long> resourceIDs = resourceManager.getAllIds();
    for (Long rid : resourceIDs) {
      buildResourceIndex(rid);
    }
  }

  public void buildResourceIndex(Long resourceId) {
    Resource resource = resourceManager.get(resourceId);
    if (resource == null) {
      throw new IllegalArgumentException("Resource " + resourceId
          + " doesn't exist");
    }
    try {
      File eml = cfg.getEmlFile(resourceId);
      log.info("Building resource metadata text index for resource["
          + resourceId + "]");
      getSAXParser().parse(eml, handler);
      // make sure lucene writer is open. Reuse existing one if kept open
      openWriter();
      // if resource was indexed before remove the existing index document
      Query query = new TermQuery(new Term(FIELD_ID, resource.getGuid()));
      writer.deleteDocuments(query);
      // create new index document
      Document doc = new Document();
      doc.add(new Field(FIELD_ID, resource.getGuid(), Field.Store.YES,
          Field.Index.NOT_ANALYZED));
      doc.add(new Field(FIELD_DATA, handler.getContent(), Field.Store.NO,
          Field.Index.ANALYZED));
      // store publication status
      String acl = resource.getCreator().getId().toString();
      if (resource.isPublic()) {
        acl += " " + PUBLIC_ACCESS;
      }
      doc.add(new Field(FIELD_ACCESS, acl, Field.Store.NO, Field.Index.ANALYZED));
      writer.addDocument(doc);
    } catch (Exception e) {
      log.error("Error indexing metadata for resource " + resourceId, e);
    } finally {
      try {
        if (writer != null) {
          writer.commit();
          writer.optimize();
        }
      } catch (CorruptIndexException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void destroy() {
    if (searcher != null) {
      try {
        searcher.close();
      } catch (IOException e) {
        log.error("Error closing Lucene searcher: " + e.getMessage(), e);
      }
    }
    if (writer != null) {
      try {
        writer.close();
      } catch (IOException e) {
        log.error("Error closing Lucene writer: " + e.getMessage(), e);
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
  public List<String> search(Long resourceId, String q) {
    File dir = getResourceIndexDirectory(resourceId);
    IndexReader reader = null;
    Searcher s = null;
    List<String> results = new LinkedList<String>();
    try {
      reader = IndexReader.open(dir);
      s = new IndexSearcher(reader);

      // do a term query
      Term term = normalizeQueryString(q);
      BooleanQuery query = new BooleanQuery();
      Query wild = new WildcardQuery(term);
      query.add(wild, BooleanClause.Occur.MUST);
      query.add(new TermQuery(new Term(FIELD_ACCESS, PUBLIC_ACCESS)),
          BooleanClause.Occur.MUST);

      TopDocCollector collector = new TopDocCollector(10);
      s.search(wild, collector);
      ScoreDoc[] hits = collector.topDocs().scoreDocs;
      int numTotalHits = collector.getTotalHits();
      log.debug("Search term[" + q + "] found[" + numTotalHits + "] records");

      for (ScoreDoc scoreDoc : hits) {
        Document doc = s.doc(scoreDoc.doc);
        String id = doc.get(FIELD_ID);
        results.add(id);
      }

    } catch (Exception e) {
      log.error("Error with FullTextSearch[" + e.getMessage()
          + "] - returning empty results rather than passing error to user", e);

    } finally {
      try {
        if (s != null) {
          s.close();
        }
      } catch (IOException e) {
        log.error("Error closing Lucene searcher: " + e.getMessage(), e);
      }
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        log.error("Error closing Lucene searcher: " + e.getMessage(), e);
      }
    }
    return results;
  }

  /*
   * (non-Javadoc) Does the seaching across all published resources
   * 
   * @see
   * org.gbif.provider.service.FullTextSearchManager#search(java.lang.String)
   */
  public List<String> search(String q) {
    List<String> results = new LinkedList<String>();
    try {
      // make sure lucene searcher is open. Reuse existing one if kept open
      openSearcher();
      // do a term query
      BooleanQuery query = new BooleanQuery();
      Query wild = new WildcardQuery(normalizeQueryString(q));
      query.add(wild, BooleanClause.Occur.MUST);
      // match only public documents
      query.add(new TermQuery(new Term(FIELD_ACCESS, PUBLIC_ACCESS)),
          BooleanClause.Occur.MUST);

      TopDocCollector collector = new TopDocCollector(10);
      searcher.search(wild, collector);
      ScoreDoc[] hits = collector.topDocs().scoreDocs;
      int numTotalHits = collector.getTotalHits();
      log.debug("Search term[" + q + "] found[" + numTotalHits + "] records");

      for (ScoreDoc scoreDoc : hits) {
        Document doc = searcher.doc(scoreDoc.doc);
        String id = doc.get(FIELD_ID);
        results.add(id);
      }

    } catch (Exception e) {
      log.error("Error with FullTextSearch[" + e.getMessage()
          + "] - returning empty results rather than passing error to user", e);
    }
    return results;
  }

  public List<String> search(String q, Long userId) {
    List<String> results = new LinkedList<String>();
    try {
      // make sure lucene searcher is open. Reuse existing one if kept open
      openSearcher();
      // do a term query
      BooleanQuery query = new BooleanQuery();
      query.add(new WildcardQuery(normalizeQueryString(q)),
          BooleanClause.Occur.MUST);
      // match only public or user owned documents
      query.add(new TermQuery(new Term(FIELD_ACCESS, PUBLIC_ACCESS)),
          BooleanClause.Occur.SHOULD);
      query.add(new TermQuery(new Term(FIELD_ACCESS, userId.toString())),
          BooleanClause.Occur.SHOULD);
      query.setMinimumNumberShouldMatch(1);

      TopDocCollector collector = new TopDocCollector(10);
      searcher.search(query, collector);
      ScoreDoc[] hits = collector.topDocs().scoreDocs;
      int numTotalHits = collector.getTotalHits();
      log.debug("Search term[" + q + "] found[" + numTotalHits + "] records");

      for (ScoreDoc scoreDoc : hits) {
        Document doc = searcher.doc(scoreDoc.doc);
        String id = doc.get(FIELD_ID);
        results.add(id);
      }

    } catch (Exception e) {
      log.error("Error with FullTextSearch[" + e.getMessage()
          + "] - returning empty results rather than passing error to user", e);
    }
    return results;
  }

  public void setIndexDirectoryName(String indexDirectoryName) {
    this.indexDirectoryName = indexDirectoryName;
  }

  /**
   * Builds (appends) to the actual index
   */
  private void buildIndex(IndexWriter writer, File data)
      throws FileNotFoundException, IOException, CorruptIndexException {
    CSVReader reader = new CSVReader(new BufferedReader(new FileReader(data)));
    String[] line;
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
      Field id = new Field(FIELD_ID, line[0], Field.Store.YES,
          Field.Index.NOT_ANALYZED);
      doc.add(id);
      doc.add(new Field(FIELD_DATA, sb.toString(), Field.Store.NO,
          Field.Index.ANALYZED));
      writer.addDocument(doc);

      if (log.isDebugEnabled() && count % 1000 == 0) {
        log.debug("Indexed[" + count + "], last id[" + id + "], last data["
            + sb.toString() + "]");
      }
      count++;
    }
  }

  private File getIndexDir() {
    if (indexDir == null) {
      indexDir = new File(cfg.getDataDir(), indexDirectoryName);
    }
    return indexDir;
  }

  private File getResourceIndexDirectory(Long resourceId) {
    return cfg.getResourceDataFile(resourceId, indexDirectoryName);
  }

  private SAXParser getSAXParser() throws ParserConfigurationException,
      SAXException {
    if (saxParser == null) {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      saxParser = factory.newSAXParser();
      log.info("Lucene SAXParser created");
    }
    return saxParser;
  }

  private Term normalizeQueryString(String q) {
    // Lucene indexes on lower case it seems
    q = q.toLowerCase();
    if (!q.endsWith("*")) {
      q = q + "*";
    }
    return new Term(FIELD_DATA, q);
  }

  private void openSearcher() throws CorruptIndexException, IOException {
    if (searcher == null) {
      if (writer != null) {
        writer.optimize();
        writer.close();
        writer = null;
      }
      searcher = new IndexSearcher(IndexReader.open(getIndexDir()));
    }
  }

  private void openWriter() throws CorruptIndexException,
      LockObtainFailedException, IOException {
    if (writer == null) {
      if (searcher != null) {
        searcher.close();
        searcher = null;
      }
      writer = new IndexWriter(getIndexDir(), new StandardAnalyzer(), true,
          IndexWriter.MaxFieldLength.UNLIMITED);
    }
  }

}
