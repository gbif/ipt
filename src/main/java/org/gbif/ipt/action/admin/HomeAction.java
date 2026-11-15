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
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.IptColorScheme;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/**
 * The administration home page: a card per administration page, most with one line about the IPT's state.
 * The facts come from memory or the data directory; the page never calls the GBIF Registry.
 */
public class HomeAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = 6041482193930893683L;

  // the link colour's default before it became the primary green (#3294)

  private final ResourceManager resourceManager;
  private final UserAccountManager userManager;
  private final ExtensionManager extensionManager;
  private final DataPackageSchemaManager schemaManager;
  private final DataDir dataDir;

  private int failedResourcesCount;
  private int resourcesWithNoticesCount;
  private int usersCount;
  private int adminsCount;
  private int deactivatedUsersCount;
  private int organisationsCount;
  private int coreTypesCount;
  private int extensionsCount;
  private int extensionUpdatesCount;
  private int schemasCount;
  private int schemaUpdatesCount;
  private int scheduledCount;
  private Resource nextScheduled;

  @Inject
  public HomeAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      UserAccountManager userManager,
      ExtensionManager extensionManager,
      DataPackageSchemaManager schemaManager,
      DataDir dataDir) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
    this.userManager = userManager;
    this.extensionManager = extensionManager;
    this.schemaManager = schemaManager;
    this.dataDir = dataDir;
  }

  @Override
  public String execute() {
    failedResourcesCount = resourceManager.listLoadFailures().size();
    resourcesWithNoticesCount = resourceManager.listWithLoadNotices().size();

    List<User> users = userManager.list();
    usersCount = users.size();
    adminsCount = (int) users.stream().filter(User::hasAdminRights).count();
    deactivatedUsersCount = (int) users.stream().filter(User::isDeactivated).count();

    // as the organizations page lists them: without the built-in "No organization" placeholder
    organisationsCount = (int) registrationManager.listAll().stream()
        .filter(o -> !Constants.DEFAULT_ORG_KEY.equals(o.getKey()))
        .count();

    // isLatest is set from the Registry on startup and on the extensions page (true when the Registry is unreachable)
    List<Extension> extensions = extensionManager.list();
    coreTypesCount = (int) extensions.stream().filter(Extension::isCore).count();
    extensionsCount = extensions.size() - coreTypesCount;
    extensionUpdatesCount = (int) extensions.stream().filter(e -> !e.isLatest()).count();

    // isUpdatable is only set when the data packages page checks the Registry; isLatest is false until then
    List<DataPackageSchema> schemas = schemaManager.list();
    schemasCount = schemas.size();
    schemaUpdatesCount = (int) schemas.stream().filter(DataPackageSchema::isUpdatable).count();

    // the same resources as the scheduled publications page lists
    List<Resource> scheduled = resourceManager.list().stream().filter(Resource::usesAutoPublishing).toList();
    scheduledCount = scheduled.size();
    nextScheduled = scheduled.stream()
        .filter(r -> r.getNextPublished() != null)
        .min(Comparator.comparing(Resource::getNextPublished))
        .orElse(null);

    return SUCCESS;
  }

  /**
   * @return number of resource folders that failed to load on startup
   */
  public int getFailedResourcesCount() {
    return failedResourcesCount;
  }

  /**
   * @return number of loaded resources the IPT worked around something for, e.g. unreadable metadata
   */
  public int getResourcesWithNoticesCount() {
    return resourcesWithNoticesCount;
  }

  public boolean isTestInstallation() {
    return cfg.isTestInstallation();
  }

  public String getIptBaseUrl() {
    return cfg.getBaseUrl();
  }

  /**
   * @return the organisation hosting this IPT, or null when the IPT isn't registered with GBIF
   */
  public Organisation getHostingOrganisation() {
    return registrationManager.getHostingOrganisation();
  }

  public int getOrganisationsCount() {
    return organisationsCount;
  }

  public int getUsersCount() {
    return usersCount;
  }

  public int getAdminsCount() {
    return adminsCount;
  }

  public int getDeactivatedUsersCount() {
    return deactivatedUsersCount;
  }

  public int getCoreTypesCount() {
    return coreTypesCount;
  }

  public int getExtensionsCount() {
    return extensionsCount;
  }

  public int getExtensionUpdatesCount() {
    return extensionUpdatesCount;
  }

  public int getSchemasCount() {
    return schemasCount;
  }

  public int getSchemaUpdatesCount() {
    return schemaUpdatesCount;
  }

  public boolean isAutoPublishingEnabled() {
    return cfg.isAutoPublishingEnabled();
  }

  public boolean isAutoPublishingAdminOnly() {
    return cfg.isAutoPublishingAdminOnly();
  }

  public int getScheduledCount() {
    return scheduledCount;
  }

  /**
   * @return the scheduled resource that publishes next, or null if none has a next publication date
   */
  public Resource getNextScheduled() {
    return nextScheduled;
  }

  /**
   * @return true when any colour differs from the defaults
   */
  public boolean isCustomColours() {
    return !new IptColorScheme().equals(cfg.getColorSchemeConfig());
  }

  /**
   * @return true while the new default colours replace the administrator's own colours, until Appearance is saved
   */
  public boolean isColorSchemeReplaced() {
    return cfg.isColorSchemeReplaced();
  }

  /**
   * @return true when an administrator uploaded a logo, checked the way AppFileAction serves it
   */
  public boolean isCustomLogo() {
    return Arrays.stream(Constants.IMAGE_TYPES).anyMatch(type -> dataDir.appLogoFile(type).exists());
  }
}
