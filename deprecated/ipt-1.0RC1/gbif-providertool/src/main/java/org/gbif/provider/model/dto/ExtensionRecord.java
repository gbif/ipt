package org.gbif.provider.model.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Record;
import org.gbif.provider.util.TabFileWriter;
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
public class ExtensionRecord implements Iterable<ExtensionProperty>, Record{
	private Long coreId;
	private Long resourceId;
	private String sourceId;
	private Extension extension;
	private Map<ExtensionProperty, String> properties = new HashMap<ExtensionProperty, String>();

	public static ExtensionRecord newInstance(ImportRecord iRec){
		ExtensionRecord extRec = new ExtensionRecord(iRec.getResourceId(), iRec.getSourceId(), iRec.getProperties());
		return extRec;
	}
	
	public ExtensionRecord(Long resourceId, String sourceId) {
		super();
		this.resourceId = resourceId;
		this.sourceId = sourceId;
	}
	public ExtensionRecord(Long resourceId, Long coreId) {
		super();
		this.resourceId = resourceId;
		this.coreId = coreId;
	}
	public ExtensionRecord(Long resourceId, String sourceId, Map<ExtensionProperty, String> properties) {
		super();
		this.resourceId = resourceId;
		this.sourceId = sourceId;
		this.properties = properties;
		}

	public Long getResourceId() {
		return resourceId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public Long getCoreId() {
		return coreId;
	}

	public void setCoreId(Long coreId) {
		this.coreId = coreId;
	}

	public Extension getExtension(){
		if (extension==null){
			for (ExtensionProperty p : properties.keySet()){
				if (p!=null){
					extension=p.getExtension();
					break;
				}
			}
		}
		return extension;
	}
	public List<ExtensionProperty> getProperties() {
		return new ArrayList<ExtensionProperty>(properties.keySet());
	}
	/* (non-Javadoc)
	 * @see org.gbif.provider.model.Record#getPropertyValue(org.gbif.provider.model.ExtensionProperty)
	 */
	public String getPropertyValue(ExtensionProperty property) {
		return properties.get(property);
	}
	public void setPropertyValue(ExtensionProperty property, String value) {
		properties.put(property, value);
		
	}
	

	public boolean isEmpty() {
		return properties.isEmpty();
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
		return new EqualsBuilder().append(this.sourceId, rhs.sourceId).append(
				this.properties, rhs.properties).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(890875527, 2143130705).append(this.sourceId)
				.append(this.properties).toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("properties", this.properties)
				.append("coreId", this.sourceId).toString();
	}
	
}
