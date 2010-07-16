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
package org.gbif.ipt.model.eml;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Simple POJO container for an ordered list of bibliographic citations.
 */

public class BibliographicCitationSet implements Serializable{

  /**
   *  Generated
   */
  private static final long serialVersionUID = -406468584517868175L;

  /**
   * A keyword or key phrase that concisely describes the resource or is related to the resource. 
   * Each keyword field should contain one and only one keyword (i.e., keywords should not be separated by commas or other delimiters). 
   * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#keyword
   */ 
  protected List<String> bibliographicCitations = Lists.newArrayList();

  /**
   * Default constructor required by Struts2
   */
  public BibliographicCitationSet() {
  } 
  
  /**
   * @param citations to initialise with
   */
  public BibliographicCitationSet(List<String> citations) {
    this.bibliographicCitations = citations;
  }
  
  /**
   * Adds a bibliographic citation to the list.  
   * This was added to simplify the Digester based rules definitions
   * @param citation to add
   */
  public void add(String citation) {
    bibliographicCitations.add(citation);
  }

  /**
   * @return the keywords
   */
  public List<String> getBibliographicCitations() {
    return bibliographicCitations;
  }

  /**
   * @param keywords the keywords to set
   */
  public void setBibliographicCitations(List<String> citations) {
    this.bibliographicCitations = citations;
  }

}
