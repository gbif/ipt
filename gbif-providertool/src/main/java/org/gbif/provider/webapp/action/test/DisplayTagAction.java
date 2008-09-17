package org.gbif.provider.webapp.action.test;

import java.util.List;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.webapp.action.BaseAction;


public class DisplayTagAction extends BaseAction{
	private static OccurrenceResource resource = new OccurrenceResource();
	private List<Taxon> taxa = new java.util.ArrayList<Taxon>();

	public String execute(){
		Taxon plants = newTaxon("Plantae");
		Taxon pinales = newTaxon("Pinales",plants);
		Taxon asterales= newTaxon("Asterales",plants);
		Taxon asteraceae = newTaxon("Asteraceae",asterales);
		Taxon pinaceae = newTaxon("Pinaaceae",pinales);
		Taxon abies = newTaxon("Abies",pinaceae);
		Taxon pinus = newTaxon("Pinus",pinaceae);
		Taxon aster = newTaxon("Aster",asteraceae);
		Taxon crepis= newTaxon("Crepis",asteraceae);
		Taxon crepis_vulg= newTaxon("Crepis vulgaris L.",crepis);
		Taxon crepis_com= newTaxon("Crepis communis L.",crepis);
		taxa.add(crepis_com);
		taxa.add(pinus);
		taxa.add(pinaceae);
		taxa.add(crepis_vulg);
		taxa.add(asterales);
		taxa.add(pinales);
		taxa.add(plants);
		taxa.add(crepis);
		taxa.add(abies);
		taxa.add(asteraceae);
		taxa.add(aster);		
		return SUCCESS;
	}
	
	
	private Taxon newTaxon(String name, Taxon parent){
		Taxon t = new Taxon();
		t.setResource(resource);
		t.setName(name);
		t.setFullname(name);
		t.setParent(parent);
		return t;
	}
	private Taxon newTaxon(String name){
		return newTaxon(name, null);
	}

	public List<Taxon> getTaxa() {
		return taxa;
	}
	
}
