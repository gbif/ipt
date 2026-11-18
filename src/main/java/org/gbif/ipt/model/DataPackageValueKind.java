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
package org.gbif.ipt.model;

/**
 * Where a generated field value comes from. A field mapping without one takes its column or fixed value, as before.
 */
public enum DataPackageValueKind {
  /** the list item, in a mapping with one row per list item */
  ITEM,
  /** the item's position in its list, 1 for the first */
  POSITION,
  /** an identifier made from a value: the column's when the field mapping has one, otherwise the list item */
  ID,
  /** a foreign key: the same rule as the primary key of the table it links to, applied to the same source row */
  LINK,
  /** a template with {column name}, {item} and {position} placeholders */
  TEMPLATE,
  /**
   * the item at the same place in another list: for an item of the k-th list column, the item at the same position in
   * the field mapping's k-th pair column, e.g. a person's ID in recordedByID for their name in recordedBy
   */
  PAIRED
}
