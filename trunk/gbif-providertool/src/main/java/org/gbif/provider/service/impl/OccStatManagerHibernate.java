package org.gbif.provider.service.impl;

import java.util.List;
import java.util.Map;

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.OccStatByRegionAndTaxon;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.OccStatManager;
import org.gbif.provider.webapp.action.portal.DwcAction;
import org.springframework.transaction.annotation.Transactional;

public class OccStatManagerHibernate extends GenericResourceRelatedManagerHibernate<OccStatByRegionAndTaxon> implements OccStatManager{

	public OccStatManagerHibernate(){
		super(OccStatByRegionAndTaxon.class);
	}

	@Transactional(readOnly = false)
	public void updateRegionAndTaxonStats(OccurrenceResource resource) {		
		this.removeAll(resource);
		List<OccStatByRegionAndTaxon> stats = getSession().createQuery("select new OccStatByRegionAndTaxon(res, t, r, count(d), min(d.location.latitude), min(d.location.longitude), max(d.location.latitude), max(d.location.longitude)) from DarwinCore d  join d.resource res  left join d.taxon t  left join d.region r WHERE d.resource=:resource  GROUP BY res, t, r")
			.setEntity("resource", resource)
			.list();
		log.debug(String.format("Created %s new RegionAndTaxon occurrence stats for resource %s", stats.size(), resource.getId()));
		this.saveAll(stats);
		this.flush();
		// also update region.numOcc and Taxon.numOcc
		// per region
		List<Object[]> counts = getSession().createQuery("select r, sum(s.numOcc), min(s.bbox.min.latitude), min(s.bbox.min.longitude), max(s.bbox.max.latitude), max(s.bbox.max.longitude) " +
				" from Region r, OccStatByRegionAndTaxon s right join s.region rd" + // 
				" WHERE rd.lft >= r.lft and rd.rgt <= r.rgt and rd.resource=:resource and r.resource=:resource " +  // 
				" GROUP BY r")
		.setEntity("resource", resource)
		.list();
		log.debug(String.format("Updating %s Region occurrence counts for resource %s", counts.size(), resource.getId()));
		for (Object[] row : counts){
			Region r = (Region) row[0];
			Long cnt = (Long) row[1];
			if (cnt==null){
				cnt=0l;
			}
			r.setOccTotal(cnt.intValue());
			// y=latitude, x=longitude
			// BBox: Double minY, Double minX,   Double maxY, Double maxX
			r.setBbox(new BBox((Double)row[2], (Double)row[3], (Double)row[4], (Double)row[5]));
			this.universalSave(r);
		}
		// per taxon. Include all descendants!
		counts = getSession().createQuery("select t, sum(s.numOcc), min(s.bbox.min.latitude), min(s.bbox.min.longitude), max(s.bbox.max.latitude), max(s.bbox.max.longitude) " +
				" from Taxon t, OccStatByRegionAndTaxon s right join s.taxon td " +
				" WHERE td.lft >= t.lft and td.rgt <= t.rgt and td.resource=:resource and t.resource=:resource " +
				" GROUP BY t")
		.setEntity("resource", resource)
		.list();
		log.debug(String.format("Updating %s Taxon occurrence counts for resource %s", counts.size(), resource.getId()));
		for (Object[] row : counts){
			Taxon t = (Taxon) row[0];
//			if (t.getDwcRank()!=null && t.getDwcRank().compareTo(Rank.Genus)==0){
//				System.out.println("genus");
//			}
//			if (t.getDwcRank() != null && t.getDwcRank().compareTo(Rank.Family)==0){
//				System.out.println("family");
//			}
			Long cnt = (Long) row[1];
			if (cnt==null){
				cnt=0l;
			}
			t.setOccTotal(cnt.intValue());
			// y=latitude, x=longitude
			t.setBbox(new BBox((Double)row[2], (Double)row[3], (Double)row[4], (Double)row[5]));
			this.universalSave(t);
		}
		this.flush();
	}

}
