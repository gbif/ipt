package org.gbif.provider.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.voc.TransformationType;
import org.apache.commons.lang.builder.CompareToBuilder;

@Entity
public class Transformation implements Comparable, ResourceRelatedObject{
	private Long id;
	private DataResource resource;
	private String title;
	private TransformationType type;
	private SourceBase source;
	private String column;
	
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

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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
	
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		Transformation myClass = (Transformation) object;
		return new CompareToBuilder()
			.append(this.resource,myClass.resource)
			.append(this.type, myClass.type)
			.append(this.source, myClass.source)
			.append(this.title, myClass.title)
			.append(this.column, myClass.column)
				.toComparison();
	}
	
}
