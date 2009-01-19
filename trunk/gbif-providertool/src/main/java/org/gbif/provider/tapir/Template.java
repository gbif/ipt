package org.gbif.provider.tapir;

import java.util.List;

import org.gbif.provider.tapir.filter.Filter;

public class Template {
	// searchTemplate or inventoryTemplate
	private TapirOperation operation;
	// */filter
	private Filter filter;
	// inventoryTemplate/concepts
	//  or
	// searchTemplate/orderBy/concept
	private List<String> concepts;
	// only for searches
	// searchTemplate/externalOutputModel/@location
	private String model;

	
	public Filter getFilter() {
		return filter;
	}
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public TapirOperation getOperation() {
		return operation;
	}
	public void setOperation(TapirOperation operation) {
		this.operation = operation;
	}
	public List<String> getConcepts() {
		return concepts;
	}
	public void setConcepts(List<String> concepts) {
		this.concepts = concepts;
	}
}
