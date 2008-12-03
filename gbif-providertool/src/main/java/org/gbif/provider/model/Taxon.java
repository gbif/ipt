package org.gbif.provider.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.gbif.provider.model.voc.Rank;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKey;
import org.hibernate.validator.NotNull;


@Entity
public class Taxon extends TreeNodeBase<Taxon, Rank> implements CoreRecord {
		protected static final Log log = LogFactory.getLog(Taxon.class);

		// for core record
		@NotNull
		private String localId;
		@NotNull
		private String guid;
		private String link;
		private boolean isDeleted;
		private Date modified;
		@NotNull
		private Resource resource;
		
		// taxon specific
		private String rank;
		private String name;
		private String authorship;
		private String code;
		// not existing in DwC, but used for TaxonCore
		private String taxonomicParentID;
		private String acceptedTaxonID;
		private String basionymID;
		private String nomenclaturalReference;
		private String taxStatus;
		private String nomStatus;
		private String notes;
		// derived
		private Taxon accepted;
		private Taxon basionym;
		
		// stats
		private BBox bbox = new BBox();
		private int occTotal;
		
		
		
		public static Taxon newInstance(Resource resource){
			Taxon tax = new Taxon();
			tax.resource=resource;
			return tax;
		}
		
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		@Override
		public Long getId() {
			return super.getId();
		}
		@Transient
		public Long getCoreId() {
			return super.getId();
		}
		
		@ManyToOne(optional = false)
		public Resource getResource() {
			return resource;
		}
		public void setResource(Resource resource) {
			this.resource = resource;
		}

		
		@Column(length=128)
		@org.hibernate.annotations.Index(name="tax_source_local_id")
		public String getLocalId() {
			return localId;
		}
		public void setLocalId(String localId) {
			this.localId = localId;
		}

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

		@ManyToOne(optional = true)
		@Override
		public Taxon getParent() {
			return super.getParent();
		}
				
		@Column(length=128)
		public String getRank() {
			return rank;
		}
		public void setRank(String rank) {
			this.rank = rank;
		}
		
		public Rank getDwcRank() {
			return super.getType();
		}
		public void setDwcRank(Rank dwcRank) {
			super.setType(dwcRank);
		}
		
		@Column(length=128)
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		@Column(length=128)
		public String getAuthorship() {
			return authorship;
		}
		public void setAuthorship(String authorship) {
			this.authorship = authorship;
		}
		
		@org.hibernate.annotations.Index(name="taxon_fullname")
		public String getFullname() {
			return getLabel();
		}
		public void setFullname(String fullname) {
			setLabel(fullname);
		}
		
		@Column(length=64)
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		
		@org.hibernate.annotations.Index(name="tax_lft")
		@Override
		public Long getLft() {
			return super.getLft();
		}
		
		@org.hibernate.annotations.Index(name="tax_rgt")
		@Override
		public Long getRgt() {
			return super.getRgt();
		}

		public BBox getBbox() {
			if (bbox==null){
				bbox = new BBox();
			}
			return bbox;
		}
		public void setBbox(BBox bbox) {
			this.bbox = bbox;
		}
		public void expandBox(Point p) {
			getBbox().expandBox(p);
		}

		public int getOccTotal() {
			return occTotal;
		}
		public void setOccTotal(int occTotal) {
			this.occTotal = occTotal;
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
		public String getTaxStatus() {
			return taxStatus;
		}
		public void setTaxStatus(String taxStatus) {
			this.taxStatus = taxStatus;
		}

		@Column(length=128)
		public String getNomStatus() {
			return nomStatus;
		}
		public void setNomStatus(String nomStatus) {
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
		public Taxon getAccepted() {
			return accepted;
		}
		public void setAccepted(Taxon accepted) {
			this.accepted = accepted;
		}

		@ManyToOne(optional = true)
		public Taxon getBasionym() {
			return basionym;
		}
		public void setBasionym(Taxon basionym) {
			this.basionym = basionym;
		}

		
		/**
		 * Count a single occurrence record
		 * @param region
		 */
		public void countOcc(DarwinCore dwc) {
			this.occTotal++;
			bbox.expandBox(dwc.getLocation());			
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
					.append(this.authorship, rhs.authorship)
					.append(this.rank, rhs.rank)
					.append(this.name, rhs.name)
					.append(this.getParent(), rhs.getParent())
					.append(this.getId(), rhs.getId()).isEquals();
		}
		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return new HashCodeBuilder(2136008009, 497664597).append(
					this.authorship).append(this.rank).append(this.getLabel())
					.append(this.name).append(this.getParent()).append(this.getId())
					.toHashCode();
		}

		
		@Transient
		public String getPropertyValue(ExtensionProperty property){
			String propName = property.getName();
			if (propName.equals("Class")){
				propName = "Classs";
			}
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
		
		
}
