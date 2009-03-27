package org.gbif.provider.model;

	import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Index;


	@Entity
	@org.hibernate.annotations.Table(
			appliesTo="Region",
		indexes={
				@Index(name="reg_label", columnNames={"label"} ),
				@Index(name="reg_lft", columnNames={"lft"} ),
				@Index(name="reg_rgt", columnNames={"rgt"} )
		}
)
public class Region extends TreeNodeBase<Region, RegionType> implements ResourceRelatedObject{
	protected static final Log log = LogFactory.getLog(Region.class);

	public static Region newInstance(DataResource resource){
		Region region = new Region();
		region.resource=resource;
		return region;
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
