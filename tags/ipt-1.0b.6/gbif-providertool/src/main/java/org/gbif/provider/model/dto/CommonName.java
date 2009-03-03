package org.gbif.provider.model.dto;

import org.apache.commons.lang.builder.CompareToBuilder;

public class CommonName implements Comparable {
	private String name;
	private String lang;
	private String region;

	public CommonName(String name, String lang, String region) {
		super();
		this.name = name;
		this.lang = lang;
		this.region = region;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
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
