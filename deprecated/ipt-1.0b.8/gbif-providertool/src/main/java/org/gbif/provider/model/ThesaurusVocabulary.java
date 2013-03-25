package org.gbif.provider.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.IndexColumn;

@Entity
public class ThesaurusVocabulary implements Comparable {
	private Long id;
	private String uri;
	private String title;
	private String link;
	private List<ThesaurusConcept> concepts;
	private Date modified = new Date();
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	
	@OneToMany(mappedBy="vocabulary",fetch=FetchType.LAZY,cascade=CascadeType.ALL)
	@IndexColumn(name = "conceptOrder",base=0, nullable=false)
	public List<ThesaurusConcept> getConcepts() {
		return concepts;
	}
	public void setConcepts(List<ThesaurusConcept> concepts) {
		this.concepts = concepts;
	}
	public void addConcept(ThesaurusConcept concept) {
		if (concepts == null) {
			concepts = new LinkedList<ThesaurusConcept>();
		}
		concept.setVocabulary(this);
		concepts.add(concept);
	}	
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}

	
	public String toString(){
		return title;
	}
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		ThesaurusVocabulary myClass = (ThesaurusVocabulary) object;
		return new CompareToBuilder().append(this.modified, myClass.modified)
				.append(this.title, myClass.title)
				.append(this.uri, myClass.uri).append(this.id, myClass.id)
				.toComparison();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof ThesaurusVocabulary)) {
			return false;
		}
		ThesaurusVocabulary rhs = (ThesaurusVocabulary) object;
		return new EqualsBuilder().append(this.modified, rhs.modified).append(
				this.title, rhs.title).append(this.uri, rhs.uri).append(
				this.id, rhs.id).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(-1469035489, -1512452511).append(
				this.modified).append(this.title).append(this.uri).append(
				this.id).toHashCode();
	}
}
