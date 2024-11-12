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

import org.gbif.ipt.model.voc.PublicationStatus;

import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Simplified IPT resource view for admin page.
 * A resource can be identified by its shortname, which has to be unique within an IPT instance.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class AdminTableViewResource {

  private String shortname;
  private String coreType;
  private Date lastModified;
  private PublicationStatus publicationStatus;
  private String creatorName;
  private boolean failed;

  // TODO: enum or constants
  public String getLoadStatus() {
    return failed ? "failed" : "loaded";
  }
}
