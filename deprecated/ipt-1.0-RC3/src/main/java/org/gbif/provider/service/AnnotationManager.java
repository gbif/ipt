/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.service;

import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.model.voc.AnnotationType;

import java.util.Date;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public interface AnnotationManager extends
    GenericResourceRelatedManager<Annotation> {
  Annotation annotate(CoreRecord record, AnnotationType type, String note);

  Annotation annotate(CoreRecord record, AnnotationType type, String actor,
      String note);

  Annotation annotate(CoreRecord record, String type,
      boolean removeDuringImport, String note);

  Annotation annotate(CoreRecord record, String type, String actor,
      boolean removeDuringImport, String note);

  Annotation annotate(Resource resource, ExtensionRecord record,
      AnnotationType type, String note);

  Annotation annotate(Resource resource, String sourceId, String guid,
      AnnotationType type, String actor, String note);

  Annotation annotate(Resource resource, String sourceId, String guid,
      String type, String actor, boolean removeDuringImport, String note);

  // annotations for the whole resource will result in guid=null
  Annotation annotateResource(Resource resource, String note);

  // annotations linked only to resource with guid=null
  Annotation badCoreRecord(Resource resource, String id, String note);

  Annotation badDataType(CoreRecord record, String property, String dataType,
      String value);

  Annotation badExtensionRecord(Resource resource, Extension extension,
      String sourceId, String note);

  Annotation badReference(CoreRecord record, String property, String id,
      String note);

  List<Annotation> getByActor(Long resourceId, String actor);

  List<Annotation> getByLatest(Long resourceId, Date earliestDate);

  List<Annotation> getByRecord(Long resourceId, String guid);

  List<Annotation> getByType(Long resourceId, String type);

  int updateCoreIds(DataResource resource);
}
