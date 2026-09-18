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
package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceTypeService;

import jakarta.inject.Inject;
import java.util.Locale;
import java.util.Map;

public class ResourceTypeServiceImpl implements ResourceTypeService {

  private final VocabulariesManager vocabManager;

  @Inject
  public ResourceTypeServiceImpl(VocabulariesManager vocabManager) {
    this.vocabManager = vocabManager;
  }

  /**
   * The resource's coreType could be null. This could happen because before 2.0.3 it was not saved to resource.xml.
   * During upgrades to 2.0.3, a bug in MetadataAction would (wrongly) automatically set the coreType:
   * Checklist resources became Occurrence, and vice versa. This method will try to infer the coreType by matching
   * the coreRowType against the taxon and occurrence rowTypes.
   *
   * @param resource Resource
   * @return resource with coreType set if it could be inferred, or unchanged if it couldn't be inferred.
   */
  public Resource inferCoreType(Resource resource) {
    if (resource != null && resource.getCoreRowType() != null) {
      if (Constants.DWC_ROWTYPE_OCCURRENCE.equalsIgnoreCase(resource.getCoreRowType())) {
        resource.setCoreType(Resource.CoreRowType.OCCURRENCE.toString().toLowerCase());
      } else if (Constants.DWC_ROWTYPE_TAXON.equalsIgnoreCase(resource.getCoreRowType())) {
        resource.setCoreType(Resource.CoreRowType.CHECKLIST.toString().toLowerCase());
      } else if (Constants.DWC_ROWTYPE_EVENT.equalsIgnoreCase(resource.getCoreRowType())) {
        resource.setCoreType(Resource.CoreRowType.SAMPLINGEVENT.toString().toLowerCase());
      }
    }
    return resource;
  }

  /**
   * The resource's subType might not have been set using a standardized term from the dataset_subtype vocabulary.
   * All versions before 2.0.4 didn't use the vocabulary, so this method is particularly important during upgrades
   * to 2.0.4 and later. Basically, if the subType isn't recognized as belonging to the vocabulary, it is reset as
   * null. That would mean the user would then have to reselect the subtype from the Basic Metadata page.
   *
   * @param resource Resource
   * @return resource with a subtype set using the term from dataset_subtype vocabulary (assuming it has been set).
   */
  public Resource standardizeSubtype(Resource resource) {
    if (resource != null && resource.getSubtype() != null) {
      // the vocabulary key names are identifiers and standard across Locales
      // it's this key we want to persist as the subtype
      Map<String, String> subtypes =
          vocabManager.getI18nVocab(Constants.VOCAB_URI_DATASET_SUBTYPES, Locale.ENGLISH.getLanguage(), false);
      boolean usesVocab = false;
      for (Map.Entry<String, String> entry : subtypes.entrySet()) {
        // remember to do comparison regardless of case, since the subtype is stored in lowercase
        if (resource.getSubtype().equalsIgnoreCase(entry.getKey())) {
          usesVocab = true;
          break;
        }
      }
      // if the subtype doesn't use a standardized term from the vocab, it's reset to null
      if (!usesVocab) {
        resource.setSubtype(null);
      }
    }
    return resource;
  }
}
