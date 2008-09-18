package org.gbif.provider.webapp.action;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;

public class ForwardAction extends BaseResourceAction{
	private String guid;
	
	public String execute(){
		assert(resource_id!=null);
		resource=resourceManager.get(resource_id);
		if (resource instanceof OccurrenceResource) {
			return OCCURRENCE;
		}else if (resource instanceof ChecklistResource) {
			return TAXON;
		}else{
			return METADATA;
		}
	}

	public String resource(){
		return execute();
	}
	
	public String detail(){
		assert(resource_id!=null);
		assert(guid!=null);
		return execute();
	}

	
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}
	
}
