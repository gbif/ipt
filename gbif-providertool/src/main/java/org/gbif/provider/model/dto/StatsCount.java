package org.gbif.provider.model.dto;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.CompareToBuilder;

public class StatsCount implements Comparable {
	private String label;
	private Long count;
	
	public StatsCount(String label, Long count) {
		super();
		this.label = (label==null ? "???" : label);
		this.count = (count==null ? 0L : count);
	}
	public String getLabel() {
		return label;
	}
	public Long getCount() {
		return count;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("count", this.count).append(
				"label", this.label).toString();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(-1819322925, 1558183721).append(this.label)
				.append(this.count).toHashCode();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof StatsCount)) {
			return false;
		}
		StatsCount rhs = (StatsCount) object;
		return new EqualsBuilder().append(this.label, rhs.label).append(
				this.count, rhs.count).isEquals();
	}
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		StatsCount myClass = (StatsCount) object;
		return new CompareToBuilder()
			.append(this.count, myClass.count)
			.append(this.label, myClass.label)
			.toComparison();
	}

	
}
