/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.webapp.action.manage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.TermMapping;
import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.service.TermMappingManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class TermMappingAction extends BaseDataResourceAction implements Preparable{
	private static final long serialVersionUID = 14321432161l;
	@Autowired
    private SourceInspectionManager sourceInspectionManager;
	@Autowired
    private SourceManager sourceManager;
	@Autowired
	@Qualifier("viewMappingManager")
    private GenericManager<ViewMappingBase> viewMappingManager;
	@Autowired
	@Qualifier("propertyMappingManager")
    private GenericManager<PropertyMapping> propertyMappingManager;
	@Autowired
    private ThesaurusManager thesaurusManager;
	@Autowired
	private TermMappingManager termMappingManager;
	
	// persistent stuff
	private Long pmid;
    private PropertyMapping propMapping;
    private List<TermMapping> termMappings;
    private Map<String, String> concepts;
	// temp stuff
    private ThesaurusVocabulary voc;
    private SourceBase source;

    
	@Override
	public void prepare(){
		super.prepare();
        if (pmid != null) {
    		// get existing property mapping
        	propMapping = propertyMappingManager.get(pmid);
        	voc = propMapping.getProperty().getVocabulary();
        	source = propMapping.getViewMapping().getSource();
        	termMappings = termMappingManager.getTermMappings(source.getId(), propMapping.getColumn().getColumnName());
        	concepts = thesaurusManager.getConceptCodeMap(voc.getUri(), getLocaleLanguage(), false);
		}
	}

	public String execute(){
		return SUCCESS;
	}

	public String scanSource(){
		try {
			List<String> terms = new ArrayList<String>(sourceInspectionManager.getDistinctValues(source, propMapping.getColumn()));
			// first cross check existing mappings with the new list of terms
			Iterator<TermMapping> itr = termMappings.iterator();
			while (itr.hasNext()){
				TermMapping tm = itr.next();
				if (terms.contains(tm.getTerm())){
					// term already in mappings
					terms.remove(tm.getTerm());
				}else{
					// term doesnt exist anymore. remove mapping
					termMappingManager.remove(tm);
					itr.remove();
				}
			}
			// now add the remaining terms as new TermMappings
			for (String t : terms){
				TermMapping tm = new TermMapping(source, propMapping.getColumn(), t);
				// try to come up with some automatic default mapping
				if (concepts.containsKey(t)){
					tm.setTargetTerm(t);
				}else{
					// or consult thesaurus to find matching concept
					ThesaurusConcept tc = thesaurusManager.getConcept(voc.getUri(), t);
					if (tc!=null){
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

	public String save() throws Exception {
        if (cancel != null) {
            return "cancel";
        }
        if (delete!= null) {
            return delete();
        }
        termMappingManager.saveAll(termMappings);
        termMappingManager.flush();
        return SUCCESS;
    }	
	
	public String delete(){
        return SUCCESS;
	}

	public Long getPmid() {
		return pmid;
	}

	public void setPmid(Long pmid) {
		this.pmid = pmid;
	}

	public Long getMid() {
		return propMapping.getViewMapping().getId();
	}

	public PropertyMapping getPropMapping() {
		return propMapping;
	}

	public Map<String, String> getConcepts() {
		return concepts;
	}
	
	public List<TermMapping> getTermMappings() {
		return termMappings;
	}

	public void setTermMappings(List<TermMapping> termMappings) {
		this.termMappings = termMappings;
	}

	public ThesaurusVocabulary getVoc() {
		return voc;
	}

	public SourceBase getSource() {
		return source;
	}
		
}
