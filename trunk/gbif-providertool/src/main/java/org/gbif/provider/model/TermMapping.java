package org.gbif.provider.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
public class TermMapping implements BaseObject, Comparable {
	private Long id;
	private SourceBase source;
	private SourceColumn column = new SourceColumn();
	private String term;
	private String targetTerm;
	
	public TermMapping(){
	}
	public TermMapping(SourceBase source, SourceColumn column, String term){
		this.source=source;
		this.column=column;
		this.term=term;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(optional=false)
	public SourceBase getSource() {
		return source;
	}
	public void setSource(SourceBase source) {
		this.source = source;
	}
	
	public SourceColumn getColumn() {
		return column;
	}
	public void setColumn(SourceColumn column) {
		this.column = column;
	}
	
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	
	@Column(length=128)
	public String getTargetTerm() {
		return targetTerm;
	}
	public void setTargetTerm(String targetTerm) {
		this.targetTerm = targetTerm;
	}
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		TermMapping myClass = (TermMapping) object;
		return new CompareToBuilder()
				.append(this.source, myClass.source)
				.append(this.column,myClass.column)
				.append(this.term, myClass.term)
				.toComparison();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof TermMapping)) {
			return false;
		}
		TermMapping rhs = (TermMapping) object;
		return new EqualsBuilder().append(this.term, rhs.term).append(
				this.targetTerm, rhs.targetTerm).append(this.column, rhs.column)
				.append(this.source, rhs.source).append(this.id, rhs.id)
				.isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(1938480675, -864001347).append(this.term)
				.append(this.targetTerm).append(this.column).append(this.source)
				.append(this.id).toHashCode();
	}


	public String toString() {
		return String.format("%s --> %s", term, targetTerm);
	}

	
	
}
