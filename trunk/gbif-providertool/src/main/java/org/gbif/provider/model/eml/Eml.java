package org.gbif.provider.model.eml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
	private transient File file;
	// serialised data
	private Agent resourceCreator = new Agent();
	private Date pubDate;
	private String language;
	private String intellectualRights;
	// keywords
	private List<String> keywords = new ArrayList<String>();
	private GeoKeyword geographicCoverage = new GeoKeyword();  // should be a list really
	private TimeKeyword temporalCoverage = new TimeKeyword();   // should be a list really
	private String taxonomicSystem;
	private String taxonomicClassification;
	private List<TaxonKeyword> taxonomicCoverage = new ArrayList<TaxonKeyword>();
	// taxonomy - other metadata
	private TaxonKeyword lowestCommonTaxon = new TaxonKeyword();
	private String latestTaxonomicScrutiny;
	// methods
	private String methods;
	private String samplingDescription;
	private String qualityControl;
	// other
	private Project researchProject = new Project();
	private String purpose;
	private String maintenance;
	
	
	private Eml(){
		super();
		this.pubDate = new Date();
	}
	
	public static Eml loadFile(Resource resource, File file) {
		Eml eml = new Eml();
		if (file.exists()){
			// load existing file
			 FileInputStream fis = null;
			 ObjectInputStream in = null;
			 try
			 {
			   fis = new FileInputStream(file);
			   in = new ObjectInputStream(fis);
			   eml = (Eml)in.readObject();
			   in.close();
			 }
			 catch(IOException ex)
			 {
			   ex.printStackTrace();
			 }
			 catch(ClassNotFoundException ex)
			 {
			   ex.printStackTrace();
			 }			
		}else{
			// copy some resource default data that is not delegated (title & description are)
			eml.resourceCreator.setEmail(resource.getContactEmail());
			eml.resourceCreator.setLastName(resource.getContactName());
		}
		eml.file = file;
		eml.resource = resource;
		return eml;
	}	
	
	
	//
	// DELEGATOR METHODS for resource
	//
	
	public Resource getResource() {
		return resource;
	}

	public File getFile() {
		return file;
	}

	public String getGuid() {
		return resource.getGuid();
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

	public String getTaxonomicSystem() {
		return taxonomicSystem;
	}

	public void setTaxonomicSystem(String taxonomicSystem) {
		this.taxonomicSystem = taxonomicSystem;
	}

	public String getTaxonomicClassification() {
		return taxonomicClassification;
	}

	public void setTaxonomicClassification(String taxonomicClassification) {
		this.taxonomicClassification = taxonomicClassification;
	}

	public List<TaxonKeyword> getTaxonomicCoverage() {
		return taxonomicCoverage;
	}

	public void setTaxonomicCoverage(List<TaxonKeyword> taxonomicCoverage) {
		this.taxonomicCoverage = taxonomicCoverage;
	}
	public void addTaxonomicCoverage(TaxonKeyword taxonomicCoverage) {
		this.taxonomicCoverage.add(taxonomicCoverage);
	}
	
	public String getLatestTaxonomicScrutiny() {
		return latestTaxonomicScrutiny;
	}

	public void setLatestTaxonomicScrutiny(String latestTaxonomicScrutiny) {
		this.latestTaxonomicScrutiny = latestTaxonomicScrutiny;
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

}
