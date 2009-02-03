package org.gbif.provider.model.eml;

import java.io.Serializable;

public class Project implements Serializable{
	private String title;
	private String _abstract;
	private String funding;
	private String studyAreaDescription;
	private String designDescription;
	private Agent personnelOriginator = new Agent();
	private Agent personnel = new Agent();
	
	public Project() {
		super();
		this.personnelOriginator.setRole(Role.ORIGINATOR);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbstract() {
		return _abstract;
	}

	public void setAbstract(String _abstract) {
		this._abstract = _abstract;
	}

	public String getFunding() {
		return funding;
	}

	public void setFunding(String funding) {
		this.funding = funding;
	}

	public String getStudyAreaDescription() {
		return studyAreaDescription;
	}

	public void setStudyAreaDescription(String studyAreaDescription) {
		this.studyAreaDescription = studyAreaDescription;
	}

	public String getDesignDescription() {
		return designDescription;
	}

	public void setDesignDescription(String designDescription) {
		this.designDescription = designDescription;
	}

	public Agent getPersonnelOriginator() {
		return personnelOriginator;
	}

	public void setPersonnelOriginator(Agent personnelOriginator) {
		this.personnelOriginator = personnelOriginator;
	}

	public Agent getPersonnel() {
		return personnel;
	}

	public void setPersonnel(Agent personnel) {
		this.personnel = personnel;
	}
	
	
}
