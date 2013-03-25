package org.gbif.provider.service.impl;

import org.gbif.provider.model.OccStatByRegionAndTaxon;

public class OccStatManagerHibernate extends GenericResourceRelatedManagerHibernate<OccStatByRegionAndTaxon> implements org.gbif.provider.service.OccStatManager{

	public OccStatManagerHibernate(){
		super(OccStatByRegionAndTaxon.class);
	}

}
