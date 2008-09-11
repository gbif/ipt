/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.datasource.ImportRecord;
import org.hibernate.validator.NotNull;


/**
 * The core class for taxon occurrence records with normalised properties used by the webapp.
 * The generated property values can be derived from different extensions like DarwinCore or ABCD
 * but the ones here are used for creating most of the webapp functionality
 * @author markus
 *
 */
@Entity
@Table(name="dwcore"
	, uniqueConstraints = {@UniqueConstraint(columnNames={"localId", "resource_fk"})}
) 

@org.hibernate.annotations.Table(appliesTo = "tableName", indexes = { 	
		@org.hibernate.annotations.Index(name="latitude", columnNames = { "lat" }), 
		@org.hibernate.annotations.Index(name="longitude", columnNames = { "lon" }) 
		})
public class DarwinCore implements CoreRecord, Comparable<DarwinCore>{
	private static I18nLog logdb = I18nLogFactory.getLog(DarwinCore.class);
	public static final Long GEO_EXTENSION_ID = 3l;
	public static final ExtensionProperty LATITUDE_PROP= new ExtensionProperty("http://rs.tdwg.org/dwc/geospatial/DecimalLatitude");
	public static final ExtensionProperty LONGITUDE_PROP= new ExtensionProperty("http://rs.tdwg.org/dwc/geospatial/DecimalLongitude");
	public static final ExtensionProperty GEODATUM_PROP= new ExtensionProperty("http://rs.tdwg.org/dwc/geospatial/GeodeticDatum");

	// for core record
	private Long id;
	@NotNull
	private String localId;
	@NotNull
	private String guid;
	private String link;
	private boolean isDeleted;
	private boolean isProblematic;
	private Date modified;
	@NotNull
	private OccurrenceResource resource;

	// DarinCore derived datatypes. calculated from raw Strings
	@NotNull
	private DarwinCoreTaxonomy tax;
	@NotNull
	private DarwinCoreLocation loc;
	// calculated fields
	private Point location;
	private Taxon taxon;
	private Region region;
	
	// DarwinCore 1.4 elements
	private String basisOfRecord;
	private String institutionCode;
	private String collectionCode;
	private String catalogNumber;
	private String informationWithheld;
	private String remarks;
	// Biological Elements
	private String sex;
	private String lifeStage;
	private String attributes;
	// references elements
	private String imageURL;
	private String relatedInformation;	

	
	public static DarwinCore newInstance(){
		DarwinCore dwc = new DarwinCore();
		dwc.tax = new DarwinCoreTaxonomy();
		dwc.tax.setDwc(dwc);
		dwc.loc = new DarwinCoreLocation();
		dwc.loc.setDwc(dwc);
		dwc.location= new Point();
		return dwc;
	}
	public static DarwinCore newMock(OccurrenceResource resource){
		Random rnd = new Random();
		DarwinCore dwc = DarwinCore.newInstance();
		// populate instance
		dwc.setResource(resource);
		// set unique localId to ensure we can save this record. Otherwise we might get a non unique constraint exception...
		String guid = UUID.randomUUID().toString();
		String localId = rnd.nextInt(99999999)+"";
		dwc.setLocalId(localId);
		dwc.setGuid(guid);
		dwc.setCatalogNumber("rbgk-"+localId+"-x");
		dwc.setBasisOfRecord("PreservedSpecimen");
		dwc.setInstitutionCode("RBGK");
		// location
		dwc.setCountry("PL");
		// taxonomy
		dwc.setScientificName("Abies alba L.");
		dwc.setGenus("Abies");
		dwc.setFamily("Pinaceae");
		return dwc;
	}
	public static DarwinCore newInstance(ImportRecord iRec){
		if (iRec==null){
			throw new NullPointerException();
		}
		DarwinCore dwc = DarwinCore.newInstance();
		dwc.setGuid(iRec.getGuid());
		dwc.setLink(iRec.getLink());
		dwc.setLocalId(iRec.getLocalId());
		dwc.setModified(iRec.getModified());
		dwc.setDeleted(false);
		for (ExtensionProperty prop : iRec.getProperties().keySet()){
			// set all dwc properties apart from:
			// DateLastModified: managed by CoreRecord and this software
			String val = iRec.getPropertyValue(prop);
			String propName = prop.getName();
			if(propName.equals("GlobalUniqueIdentifier")){
				dwc.setGlobalUniqueIdentifier(val);
			}else if(propName.equals("BasisOfRecord")){
				dwc.setBasisOfRecord(val);
			}else if(propName.equals("InstitutionCode")){
				dwc.setInstitutionCode(val);
			}else if(propName.equals("CollectionCode")){
				dwc.setCollectionCode(val);
			}else if(propName.equals("CatalogNumber")){
				dwc.setCatalogNumber(val);
			}else if(propName.equals("InformationWithheld")){
				dwc.setInformationWithheld(val);
			}else if(propName.equals("Remarks")){
				dwc.setRemarks(val);
			}else if(propName.equals("Sex")){
				dwc.setSex(val);
			}else if(propName.equals("LifeStage")){
				dwc.setLifeStage(val);
			}else if(propName.equals("Attributes")){
				dwc.setAttributes(val);
			}else if(propName.equals("ImageURL")){
				dwc.setImageURL(val);
			}else if(propName.equals("RelatedInformation")){
				dwc.setRelatedInformation(val);
			}else if(propName.equals("HigherGeography")){
				dwc.setHigherGeography(val);
			}else if(propName.equals("Continent")){
				dwc.setContinent(val);
			}else if(propName.equals("WaterBody")){
				dwc.setWaterBody(val);
			}else if(propName.equals("IslandGroup")){
				dwc.setIslandGroup(val);
			}else if(propName.equals("Island")){
				dwc.setIsland(val);
			}else if(propName.equals("Country")){
				dwc.setCountry(val);
			}else if(propName.equals("StateProvince")){
				dwc.setStateProvince(val);
			}else if(propName.equals("County")){
				dwc.setCounty(val);
			}else if(propName.equals("Locality")){
				dwc.setLocality(val);
			}else if(propName.equals("MinimumElevationInMeters")){
				dwc.setMinimumElevationInMeters(val);
				// try to convert into proper type
				Integer typedVal = null;
				if (val !=null){
					try {
						typedVal = Integer.valueOf(val);
						dwc.setMinimumElevationInMetersAsInteger(typedVal);
					} catch (NumberFormatException e) {
						dwc.setProblematic(true);
						logdb.warn("log.transform", new String[]{val, "MinimumElevationInMeters", "Integer"});
					}
				}
			}else if(propName.equals("MaximumElevationInMeters")){
				dwc.setMaximumElevationInMeters(val);
				// try to convert into proper type
				Integer typedVal = null;
				if (val !=null){
					try {
						typedVal = Integer.valueOf(val);
						dwc.setMaximumElevationInMetersAsInteger(typedVal);
					} catch (NumberFormatException e) {
						dwc.setProblematic(true);
						logdb.warn("log.transform", new String[]{val, "MaximumElevationInMeters", "Integer"});
					}
				}
			}else if(propName.equals("MinimumDepthInMeters")){
				dwc.setMinimumDepthInMeters(val);
				// try to convert into proper type
				Integer typedVal = null;
				if (val !=null){
					try {
						typedVal = Integer.valueOf(val);
						dwc.setMinimumDepthInMetersAsInteger(typedVal);
					} catch (NumberFormatException e) {
						dwc.setProblematic(true);
						logdb.warn("log.transform", new String[]{val, "MinimumDepthInMeters", "Integer"});
					}
				}
			}else if(propName.equals("MaximumDepthInMeters")){
				dwc.setMaximumDepthInMeters(val);
				// try to convert into proper type
				Integer typedVal = null;
				if (val !=null){
					try {
						typedVal = Integer.valueOf(val);
						dwc.setMaximumDepthInMetersAsInteger(typedVal);
					} catch (NumberFormatException e) {
						dwc.setProblematic(true);
						logdb.warn("log.transform", new String[]{val, "MaximumDepthInMeters", "Integer"});
					}
				}
			}else if(propName.equals("CollectingMethod")){
				dwc.setCollectingMethod(val);
			}else if(propName.equals("ValidDistributionFlag")){
				dwc.setValidDistributionFlag(val);
			}else if(propName.equals("EarliestDateCollected")){
				dwc.setEarliestDateCollected(val);
			}else if(propName.equals("LatestDateCollected")){
				dwc.setLatestDateCollected(val);
			}else if(propName.equals("DayOfYear")){
				dwc.setDayOfYear(val);
			}else if(propName.equals("Collector")){
				dwc.setCollector(val);
			}else if(propName.equals("ScientificName")){
				dwc.setScientificName(val);
			}else if(propName.equals("HigherTaxon")){
				dwc.setHigherTaxon(val);
			}else if(propName.equals("Kingdom")){
				dwc.setKingdom(val);
			}else if(propName.equals("Phylum")){
				dwc.setPhylum(val);
			}else if(propName.equals("Classs")){
				dwc.setClasss(val);
			}else if(propName.equals("Order")){
				dwc.setOrder(val);
			}else if(propName.equals("Family")){
				dwc.setFamily(val);
			}else if(propName.equals("Genus")){
				dwc.setGenus(val);
			}else if(propName.equals("SpecificEpithet")){
				dwc.setSpecificEpithet(val);
			}else if(propName.equals("InfraspecificRank")){
				dwc.setInfraspecificRank(val);
			}else if(propName.equals("InfraspecificEpithet")){
				dwc.setInfraspecificEpithet(val);
			}else if(propName.equals("AuthorYearOfScientificName")){
				dwc.setAuthorYearOfScientificName(val);
			}else if(propName.equals("NomenclaturalCode")){
				dwc.setNomenclaturalCode(val);
			}else if(propName.equals("IdentificationQualifer")){
				dwc.setIdentificationQualifer(val);
			}

		}
		return dwc;
	}
	
	public void updateWithGeoExtension(ExtensionRecord extRec){
		String geodatum = null;
		// tmp raw value
		String val; 
		for (ExtensionProperty prop : extRec){
			// check string coordinates
			if(prop.equals(LATITUDE_PROP)){
				val = extRec.getPropertyValue(prop);
				if (val !=null){
					try {
						getLocation().setLatitude(Float.valueOf(val));
					} catch (NumberFormatException e) {
						setProblematic(true);
						logdb.warn("Couldnt transform value '{0}' for property DecimalLatitude into Float value", val, e);
					} catch (IllegalArgumentException e) {
						setProblematic(true);
						logdb.warn("Latitude value '{0}' is out of allowed range", val, e);
					}
				}
			}
			else if(prop.equals(LONGITUDE_PROP)){
				val = extRec.getPropertyValue(prop);
				if (val !=null){
					try {
						getLocation().setLongitude(Float.valueOf(val));
					} catch (NumberFormatException e) {
						setProblematic(true);
						logdb.warn("Couldnt transform value '{0}' for property DecimalLongitude into Float value", val, e);
					} catch (IllegalArgumentException e) {
						setProblematic(true);
						logdb.warn("Longitude value '{0}' is out of allowed range", val, e);
					}
				}
			}
			else if(prop.equals(GEODATUM_PROP)){
				geodatum=extRec.getPropertyValue(prop);
			}
		}
		getLocation().transformIntoWGS84(geodatum);
	}
			

	
	@Transient
	public Map<String, String> getDataMap(){
		Map<String, String> m = new HashMap<String, String>();
		m.put(ID_COLUMN_NAME, this.getGuid());
		String modified = null;
		if (this.getModified() != null) {
			modified = this.getModified().toString();
		}
		m.put(MODIFIED_COLUMN_NAME, modified);

		m.put("GlobalUniqueIdentifier", getGlobalUniqueIdentifier());
		m.put("BasisOfRecord", getBasisOfRecord());
		m.put("InstitutionCode", getInstitutionCode());
		m.put("CollectionCode", getCollectionCode());
		m.put("CatalogNumber", getCatalogNumber());
		m.put("InformationWithheld", getInformationWithheld());
		m.put("Remarks", getRemarks());
		m.put("Sex", getSex());
		m.put("LifeStage", getLifeStage());
		m.put("Attributes", getAttributes());
		m.put("ImageURL", getImageURL());
		m.put("RelatedInformation", getRelatedInformation());
		m.put("HigherGeography", getHigherGeography());
		m.put("Continent", getContinent());
		m.put("WaterBody", getWaterBody());
		m.put("IslandGroup", getIslandGroup());
		m.put("Island", getIsland());
		m.put("Country", getCountry());
		m.put("StateProvince", getStateProvince());
		m.put("County", getCounty());
		m.put("Locality", getLocality());
		m.put("MinimumElevationInMeters", getMinimumElevationInMeters());
		m.put("MaximumElevationInMeters", getMaximumElevationInMeters());
		m.put("MinimumDepthInMeters", getMinimumDepthInMeters());
		m.put("MaximumDepthInMeters", getMaximumDepthInMeters());
		m.put("CollectingMethod", getCollectingMethod());
		m.put("ValidDistributionFlag", getValidDistributionFlag());
		m.put("EarliestDateCollected", getEarliestDateCollected());
		m.put("LatestDateCollected", getLatestDateCollected());
		m.put("DayOfYear", getDayOfYear());
		m.put("Collector", getCollector());
		m.put("ScientificName", getScientificName());
		m.put("HigherTaxon", getHigherTaxon());
		m.put("Kingdom", getKingdom());
		m.put("Phylum", getPhylum());
		m.put("Classs", getClasss());
		m.put("Order", getOrder());
		m.put("Family", getFamily());
		m.put("Genus", getGenus());
		m.put("SpecificEpithet", getSpecificEpithet());
		m.put("InfraspecificRank", getInfraspecificRank());
		m.put("InfraspecificEpithet", getInfraspecificEpithet());
		m.put("AuthorYearOfScientificName", getAuthorYearOfScientificName());
		m.put("NomenclaturalCode", getNomenclaturalCode());
		m.put("IdentificationQualifer", getIdentificationQualifer());
		return m;
	}
	

	// CORE RECORD
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}	

	@Column(length=128)
	public String getLocalId() {
		return localId;
	}

	public void setLocalId(String localId) {
		this.localId = localId;
	}	

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public boolean isProblematic() {
		return isProblematic;
	}

	public void setProblematic(boolean isProblematic) {
		this.isProblematic = isProblematic;
	}
	
	@ManyToOne
	public OccurrenceResource getResource() {
		return resource;
	}

	public void setResource(OccurrenceResource resource) {
		this.resource = resource;
	}
	

	// OTHER
	// optional = false breaks hibernate IdGeneration somehow... buuh
	// cascade=CascadeType.ALL
    @OneToOne(mappedBy="dwc", fetch = FetchType.LAZY, cascade=CascadeType.ALL) 
	public DarwinCoreTaxonomy getTax() {
    	// shouldnt be the case, but to prevend NPE create assure there is always the loc compound
    	if (tax==null){
    		tax = new DarwinCoreTaxonomy();
    		tax.setDwc(this);
    	}
		return tax;
	}
	public void setTax(DarwinCoreTaxonomy tax) {
		this.tax = tax;
	}
	
	// optional = false breaks hibernate IdGeneration somehow... buuh
    @OneToOne(mappedBy="dwc", fetch = FetchType.LAZY, cascade=CascadeType.ALL) 
	public DarwinCoreLocation getLoc() {
    	// shouldnt be the case, but to prevend NPE create assure there is always the loc compound
    	if (loc==null){
    		loc = new DarwinCoreLocation();
    		loc.setDwc(this);    		
    	}
		return loc;
	}
	public void setLoc(DarwinCoreLocation loc) {
		this.loc = loc;
	}
	
	
	@ManyToOne(optional = true)
	public Taxon getTaxon() {
		return taxon;
	}
	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
	}

	@ManyToOne(optional = true)
	public Region getRegion() {
		return region;
	}
	public void setRegion(Region region) {
		this.region = region;
	}
	
    @AttributeOverrides({
        @AttributeOverride(name="latitude", column = @Column(name="lat")), 
        @AttributeOverride(name="longitude", column = @Column(name="lon")) 
    })
	public Point getLocation() {
    	// to prevent NPE create new empty point in case it doesnt exist yet
    	if (location==null){
    		location=new Point();
    	}
		return location;
	}
	public void setLocation(Point location) {
		this.location = location;
	}
	
	@Transient
	public Float getLatitude() {
		return location.getLatitude();
	}
	@Transient
	public Float getLongitude() {
		return location.getLongitude();
	}
	
	/**
	 * Aliases for set/getGuid() which are part of any core record, not only darwin core
	 * @return
	 */
	@Transient
	public String getGlobalUniqueIdentifier() {
		return guid;
	}
	public void setGlobalUniqueIdentifier(String globalUniqueIdentifier) {
		this.guid = globalUniqueIdentifier;
	}
	/**no need for extra date last modified. Forward to CoreRecord property
	 * @return
	 */
	@Transient
	public Date getDateLastModified() {
		return this.getModified();
	}
	@Column(length=128)
	public String getBasisOfRecord() {
		return basisOfRecord;
	}
	public void setBasisOfRecord(String basisOfRecord) {
		this.basisOfRecord = basisOfRecord;
	}
	@Column(length=128)
	@org.hibernate.annotations.Index(name="inst_code")
	public String getInstitutionCode() {
		return institutionCode;
	}
	public void setInstitutionCode(String institutionCode) {
		this.institutionCode = institutionCode;
	}
	@Column(length=128)
	@org.hibernate.annotations.Index(name="coll_code")
	public String getCollectionCode() {
		return collectionCode;
	}
	public void setCollectionCode(String collectionCode) {
		this.collectionCode = collectionCode;
	}
	@org.hibernate.annotations.Index(name="cat_num")
	public String getCatalogNumber() {
		return catalogNumber;
	}
	public void setCatalogNumber(String catalogNumber) {
		this.catalogNumber = catalogNumber;
	}
	@Lob
	public String getInformationWithheld() {
		return informationWithheld;
	}
	public void setInformationWithheld(String informationWithheld) {
		this.informationWithheld = informationWithheld;
	}
	@Lob
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	@Column(length=64)
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getLifeStage() {
		return lifeStage;
	}
	public void setLifeStage(String lifeStage) {
		this.lifeStage = lifeStage;
	}
	@Lob
	public String getAttributes() {
		return attributes;
	}
	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	@Lob
	public String getRelatedInformation() {
		return relatedInformation;
	}
	public void setRelatedInformation(String relatedInformation) {
		this.relatedInformation = relatedInformation;
	}
	
	
	//
	// Forwarding properties
	//
	
	// LOCATION FORWARDING
	@Transient
	public String getHigherGeography() {
		return getLoc().getHigherGeography();
	}
	public void setHigherGeography(String higherGeography) {
		getLoc().setHigherGeography(higherGeography);
	}
	@Transient
	public String getContinent() {
		return getLoc().getContinent();
	}
	public void setContinent(String continent) {
		getLoc().setContinent(continent);
	}
	@Transient
	public String getWaterBody() {
		return getLoc().getWaterBody();
	}
	public void setWaterBody(String waterBody) {
		getLoc().setWaterBody(waterBody);
	}
	@Transient
	public String getIslandGroup() {
		return getLoc().getIslandGroup();
	}
	public void setIslandGroup(String islandGroup) {
		getLoc().setIslandGroup(islandGroup);
	}
	@Transient
	public String getIsland() {
		return getLoc().getIsland();
	}
	public void setIsland(String island) {
		getLoc().setIsland(island);
	}
	@Transient
	public String getCountry() {
		return getLoc().getCountry();
	}
	public void setCountry(String country) {
		getLoc().setCountry(country);
	}
	@Transient
	public String getStateProvince() {
		return getLoc().getStateProvince();
	}
	public void setStateProvince(String stateProvince) {
		getLoc().setStateProvince(stateProvince);
	}
	@Transient
	public String getCounty() {
		return getLoc().getCounty();
	}
	public void setCounty(String county) {
		getLoc().setCounty(county);
	}
	@Transient
	public String getLocality() {
		return loc.getLocality();
	}
	public void setLocality(String locality) {
		getLoc().setLocality(locality);
	}
	@Transient
	public Integer getMinimumElevationInMetersAsInteger() {
		return getLoc().getMinimumElevationInMetersAsInteger();
	}
	public void setMinimumElevationInMetersAsInteger(Integer minimumElevationInMeters) {
		getLoc().setMinimumElevationInMetersAsInteger(minimumElevationInMeters);
	}
	@Transient
	public Integer getMaximumElevationInMetersAsInteger() {
		return getLoc().getMaximumElevationInMetersAsInteger();
	}
	public void setMaximumElevationInMetersAsInteger(Integer maximumElevationInMeters) {
		getLoc().setMaximumElevationInMetersAsInteger(maximumElevationInMeters);
	}
	@Transient
	public Integer getMinimumDepthInMetersAsInteger() {
		return getLoc().getMinimumDepthInMetersAsInteger();
	}
	public void setMinimumDepthInMetersAsInteger(Integer minimumDepthInMeters) {
		getLoc().setMinimumDepthInMetersAsInteger(minimumDepthInMeters);
	}
	@Transient
	public Integer getMaximumDepthInMetersAsInteger() {
		return getLoc().getMaximumDepthInMetersAsInteger();
	}
	public void setMaximumDepthInMetersAsInteger(Integer maximumDepthInMeters) {
		getLoc().setMaximumDepthInMetersAsInteger(maximumDepthInMeters);
	}
	@Transient
	public String getMinimumElevationInMeters() {
		return getLoc().getMinimumElevationInMeters();
	}
	public void setMinimumElevationInMeters(String minimumElevationInMeters) {
		getLoc().setMinimumElevationInMeters(minimumElevationInMeters);
	}
	@Transient
	public String getMaximumElevationInMeters() {
		return getLoc().getMaximumElevationInMeters();
	}
	public void setMaximumElevationInMeters(String maximumElevationInMeters) {
		getLoc().setMaximumElevationInMeters(maximumElevationInMeters);
	}
	@Transient
	public String getMinimumDepthInMeters() {
		return getLoc().getMinimumDepthInMeters();
	}
	public void setMinimumDepthInMeters(String minimumDepthInMeters) {
		getLoc().setMinimumDepthInMeters(minimumDepthInMeters);
	}
	@Transient
	public String getMaximumDepthInMeters() {
		return getLoc().getMaximumDepthInMeters();
	}
	public void setMaximumDepthInMeters(String maximumDepthInMeters) {
		getLoc().setMaximumDepthInMeters(maximumDepthInMeters);
	}
	@Transient
	public String getCollectingMethod() {
		return getLoc().getCollectingMethod();
	}
	public void setCollectingMethod(String collectingMethod) {
		getLoc().setCollectingMethod(collectingMethod);
	}
	@Transient
	public String getValidDistributionFlag() {
		return getLoc().getValidDistributionFlag();
	}
	public void setValidDistributionFlag(String validDistributionFlag) {
		getLoc().setValidDistributionFlag(validDistributionFlag);
	}
	@Transient
	public String getEarliestDateCollected() {
		return getLoc().getEarliestDateCollected();
	}
	public void setEarliestDateCollected(String earliestDateCollected) {
		getLoc().setEarliestDateCollected(earliestDateCollected);
	}
	@Transient
	public String getLatestDateCollected() {
		return getLoc().getLatestDateCollected();
	}
	public void setLatestDateCollected(String latestDateCollected) {
		getLoc().setLatestDateCollected(latestDateCollected);
	}
	@Transient
	public String getDayOfYear() {
		return getLoc().getDayOfYear();
	}
	public void setDayOfYear(String dayOfYear) {
		getLoc().setDayOfYear(dayOfYear);
	}
	@Transient
	public String getCollector() {
		return getLoc().getCollector();
	}
	public void setCollector(String collector) {
		getLoc().setCollector(collector);
	}


	// TAXONOMY FORWARDING
	
	@Transient
	public String getScientificName() {
		return getTax().getScientificName();
	}
	public void setScientificName(String scientificName) {
		getTax().setScientificName(scientificName);
	}
	@Transient
	public String getHigherTaxon() {
		return getTax().getHigherTaxon();
	}
	public void setHigherTaxon(String higherTaxon) {
		getTax().setHigherTaxon(higherTaxon);
	}
	@Transient
	public String getKingdom() {
		return getTax().getKingdom();
	}
	public void setKingdom(String kingdom) {
		getTax().setKingdom(kingdom);
	}
	@Transient
	public String getPhylum() {
		return getTax().getPhylum();
	}
	public void setPhylum(String phylum) {
		getTax().setPhylum(phylum);
	}
	@Transient
	public String getClasss() {
		return getTax().getClasss();
	}
	public void setClasss(String classs) {
		getTax().setClasss(classs);
	}
	@Transient
	public String getOrder() {
		return getTax().getOrder();
	}
	public void setOrder(String order) {
		getTax().setOrder(order);
	}
	@Transient
	public String getFamily() {
		return getTax().getFamily();
	}
	public void setFamily(String family) {
		getTax().setFamily(family);
	}
	@Transient
	public String getGenus() {
		return getTax().getGenus();
	}
	public void setGenus(String genus) {
		getTax().setGenus(genus);
	}
	@Transient
	public String getSpecificEpithet() {
		return getTax().getSpecificEpithet();
	}
	public void setSpecificEpithet(String specificEpithet) {
		getTax().setSpecificEpithet(specificEpithet);
	}
	@Transient
	public String getInfraspecificRank() {
		return getTax().getInfraspecificRank();
	}
	public void setInfraspecificRank(String infraspecificRank) {
		getTax().setInfraspecificRank(infraspecificRank);
	}
	@Transient
	public String getInfraspecificEpithet() {
		return getTax().getInfraspecificEpithet();
	}
	public void setInfraspecificEpithet(String infraspecificEpithet) {
		getTax().setInfraspecificEpithet(infraspecificEpithet);
	}
	@Transient
	public String getAuthorYearOfScientificName() {
		return getTax().getAuthorYearOfScientificName();
	}
	public void setAuthorYearOfScientificName(String authorYearOfScientificName) {
		getTax().setAuthorYearOfScientificName(authorYearOfScientificName);
	}
	@Transient
	public String getNomenclaturalCode() {
		return getTax().getNomenclaturalCode();
	}
	public void setNomenclaturalCode(String nomenclaturalCode) {
		getTax().setNomenclaturalCode(nomenclaturalCode);
	}
	@Transient
	public String getIdentificationQualifer() {
		return getTax().getIdentificationQualifer();
	}
	public void setIdentificationQualifer(String identificationQualifer) {
		getTax().setIdentificationQualifer(identificationQualifer);
	}

	
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", this.getId())
			.append("basisOfRecord", this.basisOfRecord)
			.append("scientificName",this.getScientificName())
			.append("localID", this.getLocalId())
			.append("deleted", this.isDeleted())
			.append("institutionCode", this.institutionCode)
			.append("collectionCode",this.collectionCode)
			.append("catalogNumber",this.catalogNumber)
			.append("country",this.getCountry())
			.append("guid", this.guid)
			.toString();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof DarwinCore)) {
			return false;
		}
		DarwinCore dwc = (DarwinCore) object;
        return this.hashCode() == dwc.hashCode();		
	}
	
	/**
	 * Works on the raw imported properties and Ignores all secondary derived properties.
	 * Therefore id, deleted, lat/longAsFloat, modified,created, region & taxon are ignored in the hashing
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
        int result = 17;
        // core record
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (link != null ? link.hashCode() : 0);
        result = 31 * result + (localId != null ? localId.hashCode() : 0);
        // this dwc class
        result = 31 * result + (basisOfRecord != null ? basisOfRecord.hashCode() : 0);
        result = 31 * result + (institutionCode != null ? institutionCode.hashCode() : 0);
        result = 31 * result + (collectionCode != null ? collectionCode.hashCode() : 0);
        result = 31 * result + (catalogNumber != null ? catalogNumber.hashCode() : 0);
        result = 31 * result + (informationWithheld != null ? informationWithheld.hashCode() : 0);
        result = 31 * result + (remarks != null ? remarks.hashCode() : 0);
        result = 31 * result + (sex != null ? sex.hashCode() : 0);
        result = 31 * result + (lifeStage != null ? lifeStage.hashCode() : 0);
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        result = 31 * result + (imageURL != null ? imageURL.hashCode() : 0);
        result = 31 * result + (relatedInformation != null ? relatedInformation.hashCode() : 0);
        // dwc location
        result = 31 * result + (getHigherGeography() != null ? getHigherGeography().hashCode() : 0);
        result = 31 * result + (getContinent() != null ? getContinent().hashCode() : 0);
        result = 31 * result + (getWaterBody() != null ? getWaterBody().hashCode() : 0);
        result = 31 * result + (getIslandGroup() != null ? getIslandGroup().hashCode() : 0);
        result = 31 * result + (getIsland() != null ? getIsland().hashCode() : 0);
        result = 31 * result + (getCountry() != null ? getCountry().hashCode() : 0);
        result = 31 * result + (getStateProvince() != null ? getStateProvince().hashCode() : 0);
        result = 31 * result + (getCounty() != null ? getCounty().hashCode() : 0);
        result = 31 * result + (getLocality() != null ? getLocality().hashCode() : 0);
        result = 31 * result + (getMinimumElevationInMeters() != null ? getMinimumElevationInMeters().hashCode() : 0);
        result = 31 * result + (getMaximumElevationInMeters() != null ? getMaximumElevationInMeters().hashCode() : 0);
        result = 31 * result + (getMinimumDepthInMeters() != null ? getMinimumDepthInMeters().hashCode() : 0);
        result = 31 * result + (getMaximumDepthInMeters() != null ? getMaximumDepthInMeters().hashCode() : 0);
        result = 31 * result + (getCollectingMethod() != null ? getCollectingMethod().hashCode() : 0);
        result = 31 * result + (getValidDistributionFlag() != null ? getValidDistributionFlag().hashCode() : 0);
        result = 31 * result + (getEarliestDateCollected() != null ? getEarliestDateCollected().hashCode() : 0);
        result = 31 * result + (getLatestDateCollected() != null ? getLatestDateCollected().hashCode() : 0);
        result = 31 * result + (getDayOfYear() != null ? getDayOfYear().hashCode() : 0);
        result = 31 * result + (getCollector() != null ? getCollector().hashCode() : 0);
        // dwc taxonomy
        result = 31 * result + (getScientificName() != null ? getScientificName().hashCode() : 0);
        result = 31 * result + (getHigherTaxon() != null ? getHigherTaxon().hashCode() : 0);
        result = 31 * result + (getKingdom() != null ? getKingdom().hashCode() : 0);
        result = 31 * result + (getPhylum() != null ? getPhylum().hashCode() : 0);
        result = 31 * result + (getClasss() != null ? getClasss().hashCode() : 0);
        result = 31 * result + (getOrder() != null ? getOrder().hashCode() : 0);
        result = 31 * result + (getFamily() != null ? getFamily().hashCode() : 0);
        result = 31 * result + (getGenus() != null ? getGenus().hashCode() : 0);
        result = 31 * result + (getSpecificEpithet() != null ? getSpecificEpithet().hashCode() : 0);
        result = 31 * result + (getInfraspecificRank() != null ? getInfraspecificRank().hashCode() : 0);
        result = 31 * result + (getSpecificEpithet() != null ? getSpecificEpithet().hashCode() : 0);
        result = 31 * result + (getInfraspecificRank() != null ? getInfraspecificRank().hashCode() : 0);
        result = 31 * result + (getInfraspecificEpithet() != null ? getInfraspecificEpithet().hashCode() : 0);
        result = 31 * result + (getAuthorYearOfScientificName() != null ? getAuthorYearOfScientificName().hashCode() : 0);
        result = 31 * result + (getNomenclaturalCode() != null ? getNomenclaturalCode().hashCode() : 0);
        result = 31 * result + (getIdentificationQualifer() != null ? getIdentificationQualifer().hashCode() : 0);
        
        return result;
	}
	
	public int compareTo(DarwinCore myClass) {
		return new CompareToBuilder()
				.append(this.institutionCode, myClass.institutionCode)
				.append(this.collectionCode, myClass.collectionCode)
				.append(this.catalogNumber, myClass.catalogNumber)
				.append(this.getScientificName(),	myClass.getScientificName())
				.append(this.localId, myClass.localId)
				.append(this.guid,myClass.guid)
				.toComparison();
	}
}
