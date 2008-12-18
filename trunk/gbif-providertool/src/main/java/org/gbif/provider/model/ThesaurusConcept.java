package org.gbif.provider.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.FetchMode;

@Entity
public class ThesaurusConcept implements Comparable, BaseObject{
	private Long id;
	private String identifier;
	private String uri;
	private ThesaurusVocabulary vocabulary;
	private Integer conceptOrder;
	private Date issued;
	
	
	private Set<ThesaurusTerm> terms;
	@OneToMany(mappedBy="concept",fetch=FetchType.LAZY)
	public Set<ThesaurusTerm> getTerms() {
		return terms;
	}
	public void setTerms(Set<ThesaurusTerm> terms) {
		this.terms = terms;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(length=64)
	@org.hibernate.annotations.Index(name="concept_identifier")
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	@org.hibernate.annotations.Index(name="concept_uri")
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	
	@ManyToOne(optional = false, fetch=FetchType.LAZY)
	public ThesaurusVocabulary getVocabulary() {
		return vocabulary;
	}
	public void setVocabulary(ThesaurusVocabulary vocabulary) {
		this.vocabulary = vocabulary;
	}
	
	public Date getIssued() {
		return issued;
	}
	public void setIssued(Date issued) {
		this.issued = issued;
	}
	
	public Integer getConceptOrder() {
		return conceptOrder;
	}
	public void setConceptOrder(Integer conceptOrder) {
		this.conceptOrder = conceptOrder;
	}
	
	
	
	
	
	public String toString(){
		return identifier;
	}
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		ThesaurusConcept myClass = (ThesaurusConcept) object;
		return new CompareToBuilder().append(this.issued, myClass.issued)
				.append(this.uri, myClass.uri)
				.append(this.vocabulary, myClass.vocabulary).append(this.identifier,
						myClass.identifier).append(this.id, myClass.id)
				.toComparison();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof ThesaurusConcept)) {
			return false;
		}
		ThesaurusConcept rhs = (ThesaurusConcept) object;
		return new EqualsBuilder().append(this.issued, rhs.issued).append(
				this.uri, rhs.uri).append(this.vocabulary, rhs.vocabulary).append(
				this.identifier, rhs.identifier).append(this.id, rhs.id)
				.isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(-1161377931, -1626651913)
				.append(this.issued).append(this.uri).append(this.vocabulary)
				.append(this.identifier).append(this.id).toHashCode();
	}

}
