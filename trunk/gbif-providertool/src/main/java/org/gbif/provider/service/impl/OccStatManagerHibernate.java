package org.gbif.provider.service.impl;

import java.util.List;
import java.util.Map;

import org.gbif.provider.model.OccStatByRegionAndTaxon;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.OccStatManager;

public class OccStatManagerHibernate extends GenericResourceRelatedManagerHibernate<OccStatByRegionAndTaxon> implements OccStatManager{

	public OccStatManagerHibernate(){
		super(OccStatByRegionAndTaxon.class);
	}

	public void updateRegionAndTaxonStats(OccurrenceResource resource) {		
		this.removeAll(resource);
		List<OccStatByRegionAndTaxon> stats = getSession().createQuery("select new OccStatByRegionAndTaxon(res, t, r, count(d)) from DarwinCore d join d.taxon t join d.resource as res join d.region r WHERE d.resource=:resource  GROUP BY res, t, r")
			.setEntity("resource", resource)
			.list();
		log.debug(String.format("Created %s new RegionAndTaxon occurrence stats for resource %s", stats.size(), resource.getId()));
		this.saveAll(stats);
		// also update region.numOcc and Taxon.numOcc
		// per region
		List<Object[]> counts = getSession().createQuery("select r, count(s) from OccStatByRegionAndTaxon s join s.region r  WHERE s.resource=:resource  GROUP BY r")
		.setEntity("resource", resource)
		.list();
		log.debug(String.format("Updating %s Region occurrence counts for resource %s", counts.size(), resource.getId()));
		for (Object[] row : counts){
			Region r = (Region) row[0];
			r.setOccTotal(((Long)row[1]).intValue());
			this.universalSave(r);
		}
		// per taxon
		counts = getSession().createQuery("select r, count(s) from OccStatByRegionAndTaxon s join s.taxon r  WHERE s.resource=:resource  GROUP BY r")
		.setEntity("resource", resource)
		.list();
		log.debug(String.format("Updating %s Taxon occurrence counts for resource %s", counts.size(), resource.getId()));
		for (Object[] row : counts){
			Taxon t = (Taxon) row[0];
			t.setOccTotal(((Long)row[1]).intValue());
			this.universalSave(t);
		}
	}

}
