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
package org.gbif.provider.geo;

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.Point;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.Constants;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;

/**
 * TODO: Documentation.
 * 
 */
public class MapUtil {
  @Autowired
  private AppConfig cfg;

  public String getWMSGoogleMapUrl(Long resourceId, Taxon taxon, Region region) {
    // ESPG:900913 doesnt work, use 4326 for now :(
    // tile size=256
    String wms = "";
    try {
      wms = getWMSUrl(resourceId, 256, 256, taxon, region, URLEncoder.encode(
          "EPSG:4326", Constants.ENCODING));
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return wms;
  }

  public String getWMSUrl(Long resourceId, int width, int height, BBox bbox,
      Taxon taxon, Region region) {
    String wms = getWMSUrl(resourceId, width, height, taxon, region);
    if (bbox == null || !bbox.isValid()) {
      bbox = BBox.newWorldInstance();
    } else {
      if (bbox.surface() == 0f) {
        if (bbox.getMax().getLatitude() + 0.1f < Point.MAX_LATITUDE) {
          bbox.getMax().setLatitude(bbox.getMax().getLatitude() + 0.1f);
          bbox.getMax().setLongitude(bbox.getMax().getLongitude() + 0.1f);
        } else {
          bbox.getMin().setLatitude(bbox.getMin().getLatitude() - 0.1f);
          bbox.getMin().setLongitude(bbox.getMin().getLongitude() - 0.1f);
        }
      }
      bbox.resize(1.2f);
      float ratio = (float) width / (float) height;
      bbox.expandToMapRatio(ratio);
    }
    // add bbox and size to WMS URL
    return String.format("%s&bbox=%s", wms, bbox.toStringWMS(), width, height);
  }

  public String getWMSUrl(Long resourceId, int width, int height, Taxon taxon,
      Region region) {
    // uses default WGS84
    try {
      return getWMSUrl(resourceId, width, height, taxon, region,
          URLEncoder.encode("EPSG:4326", Constants.ENCODING));
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return "";
  }

  public String getWMSUrl(Long resourceId, int width, int height, Taxon taxon,
      Region region, String srs) {
    LinkedList<String> filters = new LinkedList<String>();

    if (taxon != null) {
      // filters.add(String.format("<PropertyIsEqualTo><PropertyName>TaxonId</PropertyName><Literal>%s</Literal></PropertyIsEqualTo>",
      // taxon.getId()));
      filters.add(String.format(
          "<PropertyIsGreaterThanOrEqualTo><PropertyName>TaxonLft</PropertyName><Literal>%s</Literal></PropertyIsGreaterThanOrEqualTo>",
          taxon.getLft()));
      filters.add(String.format(
          "<PropertyIsLessThanOrEqualTo><PropertyName>TaxonRgt</PropertyName><Literal>%s</Literal></PropertyIsLessThanOrEqualTo>",
          taxon.getRgt()));
    }
    if (region != null) {
      // filters.add(String.format("<PropertyIsEqualTo><PropertyName>SamplingLocationID</PropertyName><Literal>%s</Literal></PropertyIsEqualTo>",
      // region.getId()));
      filters.add(String.format(
          "<PropertyIsGreaterThanOrEqualTo><PropertyName>SamplingLocationLft</PropertyName><Literal>%s</Literal></PropertyIsGreaterThanOrEqualTo>",
          region.getLft()));
      filters.add(String.format(
          "<PropertyIsLessThanOrEqualTo><PropertyName>SamplingLocationRgt</PropertyName><Literal>%s</Literal></PropertyIsLessThanOrEqualTo>",
          region.getRgt()));
    }

    // produce final filter string
    String filter = "";
    if (!filters.isEmpty()) {
      filter = filters.poll();
      while (!filters.isEmpty()) {
        filter = String.format("<And>%s%s</And>", filters.poll(), filter);
      }
      filter = String.format("<Filter>%s</Filter>", filter);
    }
    // produce entire WMS URL
    try {
      return String.format(
          "%s/wms?request=GetMap&layers=%s&srs=%s&Format=image/png&transparent=true&width=%s&height=%s&filter=(%s)",
          cfg.getGeoserverUrl(), URLEncoder.encode(
              "gbif:resource" + resourceId, Constants.ENCODING), srs, width,
          height, URLEncoder.encode(filter, Constants.ENCODING));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
  }
}
