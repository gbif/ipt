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

import java.io.Serializable;

/**
 * TODO: Documentation.
 * 
 */
public class Project implements Serializable {

  private static final long serialVersionUID = 2224956553560612242L;

  // Added to support the GBIF Extended Metadata Profile:
  private String id;
  private String uuid;

  private String title;
  private String projectAbstract;
  private String funding;
  private String studyAreaDescription;
  private String designDescription;
  private Agent personnelOriginator = new Agent();
  private Agent personnel = new Agent();

  public Project() {
    super();
    this.personnelOriginator.setRole(Role.ORIGINATOR);
  }

  public String getAbstract() {
    return projectAbstract;
  }

  public String getDesignDescription() {
    return designDescription;
  }

  public String getFunding() {
    return funding;
  }

  public String getId() {
    return id;
  }

  public Agent getPersonnel() {
    return personnel;
  }

  public Agent getPersonnelOriginator() {
    return personnelOriginator;
  }

  public String getStudyAreaDescription() {
    return studyAreaDescription;
  }

  public String getTitle() {
    return title;
  }

  public String getUuid() {
    return uuid;
  }

  public void setAbstract(String projectAbstract) {
    this.projectAbstract = projectAbstract;
  }

  public void setDesignDescription(String designDescription) {
    this.designDescription = designDescription;
  }

  public void setFunding(String funding) {
    this.funding = funding;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setPersonnel(Agent personnel) {
    this.personnel = personnel;
  }

  public void setPersonnelOriginator(Agent personnelOriginator) {
    this.personnelOriginator = personnelOriginator;
  }

  public void setStudyAreaDescription(String studyAreaDescription) {
    this.studyAreaDescription = studyAreaDescription;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

}
