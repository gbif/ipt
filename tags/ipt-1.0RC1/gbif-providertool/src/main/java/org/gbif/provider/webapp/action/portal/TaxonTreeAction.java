package org.gbif.provider.webapp.action.portal;


import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.TaxonManager;
import org.springframework.beans.factory.annotation.Autowired;

public class TaxonTreeAction extends BaseTreeAction<Taxon, Rank> {
	@Autowired
    public TaxonTreeAction(TaxonManager taxonManager) {
		super(taxonManager, "taxon");
	}
}