package org.gbif.provider.webapp.action.portal;


import java.util.List;

import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.Resource;
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
    // request parameters
    private Long id;
    private Boolean human;
	 
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
    		if (human){
        		annotations=annotationManager.getAllHuman(resource_id);
    		}else{
        		annotations=annotationManager.getAll(resource_id);
    		}
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
	public void setHuman(Boolean human) {
		this.human = human;
	}

}