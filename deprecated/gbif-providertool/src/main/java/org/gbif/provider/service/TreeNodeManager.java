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

import org.gbif.provider.model.TreeNode;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 * @param <E>
 */
public interface TreeNodeManager<T extends TreeNode<T, E>, E extends Enum>
    extends GenericResourceRelatedManager<T> {
  void buildNestedSet(Long resourceId);

  int countByType(Long resourceId, E type);

  int countTerminalNodes(Long resourceId);

  int countTreeNodes(Long resourceId);

  T getByMaterializedPath(Long resourceId, String mpath);

  List<T> getChildren(Long resourceId, Long parentId);

  List<Long> getParentIds(Long resourceId, Long nodeId);

  List<T> getParents(Long resourceId, Long nodeId);

  List<T> getRoots(Long resourceId);
}
