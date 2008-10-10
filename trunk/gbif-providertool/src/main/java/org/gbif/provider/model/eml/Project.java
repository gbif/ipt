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
	
	
}
