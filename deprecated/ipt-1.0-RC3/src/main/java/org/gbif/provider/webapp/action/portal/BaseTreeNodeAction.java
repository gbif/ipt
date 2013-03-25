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
package org.gbif.provider.webapp.action.portal;

import org.gbif.provider.geo.MapUtil;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.TreeNodeManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 * @param <E>
 */
public class BaseTreeNodeAction<T extends org.gbif.provider.model.TreeNodeBase<T, E>, E extends Enum>
    extends BaseDataResourceAction {

  public int width = OccResourceStatsAction.DEFAULT_WIDTH;
  public int height = OccResourceStatsAction.DEFAULT_HEIGHT;

  @Autowired
  protected MapUtil mapUtil;

  @Autowired
  protected DarwinCoreManager darwinCoreManager;

  protected TreeNodeManager<T, E> treeNodeManager;
  protected Long id;
  protected T node;
  protected List<DarwinCore> occurrences;
  protected String geoserverMapUrl;
  protected String geoserverMapBBox;

  public BaseTreeNodeAction(TreeNodeManager<T, E> treeNodeManager) {
    super();
    this.treeNodeManager = treeNodeManager;
  }

  @Override
  public String execute() {
    if (id != null) {
      node = treeNodeManager.get(id);
      // geoserver map link
      if (node != null) {
        geoserverMapUrl = getGeoserverUrl(node);
        BBox bbox = BBox.newWorldInstance();
        if (node.getBbox() != null && node.getBbox().isValid()) {
          bbox = node.getBbox();
        } else if (resource != null && resource.getGeoCoverage() != null
            && resource.getGeoCoverage().isValid()) {
          bbox = resource.getGeoCoverage();
        }
        geoserverMapBBox = bbox.toStringWMS();
      } else {
        return RECORD404;
      }
    }
    return SUCCESS;
  }

  public String getGeoserverMapBBox() {
    return geoserverMapBBox;
  }

  public String getGeoserverMapUrl() {
    return geoserverMapUrl;
  }

  public int getHeight() {
    return height;
  }

  public Long getId() {
    return id;
  }

  public T getNode() {
    return node;
  }

  public List<DarwinCore> getOccurrences() {
    return occurrences;
  }

  public Long getRegionId() {
    return id;
  }

  public int getWidth() {
    return width;
  }

  public String occurrences() {
    if (resourceId != null && id != null) {
      node = treeNodeManager.get(id);
      if (node != null) {
        geoserverMapUrl = getGeoserverUrl(node);
        BBox bbox = BBox.newWorldInstance();
        if (node.getBbox() != null && node.getBbox().isValid()) {
          bbox = node.getBbox();
        } else if (resource != null && resource.getGeoCoverage() != null
            && resource.getGeoCoverage().isValid()) {
          bbox = resource.getGeoCoverage();
        }
        geoserverMapBBox = bbox.toStringWMS();
      } else {
        return RECORD404;
      }
    }
    return SUCCESS;
  }

  public void setId(Long id) {
    this.id = id;
  }

  private String getGeoserverUrl(T node) {
    if (Region.class.isAssignableFrom(node.getClass())) {
      Region r = (Region) node;
      return mapUtil.getWMSGoogleMapUrl(resourceId, null, r);
    } else if (Taxon.class.isAssignableFrom(node.getClass())) {
      Taxon t = (Taxon) node;
      return mapUtil.getWMSGoogleMapUrl(resourceId, t, null);
    }
    return "";
  }

}