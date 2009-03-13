package org.gbif.provider.webapp.action.manage;

import java.util.Map;

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.service.ExtensionPropertyManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.webapp.action.BaseAction;
import org.springframework.beans.factory.annotation.Autowired;


public class VocabularyAction extends BaseAction{
	@Autowired
	private ExtensionPropertyManager propertyManager;
	@Autowired
    private ThesaurusManager thesaurusManager;
	
    private Map<String,String> voc;
	private ExtensionProperty prop;
	private Long id;
	
    public String execute() {
    	prop = propertyManager.get(id);
		if (prop!=null && prop.getVocabulary()!=null){
			voc=thesaurusManager.getConceptCodeMap(prop.getVocabulary().getUri(), getLocaleLanguage(), false);
		}
        return SUCCESS;
    }

    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Map<String, String> getVoc() {
		return voc;
	}

	public ExtensionProperty getProp() {
		return prop;
	}
    
    
}
