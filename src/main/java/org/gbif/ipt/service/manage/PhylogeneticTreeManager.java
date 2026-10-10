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
package org.gbif.ipt.service.manage;

import org.gbif.ipt.model.PhylogeneticTreeFile;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.InvalidFilenameException;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Manages phylogenetic tree files attached to a resource: the working copies uploaded by managers, and the copies
 * shipped inside published DwC-As.
 */
public interface PhylogeneticTreeManager {

  String MATERIAL_CITATION_ROW_TYPE = "http://rs.gbif.org/terms/1.0/PhylogeneticMaterialCitation";
  String TIP_LABEL_TERM = "http://rs.gbif.org/terms/1.0/phylogeneticTreeTipLabel";
  String FILE_NAME_TERM = "http://rs.gbif.org/terms/1.0/phylogeneticTreeFileName";
  String TREE_NAME_TERM = "http://rs.gbif.org/terms/1.0/phylogeneticTreeName";

  /**
   * @return the working tree files of the resource, sorted by name
   */
  List<PhylogeneticTreeFile> list(Resource resource);

  /**
   * @return the working tree file, or null if the name is not a valid tree file name or the file doesn't exist
   */
  @Nullable
  File get(Resource resource, String fileName);

  /**
   * Stores an uploaded tree file, replacing any existing file with the same name.
   *
   * @throws InvalidFilenameException if the file name or extension is not allowed
   */
  PhylogeneticTreeFile add(Resource resource, File uploadedFile, String fileName)
    throws InvalidFilenameException, IOException;

  /**
   * @return true if the tree file existed and was deleted
   */
  boolean delete(Resource resource, String fileName);

  /**
   * @return names of the tree files in a published DwC-A version, empty if none or the version doesn't exist
   */
  List<String> listPublished(Resource resource, BigDecimal version);

  /**
   * @return content of a tree file in a published DwC-A version, or null if it doesn't exist
   */
  @Nullable
  byte[] readPublished(Resource resource, BigDecimal version, String fileName) throws IOException;

  /**
   * Builds a tab-separated table keyed by tip label, joining the Phylogenetic Material Citation records of a published
   * DwC-A version that cite the given tree file to their core records. The first column is the tip label, as
   * expected by Archaeopteryx.js metadata tables.
   *
   * @return the table, or null if the version doesn't have the extension or no records cite the tree file
   */
  @Nullable
  String tipMetadataTable(Resource resource, BigDecimal version, String fileName) throws IOException;

  /**
   * @return true if the file name is a safe, allowed tree file name
   */
  boolean isAllowedFileName(String fileName);
}
