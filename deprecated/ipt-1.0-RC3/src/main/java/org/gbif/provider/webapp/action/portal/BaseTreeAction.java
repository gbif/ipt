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

import org.gbif.provider.model.TreeNode;
import org.gbif.provider.service.TreeNodeManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 * @param <E>
 */
public class BaseTreeAction<T extends TreeNode<T, E>, E extends Enum> extends
    BaseDataResourceAction {
  protected TreeNodeManager<T, E> treeNodeManager;
  protected Long id;
  protected Long focus;
  protected String parents = "";
  protected List<T> nodes;
  protected String treeType;

  public BaseTreeAction(TreeNodeManager<T, E> treeNodeManager, String treeType) {
    super();
    this.treeNodeManager = treeNodeManager;
    this.treeType = treeType;
  }

  @Override
  public String execute() {
    if (id != null) {
      if (focus == null) {
        // initial tree request with selected tree node
        // return entire tree up to the id.
        // To do this first find all parent nodes.
        // Rendering of nodes will do a recursion depending on parent string
        focus = id;
        parents = StringUtils.join(
            treeNodeManager.getParentIds(resourceId, id), ".");
      } else {
        // parents already set. this is a recursive call already
        return subNodes();
      }
    }
    return rootNodes();
  }

  public Long getFocus() {
    return focus;
  }

  public Long getId() {
    return id;
  }

  public List<T> getNodes() {
    return nodes;
  }

  public String getParents() {
    return parents;
  }

  public String getTreeType() {
    return treeType;
  }

  public void setFocus(Long focus) {
    this.focus = focus;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setParents(String parents) {
    this.parents = parents;
  }

  public String subNodes() {
    nodes = treeNodeManager.getChildren(resourceId, id);
    return SUCCESS;
  }

  protected String rootNodes() {
    nodes = treeNodeManager.getRoots(resourceId);
    return SUCCESS;
  }
}