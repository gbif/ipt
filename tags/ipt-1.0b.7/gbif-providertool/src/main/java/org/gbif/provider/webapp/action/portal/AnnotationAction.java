package org.gbif.provider.webapp.action.portal;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.AnnotationType;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.AnnotationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.gbif.provider.webapp.action.BaseResourceAction;

import com.opensymphony.xwork2.Preparable;

public class AnnotationAction extends BaseMetadataResourceAction{
	@Autowired
	private AnnotationManager annotationManager;
    private List<Annotation> annotations;
    private Annotation annotation;
	private Map<String, String> annotationTypes = translateI18nMap(new HashMap<String, String>(AnnotationType.htmlSelectMap));
    // request parameters
    private Long id;
    private String annotationType;
	 
    public String execute(){
    	prepare();
    	if (resource_id !=null && guid!= null){
    		annotations=annotationManager.getByRecord(resource_id, guid);
    	}
		return SUCCESS;
    }
    public String list(){
    	prepare();
    	if (resource !=null){
    		if (StringUtils.trimToNull(annotationType)!=null){
        		annotations=annotationManager.getByType(resource_id, annotationType.toString());
    		}else{
        		annotations=annotationManager.getAll(resource_id);
    		}
    	}
		return SUCCESS;
    }

    public String show(){
    	if (id !=null){
    		annotation=annotationManager.get(id);
    	}
		return SUCCESS;
    }

    
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public Annotation getAnnotation() {
		return annotation;
	}
	public String getAnnotationType() {
		return annotationType;
	}
	public void setAnnotationType(String annotationType) {
		this.annotationType = annotationType;
	}
	public Map<String, String> getAnnotationTypes() {
		return annotationTypes;
	}

}