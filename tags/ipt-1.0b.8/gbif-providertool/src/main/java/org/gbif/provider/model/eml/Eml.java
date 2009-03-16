package org.gbif.provider.model.eml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.appfuse.model.User;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.Resource;
import org.gbif.provider.util.AppConfig;

public class Eml implements Serializable{
	private static final long serialVersionUID = 770733523572837495L;
	private transient Resource resource;
	// serialised data
	private int emlVersion = 0;
	private Agent resourceCreator = new Agent();
	private Date pubDate;
	private String language="en";
	private String intellectualRights;
	// keywords
	private List<String> keywords = new ArrayList<String>();
	private GeoKeyword geographicCoverage = new GeoKeyword();  // should be a list really
	private TimeKeyword temporalCoverage = new TimeKeyword();   // should be a list really
	// taxonomy
	private String taxonomicCoverageDescription;
	private TaxonKeyword lowestCommonTaxon = new TaxonKeyword();
	private List<TaxonKeyword> taxonomicClassification = new ArrayList<TaxonKeyword>();
	// methods
	private String methods;
	private String samplingDescription;
	private String qualityControl;
	// other
	private Project researchProject = new Project();
	private String purpose;
	private String maintenance;
	
	public Eml(){
		super();
		this.pubDate = new Date();
		this.resourceCreator.setRole(Role.ORIGINATOR);
	}
	
	
	//
	// DELEGATOR METHODS for resource
	//
	
	public Resource getResource() {
		return resource;
	}

	public String getGuid() {
		return resource.getGuid();
	}

	public int getEmlVersion() {
		return emlVersion;
	}
	public void setEmlVersion(int emlVersion) {
		this.emlVersion = emlVersion;
	}
	public int increaseEmlVersion() {
		this.emlVersion += 1;
		return this.emlVersion;
	}


	public String getAbstract() {
		return resource.getDescription();
	}
	public void setAbstract(String text) {
		resource.setDescription(text);
	}

	public String getLink() {
		return resource.getLink();
	}
	public void setLink(String link) {
		resource.setLink(link);
	}

	public String getTitle() {
		return resource.getTitle();
	}
	public void setTitle(String title) {
		resource.setTitle(title);
	}
	

	

	
	// regular getter/setter
	
	public Date getPubDate() {
		return pubDate;
	}

	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getIntellectualRights() {
		return intellectualRights;
	}

	public void setIntellectualRights(String intellectualRights) {
		this.intellectualRights = intellectualRights;
	}



	public String getTaxonomicCoverageDescription() {
		return taxonomicCoverageDescription;
	}

	public void setTaxonomicCoverageDescription(String taxonomicCoverageDescription) {
		this.taxonomicCoverageDescription = taxonomicCoverageDescription;
	}

	public List<TaxonKeyword> getTaxonomicClassification() {
		return taxonomicClassification;
	}
	
	public void setTaxonomicClassification(
			List<TaxonKeyword> taxonomicClassification) {
		this.taxonomicClassification = taxonomicClassification;
	}

	public String getMethods() {
		return methods;
	}

	public void setMethods(String methods) {
		this.methods = methods;
	}

	public String getSamplingDescription() {
		return samplingDescription;
	}

	public void setSamplingDescription(String samplingDescription) {
		this.samplingDescription = samplingDescription;
	}

	public String getQualityControl() {
		return qualityControl;
	}

	public void setQualityControl(String qualityControl) {
		this.qualityControl = qualityControl;
	}

	public Project getResearchProject() {
		return researchProject;
	}

	public void setResearchProject(Project researchProject) {
		this.researchProject = researchProject;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getMaintenance() {
		return maintenance;
	}

	public void setMaintenance(String maintenance) {
		this.maintenance = maintenance;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
	
	public void addKeyword(String keyword) {
		this.keywords.add(keyword);
	}
	
	// cant replace instance, just modify their properties
	public Agent resourceCreator() {
		return resourceCreator;
	}

	public GeoKeyword geographicCoverage() {
		return geographicCoverage;
	}

	public TimeKeyword temporalCoverage() {
		return temporalCoverage;
	}

	public TaxonKeyword lowestCommonTaxon() {
		return lowestCommonTaxon;
	}

	public Agent getResourceCreator() {
		return resourceCreator;
	}

	public void setResourceCreator(Agent resourceCreator) {
		this.resourceCreator = resourceCreator;
	}

	public GeoKeyword getGeographicCoverage() {
		return geographicCoverage;
	}

	public void setGeographicCoverage(GeoKeyword geographicCoverage) {
		this.geographicCoverage = geographicCoverage;
	}

	public TimeKeyword getTemporalCoverage() {
		return temporalCoverage;
	}

	public void setTemporalCoverage(TimeKeyword temporalCoverage) {
		this.temporalCoverage = temporalCoverage;
	}

	public TaxonKeyword getLowestCommonTaxon() {
		return lowestCommonTaxon;
	}

	public void setLowestCommonTaxon(TaxonKeyword lowestCommonTaxon) {
		this.lowestCommonTaxon = lowestCommonTaxon;
	}


	public void setResource(Resource resource) {
		this.resource = resource;
	}

}
