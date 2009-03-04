package org.gbif.provider.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.gbif.provider.util.XMLDateUtils;

@Entity
public class ThesaurusTerm implements Comparable, BaseObject{
	private Long id;
	private ThesaurusConcept concept;
	private boolean preferred;
	private String title;
	private String lang;
	private String source;
	private String relation;
	private Date created;
	private Date modified;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne(optional = false, fetch=FetchType.LAZY)
	public ThesaurusConcept getConcept() {
		return concept;
	}
	public void setConcept(ThesaurusConcept concept) {
		this.concept = concept;
	}
	
	public boolean isPreferred() {
		return preferred;
	}
	public void setPreferred(boolean preferred) {
		this.preferred = preferred;
	}
	public void setPreferred(String preferred) {
		if ("TRUE".equalsIgnoreCase(preferred)
				|| "YES".equalsIgnoreCase(preferred)
				|| "Y".equalsIgnoreCase(preferred)
				|| "T".equalsIgnoreCase(preferred)
				|| "1".equalsIgnoreCase(preferred)) {
			this.preferred = true;
		} else {
			this.preferred = false;
		}
	}
	
	@org.hibernate.annotations.Index(name="term_title")
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	@Column(length=8)
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	@Column(length=128)
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public void setCreatedXSDDateTime(String created) {
		setCreated(XMLDateUtils.toDate(created));
	}
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	public void setModifiedXSDDateTime(String modified) {
		setModified(XMLDateUtils.toDate(modified));
	}
	
	public String toString(){
		return String.format("%s [%s]",title, lang);
	}
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		ThesaurusTerm myClass = (ThesaurusTerm) object;
		return new CompareToBuilder().append(this.modified, myClass.modified)
				.append(this.created, myClass.created).append(this.lang,
						myClass.lang).append(this.title, myClass.title).append(
						this.preferred, myClass.preferred).append(this.concept,
						myClass.concept)
				.append(this.relation, myClass.relation).append(this.source,
						myClass.source).append(this.id, myClass.id)
				.toComparison();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof ThesaurusTerm)) {
			return false;
		}
		ThesaurusTerm rhs = (ThesaurusTerm) object;
		return new EqualsBuilder().append(this.modified, rhs.modified).append(
				this.created, rhs.created).append(this.lang, rhs.lang).append(
				this.title, rhs.title).append(this.preferred, rhs.preferred)
				.append(this.concept, rhs.concept).append(this.relation,
						rhs.relation).append(this.source, rhs.source).append(
						this.id, rhs.id).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(877564629, -1011155925)
				.append(this.modified).append(this.created).append(this.lang)
				.append(this.title).append(this.preferred).append(this.concept)
				.append(this.relation).append(this.source).append(this.id)
				.toHashCode();
	}
}
