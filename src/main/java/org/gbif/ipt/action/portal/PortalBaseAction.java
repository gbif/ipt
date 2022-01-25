/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.action.portal;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

/**
 * The base of all portal actions.
 */
public class PortalBaseAction extends BaseAction {

  protected ResourceManager resourceManager;
  protected Resource resource;
  protected BigDecimal version;
  public static final String UNSPECIFIED_VERSION = "unspecified";
  private static final Logger LOG = LogManager.getLogger(PortalBaseAction.class);

  @Inject
  public PortalBaseAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
  }

  /**
   * Return the resource.
   * 
   * @return the resource
   */
  public Resource getResource() {
    return resource;
  }

  @Override
  public void prepare() {
    super.prepare();
    // look for resource parameter
    String res = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_RESOURCE));
    if (res == null) {
      // try session instead
      try {
        res = (String) session.get(Constants.SESSION_RESOURCE);
      } catch (Exception e) {
        // swallow. if session is not yet opened we get an exception here...
      }
    }
    if (res != null) {
      resource = resourceManager.get(res);
    }
    // look for version parameter
    String v = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_VERSION));
    if (StringUtils.isNotBlank(v)) {
      try {
        setVersion(new BigDecimal(v));
      } catch (NumberFormatException e) {
        LOG.error("Parameter version (v) was not a valid number: " + v);
      }
    }
  }

  /**
   * Return the version number requested. Null version is equal to the latest published version.
   * 
   * @return the version number requested
   */
  @Nullable
  public BigDecimal getVersion() {
    return version;
  }

  /**
   * Version number of the resource requested. Null version is equal to the latest published version.
   * 
   * @param version version number requested
   */
  public void setVersion(BigDecimal version) {
    this.version = version;
  }

  /**
   * Return the version as a string. If it is null, return "unspecified".
   * 
   * @return the version as a string
   */
  protected String getStringVersion() {
    return (version == null) ? UNSPECIFIED_VERSION : String.valueOf(version);
  }

  /**
   * Resource requested.
   *
   * @param resource resource
   */
  public void setResource(Resource resource) {
    this.resource = resource;
  }
}
