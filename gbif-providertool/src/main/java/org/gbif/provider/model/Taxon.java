package org.gbif.provider.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.gbif.provider.model.voc.Rank;


@Entity
public class Taxon  implements BaseObject, Comparable<Taxon>, TreeNode<Taxon> {
		protected static final Log log = LogFactory.getLog(Taxon.class);

		private Long id;
		private DatasourceBasedResource resource;
		private Taxon parent;
		private String rank;
		private Rank dwcRank;
		private String fullname;
		private String name;
		private String authorship;
		private String code;
		private Long lft;
		private Long rgt;
		
		
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		
		@ManyToOne(optional = false)
		public DatasourceBasedResource getResource() {
			return resource;
		}
		public void setResource(DatasourceBasedResource resource) {
			this.resource = resource;
		}

		@ManyToOne(optional = true)
		public Taxon getParent() {
			return parent;
		}
		public void setParent(Taxon parent) {
			this.parent = parent;
		}
		
		public String getRank() {
			return rank;
		}
		public void setRank(String rank) {
			this.rank = rank;
		}
		
		public Rank getDwcRank() {
			return dwcRank;
		}
		@Transient
		public Enum getType() {
			return dwcRank;
		}
		public void setType(Enum t) {
			if (t instanceof Rank){
				setDwcRank((Rank)t);
			}else{
				throw new IllegalArgumentException();				
			}
		}
		public void setDwcRank(Rank dwcRank) {
			this.dwcRank = dwcRank;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getAuthorship() {
			return authorship;
		}
		public void setAuthorship(String authorship) {
			this.authorship = authorship;
		}
		@Transient
		public String getLabel() {
			return getFullname();
		}
		public void setLabel(String label) {
			setFullname(label);			
		}
		public String getFullname() {
			return fullname;
		}
		public void setFullname(String fullname) {
			this.fullname = fullname;
		}
		
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public Long getLft() {
			return lft;
		}
		public void setLft(Long lft) {
			this.lft = lft;
		}
		public Long getRgt() {
			return rgt;
		}
		public void setRgt(Long rgt) {
			this.rgt = rgt;
		}
		/**
		 * @see java.lang.Comparable#compareTo(Object)
		 */
		public int compareTo(Taxon taxon) {
			return new CompareToBuilder().append(this.authorship,
					taxon.authorship).append(this.rank, taxon.rank).append(
					this.fullname, taxon.fullname)
					.append(this.name, taxon.name).append(this.parent,
							taxon.parent).append(this.id, taxon.id)
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
			return new EqualsBuilder().append(this.authorship, rhs.authorship)
					.append(this.rank, rhs.rank)
					.append(this.fullname, rhs.fullname)
					.append(this.name, rhs.name).append(this.parent, rhs.parent)
					.append(this.id, rhs.id).isEquals();
		}
		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return new HashCodeBuilder(2136008009, 497664597).append(
					this.authorship).append(this.rank).append(this.fullname)
					.append(this.name).append(this.parent).append(this.id)
					.toHashCode();
		}
		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			String parentName = "";
			if (this.parent != null){
				parentName = String.format(" p=%s %s", this.parent.getId(), this.parent.getFullname());
			}
			return String.format("%s [%s,%s%s]", this.getFullname(), this.getId(), this.rank, parentName);
		}
		
}
