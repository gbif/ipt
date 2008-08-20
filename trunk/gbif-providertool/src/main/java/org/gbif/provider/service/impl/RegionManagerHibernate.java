package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.hibernate.Session;

public class RegionManagerHibernate extends GenericManagerHibernate<Region> implements RegionManager {

	public RegionManagerHibernate() {
		super(Region.class);
	}

	public List<Region> getChildren(Long resourceId, Region node) {
		List<Region> result = new ArrayList<Region>(); 
		return result;
	}

	public List<Region> getDescendants(Long resourceId, Region node) {
		List<Region> result = new ArrayList<Region>(); 
		return result;
	}

	public List<Region> getParents(Long resourceId, Region node) {
		List<Region> result = new ArrayList<Region>(); 
		return result;
	}

	public List<Region> getRoots(Long resourceId) {
		List<Region> result = new ArrayList<Region>(); 
		return result;
	}

	public int deleteAll(OccurrenceResource resource) {
		// use DML-style HQL batch updates
		// http://www.hibernate.org/hib_docs/reference/en/html/batch.html
		Session session = getSession();
		String hqlUpdate = "delete Region reg WHERE reg.resource = :resource";
		int count = session.createQuery( hqlUpdate )
		        .setEntity("resource", resource)
		        .executeUpdate();
		log.info(String.format("Removed %s regions bound to resource %s", count, resource.getTitle()));
		return count;
	}
}
