package org.gbif.provider.model.dto;

import java.util.List;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.gbif.provider.model.ExtensionProperty;

public class ValueListCount {
	private List<Object> values;
	private Long count;
	
	public ValueListCount(List<Object> values) {
		super();
		this.values = values;
	}
	public ValueListCount(Long count, List<Object> values) {
		super();
		this.values = values;
		this.count = count;
	}
	
	public List<Object> getValues() {
		return values;
	}
	public void setValues(List<Object> values) {
		this.values = values;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	
}
