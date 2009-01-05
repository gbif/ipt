package org.gbif.provider.model.dto;

import org.apache.commons.lang.builder.CompareToBuilder;

public class CommonName implements Comparable {
	public String name;
	public String lang;
	public String region;

	public CommonName(String name, String lang, String region) {
		super();
		this.name = name;
		this.lang = lang;
		this.region = region;
	}

	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		CommonName myClass = (CommonName) object;
		return new CompareToBuilder().append(this.lang, myClass.lang).append(
				this.region, myClass.region).append(this.name, myClass.name)
				.toComparison();
	}
	
}
