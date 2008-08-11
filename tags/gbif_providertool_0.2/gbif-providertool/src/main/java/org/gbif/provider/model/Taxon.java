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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
public class Taxon  implements BaseObject, Comparable {
		protected static final Log log = LogFactory.getLog(Taxon.class);

		private Long id;
		private Taxon parent;
		private String rank;
		private String name;
		private String authorship;
		private String fullname;
		
		
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
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
		public String getFullname() {
			return fullname;
		}
		public void setFullname(String fullname) {
			this.fullname = fullname;
		}
		
		
		/**
		 * @see java.lang.Comparable#compareTo(Object)
		 */
		public int compareTo(Object object) {
			Taxon myClass = (Taxon) object;
			return new CompareToBuilder().append(this.authorship,
					myClass.authorship).append(this.rank, myClass.rank).append(
					this.fullname, myClass.fullname)
					.append(this.name, myClass.name).append(this.parent,
							myClass.parent).append(this.id, myClass.id)
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
			return new ToStringBuilder(this).append("name", this.name).append(
					"rank", this.rank).append("parent", this.parent).append("id",
					this.id).append("authorship", this.authorship).append(
					"fullname", this.fullname).toString();
		}
		
		
}
