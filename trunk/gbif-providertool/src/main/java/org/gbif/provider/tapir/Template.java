package org.gbif.provider.tapir;

import java.util.LinkedHashMap;
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
	private LinkedHashMap<String, String> concepts;
	// key   = searchTemplate/orderBy/concept/@id
	// value = searchTemplate/orderBy/concept/@descend
	private LinkedHashMap<String, Boolean> orderBy;
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
	public LinkedHashMap<String, String> getConcepts() {
		return concepts;
	}
	public void setConcepts(LinkedHashMap<String, String> concepts) {
		this.concepts = concepts;
	}
	public LinkedHashMap<String, Boolean> getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(LinkedHashMap<String, Boolean> orderBy) {
		this.orderBy = orderBy;
	}
	
}
