/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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
 ***************************************************************************/

package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.validation.EmlSupport;
import org.gbif.ipt.validation.ResourceSupport;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.Role;

import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author markus
 * 
 */
public class MetadataAction extends POSTAction {
  @Inject
//the resource manager session is populated by the resource interceptor and kept alive for an entire manager session
  private ResourceManagerSession ms;
  private ResourceSupport validatorRes = new ResourceSupport();
  private EmlSupport validatorEml = new EmlSupport();
  private String section = "basic";
  private String next = "parties";
  private static final List<String> sections = Arrays.asList("basic", "parties", "geocoverage", "taxcoverage",
      "tempcoverage", "project", "methods", "citations", "collections", "physical", "keywords", "additional");

  public String getCurrentSideMenu() {
    return section;
  }

  public Eml getEml() {
    return ms.getEml();
  }

  public ResourceManagerSession getMs() {
    return ms;
  }

  public String getNext() {
    return next;
  }

  public Resource getResource() {
    return ms.getResource();
  }

  public Map getRoleOptions() {
    return Role.htmlSelectMap;
  }

  public String getSection() {
    return section;
  }

  public List<String> getSideMenuItems() {
    return sections;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    // somehow the action params in struts.xml dont seem to work right
    // we therefore take the section parameter from the requested url
    section = StringUtils.substringBetween(req.getRequestURI(), "-", ".");
    int idx = sections.indexOf(section);
    if (idx < 0 || idx == sections.size()) {
      idx = 0;
    }
    next = sections.get(idx + 1);
  }

  @Override
  public String save() throws Exception {
    ms.saveResource();
    ms.saveEml();
    return SUCCESS;
  }

  public void setMs(ResourceManagerSession ms) {
    this.ms = ms;
  }

  @Override
  public void validateHttpPostOnly() {
    validatorRes.validate(this, ms.getResource());
    validatorEml.validate(this, ms.getEml(), section);
  }

}
