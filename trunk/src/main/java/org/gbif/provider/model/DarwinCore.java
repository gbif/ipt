/**
 * 
 */
package org.gbif.provider.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The core class for taxon occurrence records with normalised properties used by the webapp.
 * The generated property values can be derived from different extensions like DarwinCore or ABCD
 * but the ones here are used for creating most of the webapp functionality
 * @author markus
 *
 */
@Entity
public class DarwinCore extends ResolvableBase {
	private OccurrenceResource occurrenceResource;
	// DarwinCore 1.4 elements
	private String globalUniqueIdentifier;
	private Date dateLastModified;
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
	

	@ManyToOne
	public OccurrenceResource getOccurrenceResource() {
		return occurrenceResource;
	}
	public void setOccurrenceResource(OccurrenceResource occurrenceResource) {
		this.occurrenceResource = occurrenceResource;
	}
	
	
	public String getGlobalUniqueIdentifier() {
		return globalUniqueIdentifier;
	}
	public void setGlobalUniqueIdentifier(String globalUniqueIdentifier) {
		this.globalUniqueIdentifier = globalUniqueIdentifier;
	}
	public Date getDateLastModified() {
		return dateLastModified;
	}
	public void setDateLastModified(Date dateLastModified) {
		this.dateLastModified = dateLastModified;
	}
	public String getBasisOfRecord() {
		return basisOfRecord;
	}
	public void setBasisOfRecord(String basisOfRecord) {
		this.basisOfRecord = basisOfRecord;
	}
	public String getInstitutionCode() {
		return institutionCode;
	}
	public void setInstitutionCode(String institutionCode) {
		this.institutionCode = institutionCode;
	}
	public String getCollectionCode() {
		return collectionCode;
	}
	public void setCollectionCode(String collectionCode) {
		this.collectionCode = collectionCode;
	}
	public String getCatalogNumber() {
		return catalogNumber;
	}
	public void setCatalogNumber(String catalogNumber) {
		this.catalogNumber = catalogNumber;
	}
	public String getInformationWithheld() {
		return informationWithheld;
	}
	public void setInformationWithheld(String informationWithheld) {
		this.informationWithheld = informationWithheld;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

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
	public String getRelatedInformation() {
		return relatedInformation;
	}
	public void setRelatedInformation(String relatedInformation) {
		this.relatedInformation = relatedInformation;
	}
	
	
	
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		DarwinCore myClass = (DarwinCore) object;
		return new CompareToBuilder().append(this.basisOfRecord,
				myClass.basisOfRecord).append(this.imageURL, myClass.imageURL)
				.append(this.institutionCode, myClass.institutionCode).append(
						this.dateLastModified, myClass.dateLastModified)
				.append(this.remarks, myClass.remarks).append(
						this.relatedInformation, myClass.relatedInformation)
				.append(this.informationWithheld, myClass.informationWithheld)
				.append(this.occurrenceResource, myClass.occurrenceResource)
				.append(this.lifeStage, myClass.lifeStage).append(this.sex,
						myClass.sex)
				.append(this.attributes, myClass.attributes).append(
						this.globalUniqueIdentifier,
						myClass.globalUniqueIdentifier).append(
						this.collectionCode, myClass.collectionCode).append(
						this.catalogNumber, myClass.catalogNumber)
				.toComparison();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof DarwinCore)) {
			return false;
		}
		DarwinCore rhs = (DarwinCore) object;
		return new EqualsBuilder()
				.append(this.basisOfRecord, rhs.basisOfRecord).append(
						this.imageURL, rhs.imageURL).append(
						this.institutionCode, rhs.institutionCode).append(
						this.dateLastModified, rhs.dateLastModified).append(
						this.remarks, rhs.remarks).append(
						this.relatedInformation, rhs.relatedInformation)
				.append(this.informationWithheld, rhs.informationWithheld)
				.append(this.occurrenceResource, rhs.occurrenceResource)
				.append(this.lifeStage, rhs.lifeStage)
				.append(this.sex, rhs.sex).append(this.attributes,
						rhs.attributes).append(this.globalUniqueIdentifier,
						rhs.globalUniqueIdentifier).append(this.collectionCode,
						rhs.collectionCode).append(this.catalogNumber,
						rhs.catalogNumber).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(139652297, -21774617).append(
				this.basisOfRecord).append(this.imageURL).append(
				this.institutionCode).append(this.dateLastModified).append(
				this.remarks).append(this.relatedInformation).append(
				this.informationWithheld).append(this.occurrenceResource)
				.append(this.lifeStage).append(this.sex)
				.append(this.attributes).append(this.globalUniqueIdentifier)
				.append(this.collectionCode).append(this.catalogNumber)
				.toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("relatedInformation",
				this.relatedInformation).append("modified", this.getModified())
				.append("id", this.getId()).append("dateLastModified",
						this.dateLastModified)
				.append("imageURL", this.imageURL).append("occurrenceResource",
						this.occurrenceResource).append("basisOfRecord",
						this.basisOfRecord).append("remarks", this.remarks)
				.append("informationWithheld", this.informationWithheld)
				.append("attributes", this.attributes).append("uri",
						this.getUri()).append("globalUniqueIdentifier",
						this.globalUniqueIdentifier).append("institutionCode",
						this.institutionCode).append("lifeStage",
						this.lifeStage).append("collectionCode",
						this.collectionCode).append("catalogNumber",
						this.catalogNumber).append("uuid", this.getUuid())
				.append("sex", this.sex).toString();
	}
	
	
}
