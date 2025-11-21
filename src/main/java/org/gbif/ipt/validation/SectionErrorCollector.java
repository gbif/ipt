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
package org.gbif.ipt.validation;

import org.gbif.ipt.model.voc.MetadataSection;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
public class SectionErrorCollector implements ErrorCollector {

  private final Map<String, ActionValidationResult> result = new LinkedHashMap<>();
  @Setter
  private MetadataSection currentSection;

  public SectionErrorCollector() {
    // Pre-fill map with empty results for all sections
    for (MetadataSection section : MetadataSection.values()) {
      result.put(section.toString(), new ActionValidationResult());
    }
  }

  private ActionValidationResult section() {
    return result.get(currentSection.toString());
  }

  @Override
  public void addFieldError(String field, String msg) {
    section().addFieldError(field, msg);
  }

  @Override
  public void addActionError(String msg) {
    section().addActionError(msg);
  }

  @Override
  public void addActionWarning(String msg) {
    section().addWarning(msg);
  }

  @Override
  public boolean hasActionErrors() {
    return result.values().stream()
        .anyMatch(ActionValidationResult::hasActionErrors);
  }

  @Override
  public boolean hasFieldErrors() {
    return result.values().stream()
        .anyMatch(ActionValidationResult::hasFieldErrors);
  }

  @Override
  public boolean hasErrors() {
    return hasActionErrors() || hasFieldErrors();
  }
}
