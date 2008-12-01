package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.TaxonManager;
import org.hibernate.Session;

public class TaxonManagerHibernate extends GenericTreeNodeManagerHibernate<Taxon> implements TaxonManager {

	public TaxonManagerHibernate() {
		super(Taxon.class);
	}

}
