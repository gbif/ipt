package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.TaxonManager;
import org.hibernate.Session;

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

	/* Delete all taxa of a given resource, thereby updating all darwin core records for the same resource (setting taxon=null)
	 * (non-Javadoc)
	 * @see org.gbif.provider.service.TaxonManager#deleteAll(org.gbif.provider.model.OccurrenceResource)
	 */
	public int deleteAll(OccurrenceResource resource) {
		// use DML-style HQL batch updates
		// http://www.hibernate.org/hib_docs/reference/en/html/batch.html
		Session session = getSession();
		
		// first remove taxa from dwcore records
		String hqlUpdate = "update DarwinCore dwc SET dwc.taxon=null WHERE dwc.resource = :resource";
		session.createQuery( hqlUpdate ).setEntity("resource", resource).executeUpdate();
		
		// now delete taxa
		hqlUpdate = "delete Taxon tax WHERE tax.resource = :resource";
		int count = session.createQuery( hqlUpdate ).setEntity("resource", resource).executeUpdate();
		
		log.info(String.format("Removed %s taxa bound to resource %s", count, resource.getTitle()));
		return count;
	}


}
