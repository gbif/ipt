package org.gbif.provider.sandbox;

import java.util.concurrent.Callable;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.TaxonManager;

public class DummyProcessingTask implements Callable<Integer>{
	private TaxonManager taxonManager;
	private OccurrenceResource resource;
	private Integer counter;
	
	/* Insert 10 dummy taxa linked to the given resource
	 *  (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public Integer call() throws Exception {
		this.counter = 0;
		Taxon tax;
		for (String name : new String[]{"Alba berlina","Hertha bscea", "Bayer leverkusia", "Colonia primera"}){
			tax = new Taxon();
			tax.setFullname(name);
			tax.setResource(resource);
			taxonManager.save(tax);
			counter++;
		}
		return counter;
	}

	public void setResource(OccurrenceResource resource) {
		this.resource = resource;
	}

	public void setTaxonManager(TaxonManager taxonManager) {
		this.taxonManager = taxonManager;
	}

}
