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

import org.gbif.api.model.registry.Network;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.KeyNamePair;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import lombok.Getter;

/**
 * The GBIF networks of a registered resource, fetched by the resource overview page after it has loaded, so a slow
 * registry doesn't hold up the page.
 * <ul>
 *   <li>resourceNetworks: the networks the resource is in, [{key, name}]</li>
 *   <li>potentialNetworks (only with all=true, for the add network dialog): the other networks, sorted by name</li>
 *   <li>available: false when the registry couldn't be reached</li>
 * </ul>
 */
public class NetworksAction extends ManagerBaseAction {

  @Serial
  private static final long serialVersionUID = -2209463521170389418L;

  private static final Logger LOG = LogManager.getLogger(NetworksAction.class);

  private final RegistryManager registryManager;

  @Getter
  private final Map<String, Object> networks = new HashMap<>();

  private boolean all;

  @Inject
  public NetworksAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      RegistryManager registryManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.registryManager = registryManager;
  }

  @Override
  public String execute() {
    if (resource == null || resource.getKey() == null) {
      networks.put("available", false);
      return SUCCESS;
    }

    try {
      List<KeyNamePair> resourceNetworks = new ArrayList<>();
      // Gson parses an empty response body as null
      List<Network> inRegistry = Objects.requireNonNullElse(registryManager.getResourceNetworks(resource), List.of());
      for (Network n : inRegistry) {
        if (n != null && n.getKey() != null) {
          String key = n.getKey().toString();
          resourceNetworks.add(new KeyNamePair(key, n.getTitle() != null ? n.getTitle() : key));
        }
      }
      networks.put("resourceNetworks", resourceNetworks);

      if (all) {
        Set<String> inResource = resourceNetworks.stream().map(KeyNamePair::getKey).collect(Collectors.toSet());
        List<KeyNamePair> allNetworks = Objects.requireNonNullElse(registryManager.getNetworksBrief(), List.of());
        List<KeyNamePair> potentialNetworks = allNetworks.stream()
            .filter(n -> n != null && n.getKey() != null && !inResource.contains(n.getKey()))
            .sorted(Comparator.comparing(n -> Objects.toString(n.getName(), ""), String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
        networks.put("potentialNetworks", potentialNetworks);
      }
      networks.put("available", true);
    } catch (RegistryException e) {
      LOG.warn("Couldn't load networks of resource {} from the registry: {}", resource.getShortname(), e.getMessage());
      networks.clear();
      networks.put("available", false);
    }
    return SUCCESS;
  }

  @StrutsParameter
  public void setAll(boolean all) {
    this.all = all;
  }
}
