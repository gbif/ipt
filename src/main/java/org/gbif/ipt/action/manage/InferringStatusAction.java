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

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.InferredMetadataManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * Whether the metadata of a resource is being inferred in the background, polled by the coverage pages.
 */
public class InferringStatusAction extends ManagerBaseAction {

  @Serial
  private static final long serialVersionUID = 3093372564384021418L;

  private final InferredMetadataManager inferredMetadataManager;

  // e.g. {"inferring": true}
  @Getter
  private final Map<String, Object> status = new HashMap<>();

  @Inject
  public InferringStatusAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      InferredMetadataManager inferredMetadataManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.inferredMetadataManager = inferredMetadataManager;
  }

  @Override
  public String execute() {
    status.put("inferring", resource != null && inferredMetadataManager.isInferring(resource.getShortname()));
    return SUCCESS;
  }
}
