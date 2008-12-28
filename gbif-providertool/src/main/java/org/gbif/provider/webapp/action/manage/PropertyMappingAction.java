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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.model.Transformation;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.model.voc.TransformationType;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.service.TransformationManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class PropertyMappingAction extends BaseDataResourceAction implements Preparable{
	private static final long serialVersionUID = 14321432161l;
	@Autowired
    private SourceInspectionManager sourceInspectionManager;
	@Autowired
    private SourceManager sourceManager;
	@Autowired
	@Qualifier("viewMappingManager")
    private GenericManager<ViewMappingBase> viewMappingManager;
	@Autowired
	private ExtensionManager extensionManager;
	@Autowired
	@Qualifier("propertyMappingManager")
    private GenericManager<PropertyMapping> propertyMappingManager;
	@Autowired
    private ThesaurusManager thesaurusManager;
	@Autowired
    private TransformationManager transformationManager;
	
	// persistent stuff
	private Long mid;
	private Long eid;
	private Long sid;
	private ViewMappingBase view;
    private List<PropertyMapping> mappings;
	// transformationID for term mapping forwarding only
	private Long tid;
	private Integer mappings_idx;
	// temp stuff
    private List<String> sourceColumns;
	private Map<Long, Map<String,String>> vocs = new HashMap<Long, Map<String,String>>();	

	@Override
	public void prepare(){
		super.prepare();
        if (mid != null) {
    		// get existing view mapping
        	view = viewMappingManager.get(mid);
        	if (view != null && view.getSource() == null && sid != null){
        		// this is probably the default core mapping without a source assigned yet.
            	view.setSource(sourceManager.get(sid));
            	viewMappingManager.save(view);
        	}
        }else if (eid != null && sid != null) {
        	// create new view mapping
        	view = new ViewExtensionMapping();
        	view.setResource(resource);
        	view.setExtension(extensionManager.get(eid));
        	view.setSource(sourceManager.get(sid));
        	viewMappingManager.save(view);
        	mid = view.getId();
        }else{
        	log.warn("No view mapping could be loaded or created");
        }
        // generate basic column mapping options
		try {
	        sourceColumns = sourceInspectionManager.getHeader(view.getSource());
		} catch (Exception e) {
			sourceColumns = new ArrayList<String>();
			log.debug("Cant read datasource column headers", e);
		}
        // prepare list of property mappings to create form with and to be filled by params interceptor
		mappings = new ArrayList<PropertyMapping>();
        int filledMappings = 0;
    	for (ExtensionProperty prop : view.getExtension().getProperties()){
        	if (prop == null){
        		continue;
        	}
    		// is this property mapped already?
    		if (view.hasMappedProperty(prop)){
    			// add existing mapping
            	mappings.add(view.getMappedProperty(prop));
            	filledMappings++;
    		}else{
    			// create new empty one. Remember to link them to the ViewMapping before they get saved
        		PropertyMapping propMap = PropertyMapping.newInstance(prop);
            	mappings.add(propMap);
    		}
    		
    		// create vocabulary drop downs
    		if (prop.getVocabulary()!=null){
    			ThesaurusVocabulary voc = prop.getVocabulary();
    			vocs.put(prop.getId(), thesaurusManager.getConceptCodeMap(voc.getUri(), getLocaleLanguage(), false));
    		}
    	}
		log.debug(mappings.size() + " mappings prepared with "+filledMappings+" existing ones");
        
		// if this mapping is still empty try to automap
		if (view.getMappedProperties().size()<1){
			// regex pattern to normalise property names
			Pattern p = Pattern.compile("[\\s_-]");
			Matcher m = null;
			int autoCount = 0;
			for (PropertyMapping pm : mappings){
				if (!pm.isEmpty()){
					// they should all be empty, but just in case...
					continue;
				}
				m = p.matcher(pm.getProperty().getName());
				String propName = m.replaceAll("");
				for (String col : sourceColumns){
					m = p.matcher(col);
					String colName = m.replaceAll("");
					if (propName.equalsIgnoreCase(colName)){
						pm.setColumn(col);
						autoCount++;
						break;
					}
				}
			}
			log.info("Automapping of columns found "+autoCount+" matching properties");
	        saveMessage("Automapping of columns found "+autoCount+" matching properties");
		}
	}

	public String execute(){
		return SUCCESS;
	}

	public String save() throws Exception {
        if (cancel != null) {
            return "cancel";
        }
        if (delete!= null) {
            return delete();
        }
        // update property mapping values
        for (PropertyMapping pm : mappings){
        	if (pm !=null && pm.getColumn() !=null){
        		String key = StringUtils.trimToEmpty(pm.getColumn());
        	}
        	// save non-empty property mappings
        	if (!pm.isEmpty()){
        		view.addPropertyMapping(pm);
        	// and remove empty ones that are still persistent
        	}else if(pm.getId()!=null){
        		view.removePropertyMapping(pm);
        		propertyMappingManager.remove(pm);
        	}
        }
        // cascade-save view mapping
        view = viewMappingManager.save(view);
        return SUCCESS;
    }
	
	public String termMapping() throws Exception{
		save();
		if (mappings_idx!= null){
			PropertyMapping pm = mappings.get(mappings_idx);
			mid = pm.getViewMapping().getId();
			tid = pm.getTermTransformationId();
			if (tid==null){
				// create new transformation
				Transformation trans = new Transformation();
				trans.setType(TransformationType.Vocabulary);
				trans.setResource(resource);
				trans.setSource(pm.getViewMapping().getSource());
				trans.setColumn(pm.getColumn());
				if (pm.getProperty().getVocabulary() != null){
					trans.setVoc(pm.getProperty().getVocabulary());
				}
				transformationManager.save(trans);
				tid = trans.getId();
				pm.setTermTransformation(trans);
				propertyMappingManager.save(pm);
			}
			return "terms";
		}
		return ERROR;
	}
	
	public String delete(){
		resource.removeExtensionMapping(view);
        viewMappingManager.remove(view);
        return SUCCESS;
	}
	
	

	public List<PropertyMapping> getMappings() {
		return mappings;
	}
	public void setMappings(List<PropertyMapping> mappings) {
		this.mappings = mappings;
	}

	public Long getMid() {
		return mid;
	}

	public void setMid(Long mid) {
		this.mid = mid;
	}

	public Long getEid() {
		return eid;
	}

	public void setEid(Long eid) {
		this.eid = eid;
	}
	
	public List<String> getColumnOptions() {
		return sourceColumns;
	}

	public ViewMappingBase getView() {
		return view;
	}

	public void setView(ViewMappingBase view) {
		this.view = view;
	}

	public List<String> getSourceColumns() {
		return sourceColumns;
	}

	public Long getSid() {
		return sid;
	}

	public void setSid(Long sid) {
		this.sid = sid;
	}

	public Map<Long, Map<String, String>> getVocs() {
		return vocs;
	}

	public void setMappings_idx(Integer mappings_idx) {
		this.mappings_idx = mappings_idx;
	}

	public Long getTid() {
		return tid;
	}

}
