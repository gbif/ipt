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

import java.util.Date;

import org.apache.commons.io.FileUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A phylogenetic tree file (Newick, Nexus or phyloXML) attached to a resource. Tree files are shipped at the root of
 * the archive, so the file name is what the Phylogenetic Material Citation extension's phylogeneticTreeFileName
 * refers to.
 */
@Getter
@AllArgsConstructor
public class PhylogeneticTreeFile {

  private final String name;
  private final long size;
  private final Date lastModified;

  public String getFormattedSize() {
    return FileUtils.byteCountToDisplaySize(size);
  }
}
