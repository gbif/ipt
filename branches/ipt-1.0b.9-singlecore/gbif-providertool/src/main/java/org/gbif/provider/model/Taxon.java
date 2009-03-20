package org.gbif.provider.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.voc.Rank;
import org.hibernate.annotations.Index;
import org.hibernate.validator.NotNull;


@Entity
@Table(
	uniqueConstraints = {@UniqueConstraint(columnNames={"localId", "resource_fk"})}
)
@org.hibernate.annotations.Table(
	appliesTo="Taxon",
	indexes={
		@Index(name="tax_label", columnNames={"label"} ),
		@Index(name="tax_lft", columnNames={"lft"} ),
		@Index(name="tax_rgt", columnNames={"rgt"} )
	}
)
public class Taxon extends TreeNodeBase<Taxon, Rank> implements CoreRecord {
		protected static final Log log = LogFactory.getLog(Taxon.class);

		// for core record
		private String localId;
		@NotNull
		private String guid;
		private String link;
		private boolean isDeleted;
		private Date modified;
		// taxon specific
		//private String taxonID; --> this.guid
		// private String scientificName; --> this.label
		private String binomial;
//		private String kingdom;
//		private String phylum;
//		private String classs;
//		private String order;
//		private String family;
//		private String genus;
//		private String subgenus;
		private String specificEpithet;
		private String taxonRank;
		private String infraspecificEpithet;
		private String scientificNameAuthorship;
		private String nomenclaturalCode;
		private String taxonAccordingTo;
		private String namePublishedIn;
		private String taxonomicStatus;
		private String nomenclaturalStatus;
//		private Taxon higherTaxon; --> this.parent
//		private String higherTaxonID;  
//		private String higherTaxon;
		private Taxon acceptedTaxon;
//		private String acceptedTaxonID;
//		private String acceptedTaxon;
		private Taxon basionym;
//		private String basionymID;
//		private String basionym;	
		
		public static Taxon newInstance(DataResource resource){
			Taxon tax = new Taxon();
			tax.resource=resource;
			tax.setGuid(UUID.randomUUID().toString());					
			return tax;
		}
		
		@Transient
		public Long getCoreId() {
			return super.getId();
		}
		
		@Transient
		public String getTaxonID() {
			return guid;
		}
		public void setTaxonID(String taxonID) {
			this.guid = taxonID;
		}
		
		public void setResource(DataResource resource) {
			this.resource = resource;
		}
		@Column(length=64)
		@org.hibernate.annotations.Index(name="tax_local_id")
		public String getLocalId() {
			return localId;
		}
		public void setLocalId(String localId) {
			this.localId = localId;
		}

		@Column(length=128, unique=true)
		@org.hibernate.annotations.Index(name="tax_guid")
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

		@org.hibernate.annotations.Index(name="tax_deleted")
		public boolean isDeleted() {
			return isDeleted;
		}
		public void setDeleted(boolean isDeleted) {
			this.isDeleted = isDeleted;
		}

		public Date getModified() {
			return modified;
		}
		public void setModified(Date modified) {
			this.modified = modified;
		}

		@Transient
		@Deprecated
		public String getRank() {
			return taxonRank;
		}
		@Deprecated
		public void setRank(String rank) {
			this.taxonRank = rank;
		}
		
		@Column(length=128)
		public String getTaxonRank() {
			return taxonRank;
		}
		public void setTaxonRank(String taxonRank) {
			this.taxonRank = taxonRank;
		}

		@Transient
		public Rank getDwcRank() {
			return super.getType();
		}
		public void setDwcRank(Rank dwcRank) {
			super.setType(dwcRank);
		}
		
		@Transient
		public String getScientificName() {
			return getLabel();
		}
		public void setScientificName(String fullname) {
			setLabel(fullname);
		}
		
		@Column(length=64)
		public String getNomenclaturalCode() {
			return nomenclaturalCode;
		}

		public void setNomenclaturalCode(String nomenclaturalCode) {
			this.nomenclaturalCode = nomenclaturalCode;
		}

		@Transient
		public String getTaxonomicParentID() {
			if (parent!=null){
				return parent.guid;				
			}
			return null;
		}

		@Transient
		public String getAcceptedTaxonID() {
			if (acceptedTaxon!=null){
				return acceptedTaxon.guid;				
			}
			return null;
		}

		@Transient
		public String getBasionymID() {
			if (basionym!=null){
				return basionym.guid;				
			}
			return null;
		}

		@Transient
		@Deprecated
		public String getNomenclaturalReference() {
			return namePublishedIn;
		}
		@Deprecated
		public void setNomenclaturalReference(String nomenclaturalReference) {
			this.namePublishedIn = nomenclaturalReference;
		}

		public String getNamePublishedIn() {
			return namePublishedIn;
		}
		public void setNamePublishedIn(String namePublishedIn) {
			this.namePublishedIn = namePublishedIn;
		}

		@Column(length=128)
		public String getTaxonomicStatus() {
			return taxonomicStatus;
		}
		public void setTaxonomicStatus(String taxStatus) {
			this.taxonomicStatus = taxStatus;
		}

		@Column(length=128)
		public String getNomenclaturalStatus() {
			return nomenclaturalStatus;
		}
		public void setNomenclaturalStatus(String nomStatus) {
			this.nomenclaturalStatus = nomStatus;
		}

		@ManyToOne(optional = true)
		public Taxon getAcceptedTaxon() {
			return acceptedTaxon;
		}
		public void setAcceptedTaxon(Taxon acceptedTaxon) {
			this.acceptedTaxon = acceptedTaxon;
		}

		@ManyToOne(optional = true)
		public Taxon getBasionym() {
			return basionym;
		}
		public void setBasionym(Taxon basionym) {
			this.basionym = basionym;
		}

		public String getBinomial() {
			return binomial;
		}
		public void setBinomial(String binomial) {
			this.binomial = binomial;
		}

		@Column(length=128)
		public String getSpecificEpithet() {
			return specificEpithet;
		}
		public void setSpecificEpithet(String specificEpithet) {
			this.specificEpithet = specificEpithet;
		}

		@Column(length=128)
		public String getInfraspecificEpithet() {
			return infraspecificEpithet;
		}
		public void setInfraspecificEpithet(String infraspecificEpithet) {
			this.infraspecificEpithet = infraspecificEpithet;
		}

		public String getScientificNameAuthorship() {
			return scientificNameAuthorship;
		}
		public void setScientificNameAuthorship(String scientificNameAuthorship) {
			this.scientificNameAuthorship = scientificNameAuthorship;
		}

		public String getTaxonAccordingTo() {
			return taxonAccordingTo;
		}

		public void setTaxonAccordingTo(String taxonAccordingTo) {
			this.taxonAccordingTo = taxonAccordingTo;
		}

		@Override
		protected int compareWithoutHierarchy(Taxon first, Taxon second) {
			return new CompareToBuilder()
				.append(first.resource, second.resource)
				.append(first.getLabel(), second.getLabel())
				.toComparison(); 						
		}

		
		/**
		 * @see java.lang.Object#equals(Object)
		 */
		public boolean equals(Object object) {
			if (!(object instanceof Taxon)) {
				return false;
			}
			Taxon rhs = (Taxon) object;
			return new EqualsBuilder()
					.append(this.getLabel(), rhs.getLabel())
					.append(this.taxonRank, rhs.taxonRank)
					.append(this.taxonAccordingTo, rhs.taxonAccordingTo)
					.append(this.getParent(), rhs.getParent())
					.append(this.getId(), rhs.getId()).isEquals();
		}
		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return new HashCodeBuilder(2136008009, 497664597).append(this.taxonRank).append(this.getLabel())
					.append(this.taxonAccordingTo).append(this.getParent()).append(this.getId())
					.toHashCode();
		}

		
		@Transient
		public String getPropertyValue(ExtensionProperty property){
			String propName = property.getName();
			String getter = String.format("get%s", propName);
			String value = null;
			try {
				Method m = this.getClass().getMethod(getter);
				Object obj = m.invoke(this);
				if (obj!=null){
					value=obj.toString();
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return value;
		}
		public boolean setPropertyValue(ExtensionProperty property, String value){
			try {
				Method m = this.getClass().getMethod(String.format("set%s", property.getName()), String.class);
				m.invoke(this, value);
			} catch (SecurityException e) {
				e.printStackTrace();
				return false;
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				return false;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return false;
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}		
		
}
