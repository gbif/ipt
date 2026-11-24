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

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.ExtensionManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * What the extension page shows besides the extension itself, shared by the administration page
 * ({@link ExtensionsAction}) and the read-only page for managers ({@link org.gbif.ipt.action.manage.ExtensionAction}),
 * which shows everything but the resources using it.
 */
public final class ExtensionPageSupport {

  private ExtensionPageSupport() {
  }

  /**
   * @return the resource's title, or its shortname if it has none
   */
  public static String titleOf(Resource r) {
    return StringUtils.defaultIfBlank(r.getTitle(), r.getShortname());
  }

  /**
   * @return the last part of a row type, e.g. Occurrence for http://rs.tdwg.org/dwc/terms/Occurrence
   */
  public static String shortName(String rowType) {
    int i = Math.max(rowType.lastIndexOf('/'), rowType.lastIndexOf('#'));
    return i >= 0 && i < rowType.length() - 1 ? rowType.substring(i + 1) : rowType;
  }

  /**
   * @return the extension's terms by group, groups in the order of the extension's definition
   */
  public static Map<String, List<ExtensionProperty>> propertiesByGroup(Extension extension) {
    return extension.getProperties().stream()
        .collect(Collectors.groupingBy(p -> StringUtils.trimToEmpty(p.getGroup()), LinkedHashMap::new, Collectors.toList()));
  }

  /**
   * @return short names of the core types an extension can be added to; null for a core type
   */
  public static List<String> worksWith(Extension extension, ExtensionManager extensionManager) {
    if (extension.getRowType() == null || extension.isCore()) {
      return null;
    }
    return AppConfig.getCoreRowTypes().stream()
        .filter(core -> extensionManager.isSuitableFor(extension, core))
        .map(ExtensionPageSupport::shortName)
        .collect(Collectors.toList());
  }

  /**
   * Fills in the resources mapping the extension, with how many of its terms each maps (by title), and the resources
   * mapping each term (by qualified name). A term counts as mapped when it has a source column or a default value, as
   * in {@link ExtensionMapping#isMapped}.
   */
  public static void populateUsage(Extension extension, List<Resource> resources,
                                   List<ExtensionsAction.ResourceUsage> extensionUsage,
                                   Map<String, List<Resource>> termUsedBy) {
    Set<String> terms = extension.getProperties().stream()
        .map(ExtensionProperty::getQualname)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    List<Resource> sorted = new ArrayList<>(resources);
    sorted.sort(Comparator.comparing(ExtensionPageSupport::titleOf, String.CASE_INSENSITIVE_ORDER));
    for (Resource r : sorted) {
      List<ExtensionMapping> mappings = r.getMappings(extension.getRowType());
      if (mappings.isEmpty()) {
        continue;
      }
      // a resource can map the same extension more than once, e.g. from two sources
      Set<String> mapped = new HashSet<>();
      for (ExtensionMapping m : mappings) {
        for (PropertyMapping pm : m.getFields()) {
          if (pm.getTerm() != null && (pm.getIndex() != null || pm.getDefaultValue() != null)
              && terms.contains(pm.getTerm().qualifiedName())) {
            mapped.add(pm.getTerm().qualifiedName());
          }
        }
      }
      extensionUsage.add(new ExtensionsAction.ResourceUsage(r, mapped.size()));
      mapped.forEach(t -> termUsedBy.computeIfAbsent(t, k -> new ArrayList<>()).add(r));
    }
  }

  /**
   * @return the number of terms with a label in each language, by language name
   */
  public static Map<String, Integer> labelTranslations(Extension extension, Function<String, String> languageName) {
    Map<String, Integer> counts = new HashMap<>();
    for (ExtensionProperty p : extension.getProperties()) {
      p.getTranslations().forEach((lang, t) -> {
        if (lang != null && t != null && StringUtils.isNotBlank(t.getLabel())) {
          counts.merge(lang, 1, Integer::sum);
        }
      });
    }
    Map<String, Integer> result = new LinkedHashMap<>();
    counts.entrySet().stream()
        .sorted(Comparator.comparing(e -> languageName.apply(e.getKey()), String.CASE_INSENSITIVE_ORDER))
        .forEach(e -> result.put(e.getKey(), e.getValue()));
    return result;
  }
}
