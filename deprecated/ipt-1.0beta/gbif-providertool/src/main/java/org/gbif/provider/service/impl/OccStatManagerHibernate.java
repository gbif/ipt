package org.gbif.provider.service.impl;

import java.util.List;
import java.util.Map;

import org.gbif.provider.model.BBox;
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
		List<OccStatByRegionAndTaxon> stats = getSession().createQuery("select new OccStatByRegionAndTaxon(res, t, r, count(d), min(d.location.latitude), min(d.location.longitude), max(d.location.latitude), max(d.location.longitude)) from DarwinCore d join d.taxon t join d.resource as res join d.region r WHERE d.resource=:resource  GROUP BY res, t, r")
			.setEntity("resource", resource)
			.list();
		log.debug(String.format("Created %s new RegionAndTaxon occurrence stats for resource %s", stats.size(), resource.getId()));
		this.saveAll(stats);
		this.flush();
		// also update region.numOcc and Taxon.numOcc
		// per region
		List<Object[]> counts = getSession().createQuery("select r, sum(s.numOcc), min(s.bbox.min.latitude), min(s.bbox.min.longitude), max(s.bbox.max.latitude), max(s.bbox.max.longitude)  from OccStatByRegionAndTaxon s join s.region r  WHERE s.resource=:resource  GROUP BY r")
		.setEntity("resource", resource)
		.list();
		log.debug(String.format("Updating %s Region occurrence counts for resource %s", counts.size(), resource.getId()));
		for (Object[] row : counts){
			Region r = (Region) row[0];
			r.setOccTotal(((Long)row[1]).intValue());
			// y=latitude, x=longitude
			// BBox: Double minY, Double minX,   Double maxY, Double maxX
			r.setBbox(new BBox((Double)row[2], (Double)row[3], (Double)row[4], (Double)row[5]));
			this.universalSave(r);
		}
		// per taxon
		counts = getSession().createQuery("select r, sum(s.numOcc), min(s.bbox.min.latitude), min(s.bbox.min.longitude), max(s.bbox.max.latitude), max(s.bbox.max.longitude)  from OccStatByRegionAndTaxon s join s.taxon r  WHERE s.resource=:resource  GROUP BY r")
		.setEntity("resource", resource)
		.list();
		log.debug(String.format("Updating %s Taxon occurrence counts for resource %s", counts.size(), resource.getId()));
		for (Object[] row : counts){
			Taxon t = (Taxon) row[0];
			t.setOccTotal(((Long)row[1]).intValue());
			// y=latitude, x=longitude
			t.setBbox(new BBox((Double)row[2], (Double)row[3], (Double)row[4], (Double)row[5]));
			this.universalSave(t);
		}
		this.flush();
	}

}
