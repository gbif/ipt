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
	private boolean empty=true;
	private boolean alpha=false;
	private String val;
	private String uri;
	private Long id;
	
    public String execute() {
    	prop = propertyManager.get(id);
		if (prop!=null && prop.getVocabulary()!=null){
			voc=thesaurusManager.getConceptCodeMap(prop.getVocabulary().getUri(), getLocaleLanguage(), false);
		}
        return SUCCESS;
    }

    public String voc() {
		voc=thesaurusManager.getConceptCodeMap(uri, getLocaleLanguage(), alpha);
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

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setAlpha(boolean alpha) {
		this.alpha = alpha;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}
	
}
