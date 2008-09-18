package org.gbif.provider.model.dto;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.CompareToBuilder;

public class StatsCount implements Comparable {
	private Long id;
	private String label;
	private Object value;
	private Long count;
	
	public StatsCount(Long id, String label, Object value, Long count) {
		super();
		this.id=id;
		this.label = (label==null ? "???" : label);
		this.value= value;
		this.count = (count==null ? 0L : count);
	}
	public StatsCount(String label, Long count) {
		this(null, label, label, count);
	}
	public String getLabel() {
		return label;
	}
	public Long getCount() {
		return count;
	}	
	public Object getValue() {
		return value;
	}
	public Long getId() {
		return id;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("count", this.count).append(
				"label", this.label).toString();
	}
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		StatsCount myClass = (StatsCount) object;
		return new CompareToBuilder()
			.append(myClass.count, this.count)
			.append(this.label, myClass.label)
			.toComparison();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(-385111033, -2132492275).append(this.value)
				.append(this.label).append(this.count).toHashCode();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof StatsCount)) {
			return false;
		}
		StatsCount rhs = (StatsCount) object;
		return new EqualsBuilder().append(this.value, rhs.value).append(
				this.label, rhs.label).append(this.count, rhs.count).isEquals();
	}

	
}
