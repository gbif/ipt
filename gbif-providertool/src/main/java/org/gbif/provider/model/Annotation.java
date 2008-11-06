package org.gbif.provider.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKey;
import org.hibernate.validator.NotNull;

@Entity
public class Annotation implements BaseObject{
	private Long id;
	@NotNull
	private String guid;
	private Integer probability;
	private String note;
	private Map<ExtensionProperty, String> proposal = new HashMap<ExtensionProperty, String>();
	private Date created = new Date();
	private String creator;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public Integer getProbability() {
		return probability;
	}
	public void setProbability(Integer probability) {
		this.probability = probability;
	}
	@Lob
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}

//	@MapKey(columns = @Column(name = "property_fk"))
	@CollectionOfElements
	public Map<ExtensionProperty, String> getProposal() {
		return proposal;
	}
	public void setProposal(Map<ExtensionProperty, String> proposal) {
		this.proposal = proposal;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	@Column(length=64)
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
}
