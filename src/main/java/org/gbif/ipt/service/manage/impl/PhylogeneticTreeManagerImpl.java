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
package org.gbif.ipt.service.manage.impl;

import org.gbif.dwc.Archive;
import org.gbif.dwc.ArchiveFile;
import org.gbif.dwc.DwcFiles;
import org.gbif.dwc.record.Record;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.PhylogeneticTreeFile;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.InvalidFilenameException;
import org.gbif.ipt.service.manage.PhylogeneticTreeManager;
import org.gbif.utils.file.ClosableIterator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.inject.Inject;

public class PhylogeneticTreeManagerImpl implements PhylogeneticTreeManager {

  private static final Logger LOG = LogManager.getLogger(PhylogeneticTreeManagerImpl.class);

  // Newick/NHX, Nexus and phyloXML, the formats Archaeopteryx.js reads
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
    "nwk", "newick", "nhx", "tre", "tree", "trees", "treefile", "nex", "nexus", "nxs", "xml", "phyloxml");
  // file names that are already used at the root of a DwC-A
  private static final Set<String> RESERVED_NAMES = Set.of(DataDir.EML_XML_FILENAME, Archive.META_FN);
  // the name ends up verbatim in phylogeneticTreeFileName values, so keep it simple
  private static final Pattern FILE_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,199}");

  // core terms copied into the tip metadata table, when present in the core
  private static final List<Term> CORE_TERMS = List.of(
    DwcTerm.occurrenceID, DwcTerm.catalogNumber, DwcTerm.scientificName, DwcTerm.institutionCode,
    DwcTerm.recordedBy, DwcTerm.country, DwcTerm.eventDate);
  private static final String LINKED_COLUMN = "Linked specimen";
  private static final String VALUE_SEPARATOR = " | ";

  private final DataDir dataDir;

  @Inject
  public PhylogeneticTreeManagerImpl(DataDir dataDir) {
    this.dataDir = dataDir;
  }

  @Override
  public boolean isAllowedFileName(String fileName) {
    return fileName != null
      && FILE_NAME.matcher(fileName).matches()
      && ALLOWED_EXTENSIONS.contains(FilenameUtils.getExtension(fileName).toLowerCase(Locale.ROOT))
      && !RESERVED_NAMES.contains(fileName.toLowerCase(Locale.ROOT));
  }

  @Override
  public List<PhylogeneticTreeFile> list(Resource resource) {
    File[] files = dataDir.resourcePhylogeneticTreesDir(resource.getShortname()).listFiles();
    if (files == null) {
      return new ArrayList<>();
    }
    return Arrays.stream(files)
      .filter(f -> f.isFile() && isAllowedFileName(f.getName()))
      .sorted(Comparator.comparing(File::getName))
      .map(PhylogeneticTreeManagerImpl::toTreeFile)
      .toList();
  }

  @Nullable
  @Override
  public File get(Resource resource, String fileName) {
    if (!isAllowedFileName(fileName)) {
      return null;
    }
    File file = new File(dataDir.resourcePhylogeneticTreesDir(resource.getShortname()), fileName);
    return file.isFile() ? file : null;
  }

  @Override
  public PhylogeneticTreeFile add(Resource resource, File uploadedFile, String fileName)
    throws InvalidFilenameException, IOException {
    if (!isAllowedFileName(fileName)) {
      throw new InvalidFilenameException("Not an allowed phylogenetic tree file name: " + fileName);
    }
    File target = new File(dataDir.resourcePhylogeneticTreesDir(resource.getShortname()), fileName);
    FileUtils.copyFile(uploadedFile, target);
    LOG.info("Added phylogenetic tree file {} to resource {}", fileName, resource.getShortname());
    return toTreeFile(target);
  }

  @Override
  public boolean delete(Resource resource, String fileName) {
    File file = get(resource, fileName);
    if (file == null) {
      return false;
    }
    boolean deleted = FileUtils.deleteQuietly(file);
    if (deleted) {
      LOG.info("Deleted phylogenetic tree file {} from resource {}", fileName, resource.getShortname());
    }
    return deleted;
  }

  @Override
  public List<String> listPublished(Resource resource, BigDecimal version) {
    List<String> names = new ArrayList<>();
    File dwca = publishedDwca(resource, version);
    if (dwca == null) {
      return names;
    }
    try (ZipFile zip = new ZipFile(dwca)) {
      zip.stream()
        .map(ZipEntry::getName)
        .filter(this::isAllowedFileName)
        .sorted()
        .forEach(names::add);
    } catch (IOException e) {
      LOG.error("Failed to list phylogenetic trees of resource {} version {}", resource.getShortname(), version, e);
    }
    return names;
  }

  @Nullable
  @Override
  public byte[] readPublished(Resource resource, BigDecimal version, String fileName) throws IOException {
    File dwca = publishedDwca(resource, version);
    if (dwca == null || !isAllowedFileName(fileName)) {
      return null;
    }
    try (ZipFile zip = new ZipFile(dwca)) {
      ZipEntry entry = zip.getEntry(fileName);
      if (entry == null) {
        return null;
      }
      try (InputStream in = zip.getInputStream(entry)) {
        return in.readAllBytes();
      }
    }
  }

  @Nullable
  @Override
  public String tipMetadataTable(Resource resource, BigDecimal version, String fileName) throws IOException {
    File dwca = publishedDwca(resource, version);
    if (dwca == null || !isAllowedFileName(fileName)) {
      return null;
    }

    File tmpDir = dataDir.tmpDir();
    try {
      return buildTipMetadataTable(DwcFiles.fromCompressed(dwca.toPath(), tmpDir.toPath()), fileName);
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException("Failed to read published DwC-A " + dwca.getName(), e);
    } finally {
      FileUtils.deleteQuietly(tmpDir);
    }
  }

  @Nullable
  private String buildTipMetadataTable(Archive archive, String fileName) throws Exception {
    ArchiveFile citations = archive.getExtensions().stream()
      .filter(ext -> MATERIAL_CITATION_ROW_TYPE.equals(ext.getRowType().qualifiedName()))
      .findFirst()
      .orElse(null);
    if (citations == null) {
      return null;
    }

    Term tipLabelTerm = findTerm(citations, TIP_LABEL_TERM);
    Term fileNameTerm = findTerm(citations, FILE_NAME_TERM);
    Term treeNameTerm = findTerm(citations, TREE_NAME_TERM);
    if (tipLabelTerm == null || fileNameTerm == null) {
      return null;
    }

    // core id -> citations (tip label, tree name) of the requested tree file
    Map<String, List<String[]>> citationsByCoreId = new HashMap<>();
    try (ClosableIterator<Record> it = citations.iterator()) {
      while (it.hasNext()) {
        Record rec = it.next();
        String tip = StringUtils.trimToNull(rec.value(tipLabelTerm));
        if (tip != null && fileName.equals(StringUtils.trim(rec.value(fileNameTerm)))) {
          String treeName = treeNameTerm == null ? null : StringUtils.trimToNull(rec.value(treeNameTerm));
          citationsByCoreId.computeIfAbsent(rec.id(), k -> new ArrayList<>()).add(new String[] {tip, treeName});
        }
      }
    }
    if (citationsByCoreId.isEmpty()) {
      return null;
    }

    List<Term> coreTerms = CORE_TERMS.stream().filter(archive.getCore()::hasTerm).toList();
    boolean withTreeName = citationsByCoreId.values().stream()
      .flatMap(List::stream)
      .anyMatch(c -> c[1] != null);

    // tip label -> column -> distinct values; a tip can cite several specimens
    Map<String, Map<String, Set<String>>> rows = new LinkedHashMap<>();
    try (ClosableIterator<Record> it = archive.getCore().iterator()) {
      while (it.hasNext()) {
        Record core = it.next();
        List<String[]> tips = citationsByCoreId.remove(core.id());
        if (tips == null) {
          continue;
        }
        for (String[] tip : tips) {
          Map<String, Set<String>> row = rows.computeIfAbsent(tip[0], k -> new LinkedHashMap<>());
          addValue(row, LINKED_COLUMN, "yes");
          if (withTreeName) {
            addValue(row, TREE_NAME_TERM, tip[1]);
          }
          for (Term term : coreTerms) {
            addValue(row, term.simpleName(), core.value(term));
          }
        }
      }
    }
    // citations whose core record is missing still mark the tip as linked
    citationsByCoreId.values().stream().flatMap(List::stream)
      .forEach(tip -> addValue(rows.computeIfAbsent(tip[0], k -> new LinkedHashMap<>()), LINKED_COLUMN, "yes"));

    List<String> columns = new ArrayList<>();
    columns.add(LINKED_COLUMN);
    if (withTreeName) {
      columns.add(TREE_NAME_TERM);
    }
    coreTerms.forEach(t -> columns.add(t.simpleName()));

    StringBuilder sb = new StringBuilder("tip");
    columns.forEach(c -> sb.append('\t').append(c.equals(TREE_NAME_TERM) ? "phylogeneticTreeName" : c));
    sb.append('\n');
    rows.forEach((tip, row) -> {
      sb.append(tsv(tip));
      for (String column : columns) {
        sb.append('\t').append(tsv(String.join(VALUE_SEPARATOR, row.getOrDefault(column, Set.of()))));
      }
      sb.append('\n');
    });
    return sb.toString();
  }

  @Nullable
  private File publishedDwca(Resource resource, BigDecimal version) {
    if (resource == null || version == null || resource.isDataPackage()) {
      return null;
    }
    File dwca = dataDir.resourceDwcaFile(resource.getShortname(), version);
    return dwca.isFile() ? dwca : null;
  }

  @Nullable
  private static Term findTerm(ArchiveFile archiveFile, String qualifiedName) {
    return archiveFile.getTerms().stream()
      .filter(t -> qualifiedName.equals(t.qualifiedName()))
      .findFirst()
      .orElse(null);
  }

  private static void addValue(Map<String, Set<String>> row, String column, String value) {
    if (StringUtils.isNotBlank(value)) {
      row.computeIfAbsent(column, k -> new LinkedHashSet<>()).add(value.trim());
    }
  }

  private static String tsv(String value) {
    return value.replaceAll("[\\t\\r\\n]+", " ");
  }

  private static PhylogeneticTreeFile toTreeFile(File file) {
    return new PhylogeneticTreeFile(file.getName(), file.length(), new Date(file.lastModified()));
  }
}
