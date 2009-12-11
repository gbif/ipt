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
package org.gbif.provider.model.factory;

import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Point;
import org.gbif.provider.model.voc.AnnotationType;
import org.gbif.provider.util.Constants;

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.util.Set;

/**
 * This class can be used to build a Darwin Core entity from an
 * {@link ImportRecord}.
 * 
 */
public class DarwinCoreFactory extends ModelBaseFactory<DarwinCore> {

  /**
   * Builds and returns a new Darwin Core entity.
   * 
   * @param resource the entity resource
   * @param rec the imported record
   * @param annotations any annotations
   * @return
   */
  public DarwinCore build(DataResource resource, ImportRecord rec,
      Set<Annotation> annotations) {
    if (rec == null) {
      return null;
    }
    DarwinCore entity = new DarwinCore();
    entity.setResource(resource);
    entity.setGuid(rec.getGuid());
    entity.setLink(rec.getLink());
    entity.setSourceId(rec.getSourceId());
    entity.setDeleted(false);
    Point loc = new Point();
    for (ExtensionProperty prop : rec.getProperties().keySet()) {
      String propVal = StringUtils.trimToNull(rec.getPropertyValue(prop));
      if (propVal != null && propVal.length() > prop.getColumnLength()
          && prop.getColumnLength() > 0) {
        propVal = propVal.substring(0, prop.getColumnLength());
        annotations.add(annotationManager.annotate(entity,
            AnnotationType.TrimmedData, String.format(
                "Exceeding data for property %s [%s] cut off", prop.getName(),
                prop.getColumnLength())));
      }
      String propName = prop.getName();
      // first try the properties which we try to persist converted as other
      // data types
      if (propName.equalsIgnoreCase("MinimumElevationInMeters")) {
        entity.setMinimumElevationInMeters(propVal);
        if (propVal != null) {
          try {
            Double typedVal = Double.valueOf(propVal);
            entity.setElevation(typedVal);
          } catch (NumberFormatException e) {
            annotations.add(annotationManager.badDataType(entity,
                "MinimumElevationInMeters", "Double", propVal));
          }
        }
      } else if (propName.equalsIgnoreCase("MinimumDepthInMeters")) {
        entity.setMinimumDepthInMeters(propVal);
        if (propVal != null) {
          try {
            Double typedVal = Double.valueOf(propVal);
            entity.setDepth(typedVal);
          } catch (NumberFormatException e) {
            annotations.add(annotationManager.badDataType(entity,
                "MinimumDepthInMeters", "Double", propVal));
          }
        }
      } else if (propName.equalsIgnoreCase("DecimalLatitude")) {
        entity.setDecimalLatitude(propVal);
        if (propVal != null) {
          try {
            loc.setLatitude(Double.valueOf(propVal));
          } catch (NumberFormatException e) {
            annotationManager.badDataType(entity, "DecimalLatitude", "Double",
                propVal);
          } catch (IllegalArgumentException e) {
            annotationManager.annotate(entity, AnnotationType.WrongDatatype,
                String.format("Latitude value '%s' is out of allowed range",
                    propVal));
          }
        }
      } else if (propName.equalsIgnoreCase("DecimalLongitude")) {
        entity.setDecimalLongitude(propVal);
        if (propVal != null) {
          try {
            loc.setLongitude(Double.valueOf(propVal));
          } catch (NumberFormatException e) {
            annotationManager.badDataType(entity, "DecimalLongitude", "Double",
                propVal);
          } catch (IllegalArgumentException e) {
            annotationManager.annotate(entity, AnnotationType.WrongDatatype,
                String.format("Longitude value '%s' is out of allowed range",
                    propVal));
          }
        }
      } else if (propName.equalsIgnoreCase("Class")) {
        // stupid case. property is called Classs because Class is a reserved
        // word in java...
        entity.setClasss(propVal);
      } else {
        // use reflection to find property
        if (!entity.setPropertyValue(prop, propVal)) {
          log.warn("Can't set unknown property DarwinCore." + propName);
        }
      }

      // now just check types and potentially annotate
      if (propVal != null && DarwinCore.isDarwinCoreTerm(propName)) {
        switch (DarwinCore.dataType(propName)) {
          case INTEGER:
            try {
              Integer.valueOf(propVal);
            } catch (NumberFormatException e) {
              annotations.add(annotationManager.badDataType(entity, propVal,
                  "Integer", propVal));
            }
            break;
          case DOUBLE:
            try {
              Double.valueOf(propVal);
            } catch (NumberFormatException e) {
              annotations.add(annotationManager.badDataType(entity, propVal,
                  "Double", propVal));
            }
            break;
          case DATE:
            try {
              Constants.dateIsoFormat().parse(propVal);
            } catch (ParseException e) {
              annotations.add(annotationManager.badDataType(entity, propVal,
                  "ISO Date", propVal));
            }
            break;
          case TOKEN:
            if (StringUtils.trimToEmpty(propVal).contains(" ")) {
              annotations.add(annotationManager.badDataType(entity, propVal,
                  "Monominal", propVal));
            }
            break;
        }
      }
    }

    // persist only valid localities
    if (loc.isValid()) {
      entity.setLocation(loc);
    }
    return entity;
  }

}
