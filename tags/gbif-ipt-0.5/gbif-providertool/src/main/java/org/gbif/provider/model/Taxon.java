package org.gbif.provider.model;

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


@Entity
public class Taxon extends TreeNodeBase<Taxon, Rank> implements ResourceRelatedObject {
		protected static final Log log = LogFactory.getLog(Taxon.class);

		private Resource resource;
		private String rank;
		private String name;
		private String authorship;
		private String code;
		// stats
		private BBox bbox = new BBox();
		private int occTotal;
		

		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		@Override
		public Long getId() {
			return super.getId();
		}
		
		@ManyToOne(optional = false)
		public Resource getResource() {
			return resource;
		}
		public void setResource(Resource resource) {
			this.resource = resource;
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
		
}
