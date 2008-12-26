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
	private Transformation transformation;
	private String term;
	private String targetTerm;
	
	public TermMapping(){
	}
	public TermMapping(Transformation transformation, String term){
		this.transformation=transformation;
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
	public Transformation getTransformation() {
		return transformation;
	}
	public void setTransformation(Transformation transformation) {
		this.transformation = transformation;
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
		return new CompareToBuilder().append(this.transformation,
				myClass.transformation).append(this.term, myClass.term).append(
				this.targetTerm, myClass.targetTerm)
				.append(this.id, myClass.id).toComparison();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof TermMapping)) {
			return false;
		}
		TermMapping rhs = (TermMapping) object;
		return new EqualsBuilder().append(this.transformation,
				rhs.transformation).append(this.term, rhs.term).append(
				this.targetTerm, rhs.targetTerm).append(this.id, rhs.id)
				.isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(1318785267, 1875601279).append(
				this.transformation).append(this.term).append(this.targetTerm)
				.append(this.id).toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("term",this.term).append("targetTerm", this.targetTerm).toString();
	}

	
	
}
