package org.gbif.provider.tapir;

import java.util.List;
import java.util.Map;

import org.gbif.provider.tapir.filter.Filter;

public class Template {
	// searchTemplate or inventoryTemplate
	private TapirOperation operation;
	// */filter
	private Filter filter;
	// key   = inventoryTemplate/concepts/concept/@id
	// value = inventoryTemplate/concepts/concept/@tagName
	private Map<String, String> concepts;
	// key   = searchTemplate/orderBy/concept/@id
	// value = searchTemplate/orderBy/concept/@descend
	private Map<String, Boolean> orderBy;
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
	public Map<String, String> getConcepts() {
		return concepts;
	}
	public void setConcepts(Map<String, String> concepts) {
		this.concepts = concepts;
	}
	public Map<String, Boolean> getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(Map<String, Boolean> orderBy) {
		this.orderBy = orderBy;
	}
	
}
