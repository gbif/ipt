package org.gbif.provider.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.gbif.provider.model.voc.Vocabulary;
import org.hibernate.validator.NotNull;

@Entity
public class ThesaurusConcept implements BaseObject{
	private Long id;
	private String identifier;
	private Vocabulary type;
	private Date issued;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public Vocabulary getType() {
		return type;
	}
	public void setType(Vocabulary type) {
		this.type = type;
	}
	
	public Date getIssued() {
		return issued;
	}
	public void setIssued(Date issued) {
		this.issued = issued;
	}
	
	public String toString(){
		return identifier;
	}

}
