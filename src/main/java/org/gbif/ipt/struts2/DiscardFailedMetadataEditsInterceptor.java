/*
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
package org.gbif.ipt.struts2;

import org.gbif.ipt.action.manage.CamtrapMetadataAction;
import org.gbif.ipt.action.manage.DataPackageMetadataAction;
import org.gbif.ipt.action.manage.ManagerBaseAction;
import org.gbif.ipt.action.manage.MetadataAction;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.ResourceMetadataLoader;

import java.io.Serial;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ActionInvocation;
import org.apache.struts2.action.Action;
import org.apache.struts2.interceptor.AbstractInterceptor;

/**
 * Discards the values of a metadata form that failed to save.
 * <p>
 * The metadata forms (EML, Camtrap DP and other data package metadata) edit the resource's metadata in memory: Struts
 * sets the submitted values on it before they are validated. When the save fails (validation errors, or content
 * removed by {@link SanitizeHtmlFilter}), the form is shown again with the submitted values and the errors, but the
 * values would stay in memory and be written to disk by the next successful save of any section.
 * <p>
 * This interceptor must wrap the whole stack: once the form has been rendered, it reloads the metadata from disk.
 */
public class DiscardFailedMetadataEditsInterceptor extends AbstractInterceptor {

  @Serial
  private static final long serialVersionUID = 4385204329735417195L;

  private static final Logger LOG = LogManager.getLogger(DiscardFailedMetadataEditsInterceptor.class);

  private transient ResourceMetadataLoader resourceMetadataLoader;
  private transient DataDir dataDir;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    String result = invocation.invoke();

    HttpServletRequest request = invocation.getInvocationContext().getServletRequest();
    if (Action.INPUT.equals(result)
        && request != null && "POST".equalsIgnoreCase(request.getMethod())
        && invocation.getAction() instanceof ManagerBaseAction action
        && action.hasErrors()
        && action.getResource() != null) {
      Resource resource = action.getResource();
      String shortname = resource.getShortname();

      // never saved: there is no file to go back to
      if (action instanceof MetadataAction) {
        if (dataDir.resourceEmlFile(shortname).exists()) {
          LOG.debug("Metadata of resource {} not saved, reloading it from eml.xml", shortname);
          resourceMetadataLoader.loadEml(resource);
        }
      } else if (action instanceof CamtrapMetadataAction || action instanceof DataPackageMetadataAction) {
        if (dataDir.resourceDatapackageMetadataFile(shortname, resource.getCoreType()).exists()) {
          LOG.debug("Data package metadata of resource {} not saved, reloading it", shortname);
          resourceMetadataLoader.loadDatapackageMetadata(resource);
        }
      }
    }

    return result;
  }

  @Inject
  public void setResourceMetadataLoader(ResourceMetadataLoader resourceMetadataLoader) {
    this.resourceMetadataLoader = resourceMetadataLoader;
  }

  @Inject
  public void setDataDir(DataDir dataDir) {
    this.dataDir = dataDir;
  }
}
