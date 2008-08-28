package org.gbif.provider.model;

	import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;


	@Entity
	public class Region implements BaseObject, TreeNode<Region>, Comparable<Region> {
			protected static final Log log = LogFactory.getLog(Region.class);

			private Long id;
			private DatasourceBasedResource resource;
			private Region parent;
			private RegionType rank;
			private String label;
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
			public Region getParent() {
				return parent;
			}
			public void setParent(Region parent) {
				this.parent = parent;
			}
						
			@Transient
			public Boolean isLeafNode() {
				return rgt == lft + 1;
			}
			
			@Transient
			public Enum getType() {
				return rank;
			}
			public void setType(Enum t) {
				if (t instanceof RegionType){
					setRank((RegionType)t);
				}else{
					throw new IllegalArgumentException();				
				}
			}
			public RegionType getRank() {
				return rank;
			}
			public void setRank(RegionType rank) {
				this.rank = rank;
			}
			
			public String getLabel() {
				return label;
			}
			public void setLabel(String label) {
				this.label = label;
			}
			
			@org.hibernate.annotations.Index(name="reg_lft")
			public Long getLft() {
				return lft;
			}
			public void setLft(Long lft) {
				this.lft = lft;
			}
			@org.hibernate.annotations.Index(name="reg_rgt")
			public Long getRgt() {
				return rgt;
			}
			public void setRgt(Long rgt) {
				this.rgt = rgt;
			}
			/**
			 * @see java.lang.Comparable#compareTo(Object)
			 */
			public int compareTo(Region region) {
				return new CompareToBuilder()
						.append(this.resource, region.resource)
						.append(this.parent, region.parent)
						.append(this.rank, region.rank)
						.append(this.label, region.label)
						.toComparison();
			}
			/**
			 * @see java.lang.Object#equals(Object)
			 */
			public boolean equals(Object object) {
				if (object == this) {
					return true;
				}
				if (!(object instanceof Region)) {
					return false;
				}
				Region region = (Region) object;
		        return this.hashCode() == region.hashCode();		
			}
			/**
			 * @see java.lang.Object#hashCode()
			 */
			public int hashCode() {
		        int result = 17;
		        result = 31 * result + (resource != null ? resource.hashCode() : 0);
		        result = 31 * result + (parent != null ? parent.hashCode() : 0);
		        result = 31 * result + (rank != null ? rank.hashCode() : 0);
		        result = 31 * result + (label != null ? label.hashCode() : 0);
		        return result;
			}
			
			/**
			 * @see java.lang.Object#toString()
			 */
			public String toString() {
				String parentName = "";
				if (this.parent != null){
					parentName = String.format(" (%s,%s)", this.parent.getId(), this.parent.getLabel());
				}
				return String.format("%s [%s,%s%s]", this.label, this.getId(), this.rank, parentName);
			}

	}
