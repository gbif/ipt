/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.gbif.provider.util.AppConfig;
import org.hibernate.annotations.MapKey;

/**
 * A mapping between a resource and an extension (incl darwincore itself).
 * The ExtensionMapping defines the sql statement used to upload data for a certain extension,
 * therefore for every extension there exists a separate sql statement which should be uploaded one after the other.
 * @author markus
 *
 */
@Entity
public class ExtensionMapping  implements BaseObject, Comparable<ExtensionMapping>, ResourceRelatedObject {
	public static final String TEMPLATE_ID_PLACEHOLDER = "<ID>";
	private Long id;
	private DataResource resource;
	private Extension extension;
	private SourceBase source;
	private String coreIdColumn;
	private Map<Long, PropertyMapping> propertyMappings = new HashMap<Long, PropertyMapping>();
	private int recTotal = 0;
	private String linkColumn;
	private String linkTemplate;
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO) 
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne
    @JoinColumn(name="resource_fk", nullable=false)
	public DataResource getResource() {
		return resource;
	}
	public void setResource(DataResource resource) {
		this.resource = resource;
	}
	@Transient
	public Long getResourceId() {
		return resource.getId();
	}
	
	@ManyToOne
	public Extension getExtension() {
		return extension;
	}
	public void setExtension(Extension extension) {
		this.extension = extension;
	}

	
	public int getRecTotal() {
		return recTotal;
	}
	public void setRecTotal(int recTotal) {
		this.recTotal = recTotal;
	}
	

	/**
	 * Index of resultset column for the local or global identifier for a core-record. 
	 * Acts as the primary key for the core mapping or the foreign key for extension mappings
	 * @return
	 */
	@Column(length=128, name="localid_col")
	public String getCoreIdColumn() {
		return coreIdColumn;
	}
	public void setCoreIdColumn(String coreIdColumn) {
		this.coreIdColumn = coreIdColumn;
	}

	
	@OneToMany(mappedBy="viewMapping", cascade=CascadeType.ALL)
	@MapKey(columns = @Column(name = "property_fk"))
	public Map<Long, PropertyMapping> getPropertyMappings() {
		return propertyMappings;
	}
	public void setPropertyMappings(Map<Long, PropertyMapping> propertyMappings) {
		this.propertyMappings = propertyMappings;
	}
	
	public void addPropertyMapping(PropertyMapping propertyMapping) {
		propertyMapping.setViewMapping(this);
		propertyMappings.put(propertyMapping.getProperty().getId(), propertyMapping);
	}
	public void removePropertyMapping(PropertyMapping propertyMapping) {
		propertyMapping.setViewMapping(null);
		propertyMappings.remove(propertyMapping.getProperty().getId());
	}
		
	@Transient
	public PropertyMapping getPropertyMapping(Long propertyId) {
		return propertyMappings.get(propertyId);
	}
	@Transient
	public List<PropertyMapping> getPropertyMappingsSorted() {
		List<PropertyMapping> pms = new ArrayList<PropertyMapping>(propertyMappings.values());
		Collections.sort(pms);
		return pms;
	}
	
	@ManyToOne
	public SourceBase getSource() {
		return source;
	}
	public void setSource(SourceBase source) {
		this.source = source;
	}
	
	@Transient
	public boolean hasMappedProperty(ExtensionProperty property) {
		if (property!=null && propertyMappings.containsKey(property.getId())){
			return true;
		}
		return false;
	}	
	@Transient
	public boolean hasMappedProperty(String propertyName) {
		if (getPropertyMappingByName(propertyName)!=null){
			return true;
		}
		return false;
	}	

	@Transient
	public PropertyMapping getMappedProperty(ExtensionProperty property) {
		return propertyMappings.get(property.getId());
	}	
	
	@Transient
	public PropertyMapping getPropertyMappingByName(String property) {
		for (PropertyMapping pm : propertyMappings.values()){
			if (pm.getProperty().getName().equals(property)){
				return pm;
			}
		}
		return null;
	}	

	@Transient
	public List<ExtensionProperty> getMappedProperties() {
		List<ExtensionProperty> props = new ArrayList<ExtensionProperty>();
		for (PropertyMapping pm : propertyMappings.values()){
			props.add(pm.getProperty());
		}
		return props;
	}
	@Transient
	public boolean isMappedToFile() {
		if (this.source instanceof SourceFile){
			return true;
		}else{
			return false;
		}
	}
	
	@Transient
	public boolean hasValidSource() {
		if (source==null){
			return false;
		}
		return source.isValid();
	}
	
	/**
	 * Natural sort order is resource, then extension
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(ExtensionMapping view) {
		if (resource != null){
			int resCmp = resource.compareTo(view.resource);
			int extComp = (extension == null ? extComp = -1 : extension.compareTo(view.extension)); 
			return (resCmp != 0 ? resCmp : extComp);			
		}else{
			return -1;
		}
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
        int result = 17;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        result = 31 * result + (resource != null ? (resource.getId() != null ? resource.getId().hashCode() : 0) : 0);
        //result = 31 * result + (resource != null ? resource.hashCode() : 0);
        result = 31 * result + (propertyMappings != null ? propertyMappings.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (coreIdColumn != null ? coreIdColumn.hashCode() : 0);
        return result;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this)
		.append("id", this.id)
		.append("source", this.source)
		.append("coreIdColumn", this.coreIdColumn)
		.append("extension", this.extension)
		.toString();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExtensionMapping)) {
            return false;
        }

        final ExtensionMapping vm = (ExtensionMapping) o;

        return this.hashCode() == vm.hashCode();
	}
	
	@Transient
	public boolean isCore() {
		if (extension != null){
			return extension.isCore();			
		}
		return false;
	}
	
	@Column(length = 128, name = "link_col")
	public String getLinkColumn() {
		return linkColumn;
	}
	public void setLinkColumn(String linkColumn) {
		this.linkColumn = StringUtils.trimToNull(linkColumn);
	}
	
	public String getLinkTemplate() {
		return linkTemplate;
	}
	public void setLinkTemplate(String linkTemplate) {
		this.linkTemplate = StringUtils.trimToNull(linkTemplate);
	}

}
