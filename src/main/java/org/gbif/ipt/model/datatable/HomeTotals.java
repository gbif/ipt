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
package org.gbif.ipt.model.datatable;

import lombok.Value;

/**
 * The totals of all public resources, for the line at the top of the home page (portal/home.ftl).
 */
@Value
public class HomeTotals {

  // public resources
  int resources;
  // records in their last published versions
  long records;
  // the ones registered with GBIF
  int onGbif;
}
