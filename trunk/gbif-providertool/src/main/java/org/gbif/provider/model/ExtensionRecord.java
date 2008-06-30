package org.gbif.provider.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.appfuse.model.BaseObject;
import org.gbif.provider.service.Resolvable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@MappedSuperclass
public class ExtensionRecord extends BaseObject {
	private Long coreId;
	private Map<ExtensionProperty, String> properties = new HashMap<ExtensionProperty, String>();

	public static ExtensionRecord newInstance(CoreRecord coreRec){
		ExtensionRecord extRec = new ExtensionRecord();
		extRec.setCoreId(coreRec.getId());
		extRec.setProperties(coreRec.getProperties());
		return extRec;
	}
	
	public Long getCoreId() {
		return coreId;
	}
	public void setCoreId(Long coreId) {
		this.coreId = coreId;
	}
	
	@Transient
	public Map<ExtensionProperty, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<ExtensionProperty, String> properties) {
		this.properties = properties;
	}
	public void setPropertyValue(ExtensionProperty property, String value) {
		properties.put(property, value);
		
	}
	
	
	
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof ExtensionRecord)) {
			return false;
		}
		ExtensionRecord rhs = (ExtensionRecord) object;
		return new EqualsBuilder().append(this.coreId, rhs.coreId).append(
				this.properties, rhs.properties).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(890875527, 2143130705).append(this.coreId)
				.append(this.properties).toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("properties", this.properties)
				.append("coreId", this.coreId).toString();
	}
	
}
