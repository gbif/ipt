package org.gbif.provider.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.gbif.provider.datasource.ImportRecord;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;



/**
 * A record with property values inside the properties map for any extension as opposed to a CoreRecord.
 * The ExtensionRecord is not managed by Hibernate (therefore it is no Entity) 
 * as the number and name of properties as well as the extension it is attached to varies
 * @author markus
 *
 */
public class ExtensionRecord implements Iterable<ExtensionProperty>{
	private Long coreId;
	private Map<ExtensionProperty, String> properties = new HashMap<ExtensionProperty, String>();

	public static ExtensionRecord newInstance(ImportRecord iRec){
		ExtensionRecord extRec = new ExtensionRecord();
		extRec.setCoreId(iRec.getId());
		extRec.setProperties(iRec.getProperties());
		return extRec;
	}
	
	public Long getCoreId() {
		return coreId;
	}
	public void setCoreId(Long coreId) {
		this.coreId = coreId;
	}
	
	public Map<ExtensionProperty, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<ExtensionProperty, String> properties) {
		this.properties = properties;
	}
	public String getPropertyValue(ExtensionProperty property) {
		return properties.get(property);
	}
	public void setPropertyValue(ExtensionProperty property, String value) {
		properties.put(property, value);
		
	}
	
	

	public Iterator<ExtensionProperty> iterator() {
		return new PropertyIterator();
		
	}

	private class PropertyIterator implements Iterator<ExtensionProperty> {
        private int index = 0;
        private ExtensionProperty[] props;
      
        protected PropertyIterator(){
        	index = 0;
        	this.props = (ExtensionProperty[]) properties.keySet().toArray(new ExtensionProperty[properties.keySet().size()]);
        }
        public boolean hasNext()   {
            return index < props.length;  
        }
        public ExtensionProperty next()  {
        	if(hasNext())
                return props[index++];
            else
                throw new NoSuchElementException();
        }
        public void remove()  {
    	    throw new UnsupportedOperationException();		
        }
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
