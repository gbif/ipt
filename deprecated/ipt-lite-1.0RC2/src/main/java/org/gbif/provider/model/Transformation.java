package org.gbif.provider.model;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.gbif.provider.model.voc.TransformationType;

@Entity
public class Transformation implements Comparable, ResourceRelatedObject{
	private Long id;
	private DataResource resource;
	private TransformationType type;
	private SourceBase source;
	private String column;
	private ThesaurusVocabulary voc;
	private List<PropertyMapping> propertyMappings;
	private Set<TermMapping> termMappings;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(optional=false)
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

	public TransformationType getType() {
		return type;
	}
	public void setType(TransformationType type) {
		this.type = type;
	}
	
	@ManyToOne(optional=false)
	public SourceBase getSource() {
		return source;
	}
	public void setSource(SourceBase source) {
		this.source = source;
	}
	
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	@Transient
	public String[] getColumns() {
		return StringUtils.split(column, '|');
	}
	public void setColumns(String[] columns) {
		this.column = StringUtils.join(columns,'|');
	}
	
	@OneToMany(mappedBy="termTransformation")
	public List<PropertyMapping> getPropertyMappings() {
		return propertyMappings;
	}
	public void setPropertyMappings(List<PropertyMapping> propertyMappings) {
		this.propertyMappings = propertyMappings;
	}
	
	@ManyToOne
	public ThesaurusVocabulary getVoc() {
		return voc;
	}
	public void setVoc(ThesaurusVocabulary voc) {
		this.voc = voc;
	}
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="transformation")
	public Set<TermMapping> getTermMappings() {
		return termMappings;
	}
	public void setTermMappings(Set<TermMapping> termMappings) {
		this.termMappings = termMappings;
	}
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		Transformation myClass = (Transformation) object;
		return new CompareToBuilder()
		.append(this.resource,myClass.resource)
		.append(this.type, myClass.type)
		.append(this.voc, myClass.voc)
		.append(this.column, myClass.column)
		.append(this.id, myClass.id)
				.toComparison();
	}
	
}
