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
package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import lombok.Getter;

/**
 * Whether a shortname is still free for a new resource, asked by the create resource page while it's typed, so a
 * taken one is known before an archive is uploaded. Answers for one name only, so no resources are listed.
 */
public class ShortnameAvailableAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = 5137830627641139058L;

  private final ResourceManager resourceManager;

  private String shortname;

  // e.g. {"available": false}
  @Getter
  private final Map<String, Object> status = new HashMap<>();

  @Inject
  public ShortnameAvailableAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
  }

  @Override
  public String execute() {
    // the same check as on create (CreateResourceAction.save)
    status.put("available", StringUtils.isNotBlank(shortname) && resourceManager.get(shortname) == null);
    return SUCCESS;
  }

  @StrutsParameter
  public void setShortname(String shortname) {
    this.shortname = StringUtils.trimToNull(shortname);
  }
}
