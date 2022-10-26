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
package org.gbif.ipt.model.datapackage.metadata.camtrap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * GeoJSON Object
 * <p>
 * This object represents a geometry, feature, or collection of features.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Geojson implements Serializable {

  private final static long serialVersionUID = -7011418636212199463L;

  /**
   * Type
   * <p>
   * The type of GeoJSON object.
   * (Required)
   */
  @JsonProperty("type")
  @NotNull
  private Geojson.Type type = Geojson.Type.POLYGON;

  /**
   * Coordinate Reference System (CRS)
   * <p>
   * The coordinate reference system (CRS) of a GeoJSON object is determined by its `crs` member (referred to as the CRS object below). If an object has no crs member, then its parent or grandparent object's crs member may be acquired. If no crs member can be so acquired, the default CRS shall apply to the GeoJSON object.
   * <p>
   * * The default CRS is a geographic coordinate reference system, using the WGS84 datum, and with longitude and latitude units of decimal degrees.
   * <p>
   * * The value of a member named `crs` must be a JSON object (referred to as the CRS object below) or JSON null. If the value of CRS is null, no CRS can be assumed.
   * <p>
   * * The crs member should be on the top-level GeoJSON object in a hierarchy (in feature collection, feature, geometry order) and should not be repeated or overridden on children or grandchildren of the object.
   * <p>
   * * A non-null CRS object has two mandatory members: `type` and `properties`.
   * <p>
   * * The value of the type member must be a string, indicating the type of CRS object.
   * <p>
   * * The value of the properties member must be an object.
   * <p>
   * * CRS shall not change coordinate ordering.
   */
  @JsonProperty("crs")
  private Object crs;

  /**
   * Bounding Box
   * <p>
   * To include information on the coordinate range for geometries, features, or feature collections, a GeoJSON object may have a member named `bbox`. The value of the bbox member must be a 2*n array where n is the number of dimensions represented in the contained geometries, with the lowest values for all axes followed by the highest values. The axes order of a bbox follows the axes order of geometries. In addition, the coordinate reference system for the bbox is assumed to match the coordinate reference system of the GeoJSON object of which it is a member.
   */
  @JsonProperty("bbox")
  @Size(min = 4)
  @Valid
  private List<Double> bbox = new ArrayList<>();

  @SuppressWarnings("FieldMayBeFinal")
  @JsonIgnore
  @Valid
  private Map<String, Object> additionalProperties = new HashMap<>();

  /**
   * Type
   * <p>
   * The type of GeoJSON object.
   * (Required)
   */
  @JsonProperty("type")
  public Geojson.Type getType() {
    return type;
  }

  /**
   * Type
   * <p>
   * The type of GeoJSON object.
   * (Required)
   */
  @JsonProperty("type")
  public void setType(Geojson.Type type) {
    this.type = type;
  }

  /**
   * Coordinate Reference System (CRS)
   * <p>
   * The coordinate reference system (CRS) of a GeoJSON object is determined by its `crs` member (referred to as the CRS object below). If an object has no crs member, then its parent or grandparent object's crs member may be acquired. If no crs member can be so acquired, the default CRS shall apply to the GeoJSON object.
   * <p>
   * * The default CRS is a geographic coordinate reference system, using the WGS84 datum, and with longitude and latitude units of decimal degrees.
   * <p>
   * * The value of a member named `crs` must be a JSON object (referred to as the CRS object below) or JSON null. If the value of CRS is null, no CRS can be assumed.
   * <p>
   * * The crs member should be on the top-level GeoJSON object in a hierarchy (in feature collection, feature, geometry order) and should not be repeated or overridden on children or grandchildren of the object.
   * <p>
   * * A non-null CRS object has two mandatory members: `type` and `properties`.
   * <p>
   * * The value of the type member must be a string, indicating the type of CRS object.
   * <p>
   * * The value of the properties member must be an object.
   * <p>
   * * CRS shall not change coordinate ordering.
   */
  @JsonProperty("crs")
  public Object getCrs() {
    return crs;
  }

  /**
   * Coordinate Reference System (CRS)
   * <p>
   * The coordinate reference system (CRS) of a GeoJSON object is determined by its `crs` member (referred to as the CRS object below). If an object has no crs member, then its parent or grandparent object's crs member may be acquired. If no crs member can be so acquired, the default CRS shall apply to the GeoJSON object.
   * <p>
   * * The default CRS is a geographic coordinate reference system, using the WGS84 datum, and with longitude and latitude units of decimal degrees.
   * <p>
   * * The value of a member named `crs` must be a JSON object (referred to as the CRS object below) or JSON null. If the value of CRS is null, no CRS can be assumed.
   * <p>
   * * The crs member should be on the top-level GeoJSON object in a hierarchy (in feature collection, feature, geometry order) and should not be repeated or overridden on children or grandchildren of the object.
   * <p>
   * * A non-null CRS object has two mandatory members: `type` and `properties`.
   * <p>
   * * The value of the type member must be a string, indicating the type of CRS object.
   * <p>
   * * The value of the properties member must be an object.
   * <p>
   * * CRS shall not change coordinate ordering.
   */
  @JsonProperty("crs")
  public void setCrs(Object crs) {
    this.crs = crs;
  }

  /**
   * Bounding Box
   * <p>
   * To include information on the coordinate range for geometries, features, or feature collections, a GeoJSON object may have a member named `bbox`. The value of the bbox member must be a 2*n array where n is the number of dimensions represented in the contained geometries, with the lowest values for all axes followed by the highest values. The axes order of a bbox follows the axes order of geometries. In addition, the coordinate reference system for the bbox is assumed to match the coordinate reference system of the GeoJSON object of which it is a member.
   */
  @JsonProperty("bbox")
  public List<Double> getBbox() {
    return bbox;
  }

  /**
   * Bounding Box
   * <p>
   * To include information on the coordinate range for geometries, features, or feature collections, a GeoJSON object may have a member named `bbox`. The value of the bbox member must be a 2*n array where n is the number of dimensions represented in the contained geometries, with the lowest values for all axes followed by the highest values. The axes order of a bbox follows the axes order of geometries. In addition, the coordinate reference system for the bbox is assumed to match the coordinate reference system of the GeoJSON object of which it is a member.
   */
  @JsonProperty("bbox")
  public void setBbox(List<Double> bbox) {
    this.bbox = bbox;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }


  /**
   * Type
   * <p>
   * The type of GeoJSON object.
   */
  public enum Type {

    POINT("Point"),
    MULTI_POINT("MultiPoint"),
    LINE_STRING("LineString"),
    MULTI_LINE_STRING("MultiLineString"),
    POLYGON("Polygon"),
    MULTI_POLYGON("MultiPolygon"),
    GEOMETRY_COLLECTION("GeometryCollection"),
    FEATURE("Feature"),
    FEATURE_COLLECTION("FeatureCollection");
    private final String value;
    private final static Map<String, Geojson.Type> CONSTANTS = new HashMap<>();

    static {
      for (Geojson.Type c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    Type(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }

    @JsonValue
    public String value() {
      return this.value;
    }

    @JsonCreator
    public static Geojson.Type fromValue(String value) {
      Geojson.Type constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Geojson.class.getSimpleName() + "[", "]")
        .add("type=" + type)
        .add("crs=" + crs)
        .add("bbox=" + bbox)
        .add("additionalProperties=" + additionalProperties)
        .toString();
  }
}
