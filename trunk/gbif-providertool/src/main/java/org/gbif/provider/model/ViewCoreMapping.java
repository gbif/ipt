package org.gbif.provider.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@DiscriminatorValue("CORE")
public class ViewCoreMapping extends ViewMappingBase {
	private ColumnMapping guidColumn = new ColumnMapping ();
	private ColumnMapping linkColumn = new ColumnMapping ();
	
	
	@Embedded
	@AttributeOverrides( {
        @AttributeOverride(name="columnName", column = @Column(name="guid_col", length=64) ),
	} )
	public ColumnMapping getGuidColumn() {
		return guidColumn;
	}
	public void setGuidColumn(ColumnMapping guidColumn) {
		this.guidColumn = guidColumn;
	}
	
	@Embedded
	@AttributeOverrides( {
        @AttributeOverride(name="columnName", column = @Column(name="link_col", length=64) ),
	} )
	public ColumnMapping getLinkColumn() {
		return linkColumn;
	}
	public void setLinkColumn(ColumnMapping linkColumn) {
		this.linkColumn = linkColumn;
	}
	

	
	
}
