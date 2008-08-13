package org.gbif.provider.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Embeddable
public class ColumnMapping {
	private String columnName;

	@Column(length=128)
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String colName) {
		if (colName != null && colName.trim().equals("")){
			colName=null;
		}
		this.columnName = colName;
	}
	public void setColumnName(String table, String columnName) {
		this.columnName = String.format("[%s].[%s]", table.trim(), columnName.trim());
	}
	
	
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof ColumnMapping)) {
			return false;
		}
		ColumnMapping rhs = (ColumnMapping) object;
		return new EqualsBuilder().append(this.columnName, rhs.columnName).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */

	public int hashCode() {
        int result = 17;
        result = 31 * (columnName != null ? columnName.hashCode() : 0);
        return result;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("column", this.columnName)
				.toString();
	}

	
	
}
