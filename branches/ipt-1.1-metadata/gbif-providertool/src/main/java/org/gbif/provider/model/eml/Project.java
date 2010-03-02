/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.model.eml;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * A class encapsulating the project information
 */
public class Project implements Serializable {
	/**
	 * Generated
	 */
	private static final long serialVersionUID = 2224956553560612242L;

	/**
	 * A descriptive title for the research project. 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-project.html#title
	 */
	private String title;
	
	/**
	 * The Personnel field extends ResponsibleParty with role information and is used to document 
	 * people involved in a research project by providing contact information and their role in the project. 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-project.html#personnel
	 */
	private Agent personnel = new Agent();
	
	/**
	 * The funding field is used to provide information about funding sources for the project such as: grant and 
	 * contract numbers; names and addresses of funding sources. 
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-project.html#funding
	 */
	private String funding;
	
	/**
	 * The studyAreaDescription field documents the physical area associated with the research project. It can include 
	 * descriptions of the geographic, temporal, and taxonomic coverage of the research location.
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-project.html#descriptor
	 */
	private String studyAreaDescription;
	
	/**
	 * A general description in textual form describing some aspect of the study area
	 * @see http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-project.html#designDescription
	 */
	private String designDescription;

	/**
	 * Required by Struts2
	 */
	public Project() {
		personnel.setRole(Role.RESPONSIBLE_PARTY);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Project)) {
			return false;
		}
		Project o = (Project) other;
		return equal(title, o.title) && equal(funding, o.funding)
				&& equal(studyAreaDescription, o.studyAreaDescription) && equal(designDescription, o.designDescription) 
				&& equal(personnel, o.personnel);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(title, funding, studyAreaDescription, designDescription, personnel);
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the personnel
	 */
	public Agent getPersonnel() {
		return personnel;
	}

	/**
	 * @param personnel the personnel to set
	 */
	public void setPersonnel(Agent personnel) {
		this.personnel = personnel;
	}

	/**
	 * @return the funding
	 */
	public String getFunding() {
		return funding;
	}

	/**
	 * @param funding the funding to set
	 */
	public void setFunding(String funding) {
		this.funding = funding;
	}

	/**
	 * @return the studyAreaDescription
	 */
	public String getStudyAreaDescription() {
		return studyAreaDescription;
	}

	/**
	 * @param studyAreaDescription the studyAreaDescription to set
	 */
	public void setStudyAreaDescription(String studyAreaDescription) {
		this.studyAreaDescription = studyAreaDescription;
	}

	/**
	 * @return the designDescription
	 */
	public String getDesignDescription() {
		return designDescription;
	}

	/**
	 * @param designDescription the designDescription to set
	 */
	public void setDesignDescription(String designDescription) {
		this.designDescription = designDescription;
	}
}
