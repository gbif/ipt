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
package org.gbif.provider.model;

import java.util.Queue;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 * @param <E>
 */
public interface TreeNode<T extends TreeNode, E extends Enum> extends
    ResourceRelatedObject {
  String getLabel();

  Long getLft();

  String getMpath();

  T getParent();

  /**
   * Retrieves the list of all parental nodes from the top root node down to the
   * direct parent.
   * 
   * @return list of all parental nodes from the top root node down to the
   *         immediate parent nodes. First list element is the root node.
   */
  Queue<T> getParents();

  Long getRgt();

  E getType();

  Boolean isLeafNode();

  void setLabel(String label);

  void setLft(Long lft);

  void setMpath(String mpath);

  void setParent(T parent);

  void setRgt(Long rgt);

  void setType(E t);
}
