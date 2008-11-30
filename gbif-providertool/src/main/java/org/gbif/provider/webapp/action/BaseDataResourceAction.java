package org.gbif.provider.webapp.action;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.GenericResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class BaseDataResourceAction extends BaseResourceAction {
	protected DataResource dataResource;
	
	@Override
	public void prepare() {
		super.prepare();
		if (resource != null) {
			dataResource = (DataResource) resource;
		}
	}
}
