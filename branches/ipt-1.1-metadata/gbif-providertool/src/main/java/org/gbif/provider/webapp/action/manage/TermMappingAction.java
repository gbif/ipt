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
package org.gbif.provider.webapp.action.manage;

import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.TermMapping;
import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.model.Transformation;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.TermMappingManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.service.TransformationManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;

import com.opensymphony.xwork2.Preparable;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class TermMappingAction extends BaseDataResourceAction implements
    Preparable {
  private static final long serialVersionUID = 14321432161L;
  @Autowired
  private TransformationManager transformationManager;
  @Autowired
  private TermMappingManager termMappingManager;
  @Autowired
  private ThesaurusManager thesaurusManager;
  @Autowired
  private SourceInspectionManager sourceInspectionManager;

  // persistent stuff
  private Long tid;
  private Transformation transformation;
  private List<TermMapping> termMappings;
  // target terms
  private ThesaurusVocabulary voc;
  private Map<String, String> concepts;
  // term source
  private SourceBase source;
  private String column;
  // original actions for save&cancel
  private String origin = "mappings";
  private Long mid;

  public String delete() {
    return SUCCESS;
  }

  @Override
  public String execute() {
    return SUCCESS;
  }

  public String getColumn() {
    return column;
  }

  public Map<String, String> getConcepts() {
    return concepts;
  }

  public Long getMid() {
    return mid;
  }

  public String getOrigin() {
    return origin;
  }

  public SourceBase getSource() {
    return source;
  }

  public List<TermMapping> getTermMappings() {
    return termMappings;
  }

  public Long getTid() {
    return tid;
  }

  public Transformation getTransformation() {
    return transformation;
  }

  public ThesaurusVocabulary getVoc() {
    return voc;
  }

  @Override
  public void prepare() {
    super.prepare();
    if (tid != null) {
      transformation = transformationManager.get(tid);
      termMappings = termMappingManager.getTermMappings(tid);
      // get existing property mapping
      voc = transformation.getVoc();
      concepts = thesaurusManager.getConceptCodeMap(voc.getUri(),
          getLocaleLanguage(), false);
      source = transformation.getSource();
      column = transformation.getColumn();
    }
    if (mid != null) {
      origin = "propMapping";
    }
  }

  public String save() throws Exception {
    if (cancel != null) {
      return "cancel";
    }
    if (delete != null) {
      return delete();
    }
    termMappingManager.saveAll(termMappings);
    termMappingManager.flush();
    return SUCCESS;
  }

  public String scanSource() {
    try {
      List<String> terms = new ArrayList<String>(
          sourceInspectionManager.getDistinctValues(source, column));
      // first cross check existing mappings with the new list of terms
      Iterator<TermMapping> itr = termMappings.iterator();
      while (itr.hasNext()) {
        TermMapping tm = itr.next();
        if (terms.contains(tm.getTerm())) {
          // term already in mappings
          terms.remove(tm.getTerm());
        } else {
          // term doesnt exist anymore. remove mapping
          termMappingManager.remove(tm);
          itr.remove();
        }
      }
      // now add the remaining terms as new TermMappings
      for (String t : terms) {
        TermMapping tm = new TermMapping(transformation, t);
        // try to come up with some automatic default mapping
        if (concepts.containsKey(t.toLowerCase())) {
          tm.setTargetTerm(t.toLowerCase());
        } else {
          // or consult thesaurus to find matching concept
          ThesaurusConcept tc = thesaurusManager.getConcept(voc.getUri(), t);
          if (tc != null) {
            tm.setTargetTerm(tc.getIdentifier());
          }
        }
        termMappingManager.save(tm);
        termMappings.add(tm);
      }
      // finally sort mappings
      Collections.sort(termMappings);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return SUCCESS;
  }

  public void setMid(Long mid) {
    this.mid = mid;
  }

  public void setTermMappings(List<TermMapping> termMappings) {
    this.termMappings = termMappings;
  }

  public void setTid(Long tid) {
    this.tid = tid;
  }

  public void setTransformation(Transformation transformation) {
    this.transformation = transformation;
  }

}
