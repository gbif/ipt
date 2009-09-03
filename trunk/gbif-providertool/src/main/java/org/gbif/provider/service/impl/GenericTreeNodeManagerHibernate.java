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
package org.gbif.provider.service.impl;

import org.gbif.provider.model.Resource;
import org.gbif.provider.model.TreeNode;
import org.gbif.provider.service.TreeNodeManager;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 * @param <E>
 */
public class GenericTreeNodeManagerHibernate<T extends TreeNode<T, E>, E extends Enum>
    extends GenericResourceRelatedManagerHibernate<T> implements
    TreeNodeManager<T, E> {
  private final TreeNodeSupportHibernate<T, E> treeNodeSupport;

  public GenericTreeNodeManagerHibernate(final Class<T> persistentClass) {
    super(persistentClass);
    treeNodeSupport = new TreeNodeSupportHibernate<T, E>(persistentClass, this);
  }

  @Transactional(readOnly = false)
  public void buildNestedSet(Long resourceId) {
    treeNodeSupport.buildNestedSet(resourceId, getSession());
  }

  public int countByType(Long resourceId, E type) {
    return treeNodeSupport.countByType(resourceId, type, getSession());
  }

  public int countTerminalNodes(Long resourceId) {
    return treeNodeSupport.countTerminalNodes(resourceId, getSession(), null);
  }

  public int countTreeNodes(Long resourceId) {
    return treeNodeSupport.countTreeNodes(resourceId, getSession());
  }

  public T getByMaterializedPath(Long resourceId, String mpath) {
    return treeNodeSupport.getByMaterializedPath(resourceId, mpath,
        getSession());
  }

  public List<T> getChildren(Long resourceId, Long parentId) {
    return treeNodeSupport.getChildren(resourceId, parentId, getSession());
  }

  public List<Long> getParentIds(Long resourceId, Long nodeId) {
    return treeNodeSupport.getParentIds(resourceId, nodeId, getSession());
  }

  public List<T> getParents(Long resourceId, Long nodeId) {
    return treeNodeSupport.getParents(resourceId, nodeId, getSession());
  }

  public List<T> getRoots(Long resourceId) {
    return treeNodeSupport.getRoots(resourceId, getSession(), null);
  }

  @Override
  @Transactional(readOnly = false)
  public int removeAll(Resource resource) {
    return treeNodeSupport.removeAll(resource, getSession());
  }

}
