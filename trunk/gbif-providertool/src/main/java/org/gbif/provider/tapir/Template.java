package org.gbif.provider.tapir;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gbif.provider.tapir.filter.Filter;

public class Template {
	// searchTemplate or inventoryTemplate
	private TapirOperation operation;
	// */filter
	private Filter filter;
	// key   = inventoryTemplate/concepts/concept/@id
	// value = inventoryTemplate/concepts/concept/@tagName
	private Map<String, String> concepts = new LinkedHashMap<String, String>();
	// key   = searchTemplate/orderBy/concept/@id
	// value = searchTemplate/orderBy/concept/@descend
	private Map<String, Boolean> orderBy = new LinkedHashMap<String, Boolean>();
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
	/*
	public void setOperation(String operationAsString) {
		if ("PING".equalsIgnoreCase(operationAsString)) {
			setOperation(TapirOperation.ping);
		} else if ("CAPABILITIES".equalsIgnoreCase(operationAsString)) {
			setOperation(TapirOperation.capabilities);
		} else if ("METADATA".equalsIgnoreCase(operationAsString)) {
			setOperation(TapirOperation.metadata);
		} else if ("INVENTORY".equalsIgnoreCase(operationAsString)) {
			setOperation(TapirOperation.inventory);
		} else if ("SEARCH".equalsIgnoreCase(operationAsString)) {
			setOperation(TapirOperation.search);
		} 
	} 
	*/
	public Map<String, String> getConcepts() {
		return concepts;
	}
	public void setConcepts(Map<String, String> concepts) {
		this.concepts = concepts;
	}
	public Map<String, Boolean> getOrderBy() {
		return orderBy;
	}
	// required by Digester based parsing
	public void setOrderByStringMap(Map<String, String> orderBy) {
		this.orderBy = new LinkedHashMap<String, Boolean>();
		for (String key : orderBy.keySet()) {
			if ("TRUE".equalsIgnoreCase(orderBy.get(key))) {
				this.orderBy.put(key, true);
			} else {
				this.orderBy.put(key, false);
			}
		}
	}
}
