package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.BaseObject;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.TreeNode;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.service.TreeNodeManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ExtensionManagerHibernate extends GenericManagerHibernate<Extension> implements ExtensionManager {
	@Autowired
	private GenericManager<ExtensionProperty> extensionPropertyManager;

	public ExtensionManagerHibernate() {
	        super(Extension.class);
    }

	public void installExtension(Extension extension){
		
	}
	public void removeExtension(Extension extension){
		
	}
}
