package org.gbif.provider.webapp.action.portal;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.ExtensionRecordsWrapper;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.NamespaceRegistry;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class AnnotationAction extends org.gbif.provider.webapp.action.BaseResourceAction<Resource>{
	@Autowired
	private AnnotationManager annotationManager;
    private Long id;
    private String guid;
    private List<Annotation> annotations;
    private Annotation annotation;
	 
    public String execute(){
    	if (id !=null){
    		annotation=annotationManager.get(id);
    	}
		return SUCCESS;
    }
    public String record(){
    	if (resource_id !=null && guid!= null){
    		annotations=annotationManager.getByRecord(resource_id, guid);
    	}
		return SUCCESS;
    }
    public String list(){
    	if (resource !=null){
    		annotations=annotationManager.getAll(resource_id);
    	}
		return SUCCESS;
    }

    
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

}