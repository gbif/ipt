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
import org.gbif.ipt.action.admin.ExtensionPageSupport;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

/**
 * The extension page for resource managers, opened from the mapping and vocabulary pages: the extension and its
 * terms, read only. No Registry check and no "Used by": administrators have those on the page under administration
 * ({@link org.gbif.ipt.action.admin.ExtensionsAction}).
 */
public class ExtensionAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = 3417269830258937424L;

  private final ExtensionManager extensionManager;
  @Getter
  private Extension extension;
  @Getter
  private Map<String, List<ExtensionProperty>> propertiesByGroup = new LinkedHashMap<>();
  // the core types the extension can be added to, by row type, as on the administration page
  @Getter
  private final Map<String, List<String>> worksWith = new HashMap<>();
  // number of terms with a label in each language, by language name
  @Getter
  private Map<String, Integer> labelTranslations = new LinkedHashMap<>();

  @Inject
  public ExtensionAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ExtensionManager extensionManager) {
    super(textProvider, cfg, registrationManager);
    this.extensionManager = extensionManager;
  }

  @Override
  public String execute() throws Exception {
    extension = id == null ? null : extensionManager.get(id);
    if (extension == null) {
      return NOT_FOUND;
    }
    propertiesByGroup = ExtensionPageSupport.propertiesByGroup(extension);
    List<String> cores = ExtensionPageSupport.worksWith(extension, extensionManager);
    if (cores != null) {
      worksWith.put(extension.getRowType(), cores);
    }
    labelTranslations = ExtensionPageSupport.labelTranslations(extension, this::languageName);
    return SUCCESS;
  }
}
