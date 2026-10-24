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
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.StringUtils;

import jakarta.inject.Inject;
import java.io.Serial;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import lombok.Getter;

/**
 * Whether extensions and data package types that aren't installed can be installed from the registry, and whether an
 * installed data package type can be updated to a newer version, asked by the archive precheck on the create resource
 * page only when an archive needs one of them.
 */
public class InstallableAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = -4378281209432876117L;

  private static final Logger LOG = LogManager.getLogger(InstallableAction.class);

  private final RegistryManager registryManager;
  private final DataPackageSchemaManager schemaManager;

  private List<String> rowTypes = new ArrayList<>();
  private List<String> dataPackageTypes = new ArrayList<>();
  // name/version, e.g. camtrap-dp/1.1
  private List<String> dataPackageVersions = new ArrayList<>();

  // e.g. {"rowTypes": {"http://rs.gbif.org/terms/1.0/Multimedia": true}, "dataPackageTypes": {...},
  // "dataPackageVersions": {"camtrap-dp/1.1": false}}, empty if the registry couldn't be reached
  @Getter
  private final Map<String, Map<String, Boolean>> installable = new HashMap<>();

  @Inject
  public InstallableAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      RegistryManager registryManager,
      DataPackageSchemaManager schemaManager) {
    super(textProvider, cfg, registrationManager);
    this.registryManager = registryManager;
    this.schemaManager = schemaManager;
  }

  @Override
  public String execute() {
    try {
      if (!rowTypes.isEmpty()) {
        Set<String> registered = registryManager.getExtensions().stream()
            .map(Extension::getRowType)
            .collect(Collectors.toSet());
        installable.put("rowTypes", rowTypes.stream()
            .distinct()
            .collect(Collectors.toMap(rowType -> rowType, registered::contains)));
      }
      if (!dataPackageTypes.isEmpty()) {
        List<DataPackageSchema> registered = registryManager.getLatestDataPackageSchemas();
        installable.put("dataPackageTypes", dataPackageTypes.stream()
            .distinct()
            .collect(Collectors.toMap(type -> type, type -> registered.stream().anyMatch(s -> matches(s, type)))));
      }
      if (!dataPackageVersions.isEmpty()) {
        installable.put("dataPackageVersions", dataPackageVersions.stream()
            .distinct()
            .collect(Collectors.toMap(nameAndVersion -> nameAndVersion, this::canBeUpdatedTo)));
      }
    } catch (RegistryException e) {
      // without the registry it's unknown, the precheck then offers no install link
      LOG.warn("Could not check the registry for installable extensions and data package types", e);
      installable.clear();
    }
    return SUCCESS;
  }

  /**
   * An installed data package type is updated to the latest version compatible with it (see the data packages admin
   * page), so a newer version can be had if that one is at least as new.
   *
   * @param nameAndVersion e.g. camtrap-dp/1.1
   */
  private boolean canBeUpdatedTo(String nameAndVersion) {
    int slash = nameAndVersion.lastIndexOf('/');
    if (slash <= 0) {
      return false;
    }
    String name = nameAndVersion.substring(0, slash);
    String version = nameAndVersion.substring(slash + 1);
    String installedVersion = schemaManager.getVersion(name);
    if (installedVersion == null) {
      return false;
    }
    String latestCompatible = registryManager.getLatestCompatibleSchemaVersion(name, installedVersion);
    return latestCompatible != null && compareVersions(latestCompatible, version) >= 0;
  }

  /**
   * Compares versions made of numbers, like 1.0.10 and 1.0.2. Any other versions are only equal or not, a version that
   * isn't the same then counts as older, so nothing is offered for it.
   */
  static int compareVersions(String a, String b) {
    if (a.equals(b)) {
      return 0;
    }
    if (!a.matches("\\d+(\\.\\d+)*") || !b.matches("\\d+(\\.\\d+)*")) {
      return -1;
    }
    String[] partsA = a.split("\\.");
    String[] partsB = b.split("\\.");
    for (int i = 0; i < Math.max(partsA.length, partsB.length); i++) {
      // whatever its length, a version comes from a datapackage.json
      BigInteger partA = i < partsA.length ? new BigInteger(partsA[i]) : BigInteger.ZERO;
      BigInteger partB = i < partsB.length ? new BigInteger(partsB[i]) : BigInteger.ZERO;
      if (!partA.equals(partB)) {
        return partA.compareTo(partB);
      }
    }
    return 0;
  }

  private static boolean matches(DataPackageSchema schema, String identifier) {
    return StringUtils.equalsWithoutProtocol(schema.getIdentifier(), identifier)
        || identifier.equals(schema.getName())
        || (schema.getPreviousIdentifiers() != null && schema.getPreviousIdentifiers().stream()
            .anyMatch(previous -> StringUtils.equalsWithoutProtocol(previous, identifier)));
  }

  @StrutsParameter
  public void setRowType(List<String> rowTypes) {
    this.rowTypes = rowTypes;
  }

  @StrutsParameter
  public void setDataPackageType(List<String> dataPackageTypes) {
    this.dataPackageTypes = dataPackageTypes;
  }

  @StrutsParameter
  public void setDataPackageVersion(List<String> dataPackageVersions) {
    this.dataPackageVersions = dataPackageVersions;
  }
}
