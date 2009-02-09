package org.gbif.provider.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.gbif.provider.util.TabFileWriter;

@Entity
@DiscriminatorValue("CORE")
public class ViewCoreMapping extends ViewMappingBase {
	public static final String TEMPLATE_ID_PLACEHOLDER = "<ID>";
	private String guidColumn;
	private String linkColumn;
	private String linkTemplate;
	
	@Override
	@Transient
	public boolean isCore(){
		return true;
	}
	
	@Column(length=128, name="guid_col")
	public String getGuidColumn() {
		return guidColumn;
	}
	public void setGuidColumn(String guidColumn) {
		this.guidColumn = guidColumn;
	}
	
	@Column(length=128, name="link_col")
	public String getLinkColumn() {
		return linkColumn;
	}
	public void setLinkColumn(String linkColumn) {
		this.linkColumn = linkColumn;
	}

	public String getLinkTemplate() {
		return linkTemplate;
	}
	public void setLinkTemplate(String linkTemplate) {
		this.linkTemplate = linkTemplate;
	}

}
