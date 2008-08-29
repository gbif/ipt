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
	public class Region extends TreeNodeBase<Region, RegionType> {
		protected static final Log log = LogFactory.getLog(Region.class);

		private DatasourceBasedResource resource;
		
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		@Override
		public Long getId() {
			return super.getId();
		}
		
		@ManyToOne(optional = false)
		public DatasourceBasedResource getResource() {
			return resource;
		}
		public void setResource(DatasourceBasedResource resource) {
			this.resource = resource;
		}

		@ManyToOne(optional = true)
		@Override
		public Region getParent() {
			return super.getParent();
		}
		
		@Override
		public String getLabel() {
			return super.getLabel();
		}

		@org.hibernate.annotations.Index(name="reg_lft")
		@Override
		public Long getLft() {
			return super.getLft();
		}
		@org.hibernate.annotations.Index(name="reg_rgt")
		@Override
		public Long getRgt() {
			return super.getRgt();
		}
		
		
		@Override
		protected int compareWithoutHierarchy(Region first, Region second) {
			return new CompareToBuilder()
				.append(first.resource, second.resource)
				.append(first.getLabel(), second.getLabel())
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
	        result = 31 * result + (getParent() != null ? getParent().hashCode() : 0);
	        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
	        result = 31 * result + (getLabel() != null ? getLabel().hashCode() : 0);
	        return result;
		}

	}
