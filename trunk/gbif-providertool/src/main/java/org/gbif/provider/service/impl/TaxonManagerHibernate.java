package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.TaxonManager;

public class TaxonManagerHibernate extends GenericManagerHibernate<Taxon> implements TaxonManager {

	public TaxonManagerHibernate() {
		super(Taxon.class);
	}

	public List<Taxon> getChildren(Long resourceId, Taxon node) {
		List<Taxon> result = new ArrayList<Taxon>(); 
		return result;
	}

	public List<Taxon> getDescendants(Long resourceId, Taxon node) {
		List<Taxon> result = new ArrayList<Taxon>(); 
		return result;
	}

	public List<Taxon> getParents(Long resourceId, Taxon node) {
		List<Taxon> result = new ArrayList<Taxon>(); 
		return result;
	}

	public List<Taxon> getRoots(Long resourceId) {
		List<Taxon> result = new ArrayList<Taxon>(); 
		return result;
	}


}
