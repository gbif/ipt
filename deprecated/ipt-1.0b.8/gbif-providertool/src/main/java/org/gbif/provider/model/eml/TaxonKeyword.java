package org.gbif.provider.model.eml;

import java.io.Serializable;

public class TaxonKeyword implements Serializable{
	private String scientificName;
	private String rank;
	private String commonName;
	
	
	public TaxonKeyword() {
		super();
	}
	public TaxonKeyword(String scientificName, String rank, String commonName) {
		super();
		this.scientificName=scientificName;
		this.rank=rank;
		this.commonName=commonName;
	}
	
	public String getScientificName() {
		return scientificName;
	}
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}
	public String getRank() {
		return rank;
	}
	public void setRank(String rank) {
		this.rank = rank;
	}
	public String getCommonName() {
		return commonName;
	}
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	
}
