package org.gbif.provider.model.dto;

import org.apache.commons.lang.builder.CompareToBuilder;

public class Distribution implements Comparable {
	public String region;
	public String status;
	
	public Distribution(String region, String status) {
		super();
		this.region = region;
		this.status = status;
	}

	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		Distribution myClass = (Distribution) object;
		return new CompareToBuilder().append(this.region, myClass.region)
				.append(this.status, myClass.status).toComparison();
	}
}
