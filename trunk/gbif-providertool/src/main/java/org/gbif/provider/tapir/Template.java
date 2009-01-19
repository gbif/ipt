package org.gbif.provider.tapir;

import org.gbif.provider.tapir.filter.Filter;

public class Template {
	private Filter filter;
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
	
	
}
