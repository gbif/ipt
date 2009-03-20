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
		private String rank;
		private String name;
		private String nomenclaturalCode;
		// not existing in DwC, but used for TaxonCore
		private String taxonomicParentID;
		private String acceptedTaxonID;
		private String basionymID;
		private String nomenclaturalReference;
		private String taxStatus;
		private String nomStatus;
		private String notes;
		// derived
		private Taxon acceptedTaxon;
		private Taxon basionym;
		
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

		@Column(length=128)
		public String getRank() {
			return rank;
		}
		public void setRank(String rank) {
			this.rank = rank;
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

		@Column(length=32)
		public String getTaxonomicParentID() {
			return taxonomicParentID;
		}
		public void setTaxonomicParentID(String taxonomicParentID) {
			this.taxonomicParentID = taxonomicParentID;
		}

		@Column(length=32)
		public String getAcceptedTaxonID() {
			return acceptedTaxonID;
		}
		public void setAcceptedTaxonID(String acceptedTaxonID) {
			this.acceptedTaxonID = acceptedTaxonID;
		}

		@Column(length=32)
		public String getBasionymID() {
			return basionymID;
		}
		public void setBasionymID(String basionymID) {
			this.basionymID = basionymID;
		}

		public String getNomenclaturalReference() {
			return nomenclaturalReference;
		}
		public void setNomenclaturalReference(String nomenclaturalReference) {
			this.nomenclaturalReference = nomenclaturalReference;
		}

		@Column(length=128)
		public String getTaxonomicStatus() {
			return taxStatus;
		}
		public void setTaxonomicStatus(String taxStatus) {
			this.taxStatus = taxStatus;
		}

		@Column(length=128)
		public String getNomenclaturalStatus() {
			return nomStatus;
		}
		public void setNomenclaturalStatus(String nomStatus) {
			this.nomStatus = nomStatus;
		}

		@Lob
		public String getNotes() {
			return notes;
		}
		public void setNotes(String notes) {
			this.notes = notes;
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
					.append(this.rank, rhs.rank)
					.append(this.name, rhs.name)
					.append(this.getParent(), rhs.getParent())
					.append(this.getId(), rhs.getId()).isEquals();
		}
		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return new HashCodeBuilder(2136008009, 497664597).append(this.rank).append(this.getLabel())
					.append(this.name).append(this.getParent()).append(this.getId())
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
