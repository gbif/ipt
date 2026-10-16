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
package org.gbif.ipt.validation;

import org.gbif.dwc.Archive;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.service.InvalidPhylogeneticTreeException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.forester.io.parsers.PhylogenyParser;
import org.forester.io.parsers.nexus.NexusPhylogeniesParser;
import org.forester.io.parsers.nhx.NHXParser;
import org.forester.io.parsers.phyloxml.PhyloXmlParser;
import org.forester.io.parsers.util.ParserUtils;
import org.forester.phylogeny.Phylogeny;
import org.forester.phylogeny.PhylogenyNode;
import org.forester.phylogeny.data.PhylogenyDataUtil;
import org.forester.phylogeny.iterators.PhylogenyNodeIterator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Checks phylogenetic tree files (Newick/NHX, Nexus or phyloXML): that the file name is an allowed tree file name, and
 * that the content can be read by forester, the library behind Archaeopteryx, into usable trees: every tree has at
 * least two tips, unique tip labels and finite branch lengths.
 */
public class PhylogeneticTreeValidator {

  private static final Logger LOG = LogManager.getLogger(PhylogeneticTreeValidator.class);
  private static final String UNREADABLE = "the file could not be read as a Newick, Nexus or phyloXML tree";

  // Newick/NHX, Nexus and phyloXML, the formats Archaeopteryx.js reads
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
    "nwk", "newick", "nhx", "tre", "tree", "trees", "treefile", "nex", "nexus", "nxs", "xml", "phyloxml");
  // file names that are already used at the root of a DwC-A
  private static final Set<String> RESERVED_NAMES = Set.of(DataDir.EML_XML_FILENAME, Archive.META_FN);
  // same rule as for source files; no path separators, and "." or ".." fail the extension check
  private static final Pattern FILE_NAME = Pattern.compile(Constants.ACCEPTED_FILE_NAMES);

  /**
   * @return true if the file name is a safe tree file name with a tree file extension, not clashing with the files
   * at the root of a DwC-A. Anything else in a resource's tree folder (e.g. .DS_Store) isn't a tree file.
   */
  public static boolean isAllowedFileName(String fileName) {
    return fileName != null
      && FILE_NAME.matcher(fileName).matches()
      && hasOnlyInnerSpaces(fileName)
      && ALLOWED_EXTENSIONS.contains(FilenameUtils.getExtension(fileName).toLowerCase(Locale.ROOT))
      && !isReservedFileName(fileName);
  }

  /**
   * @return true if the name is one of the files at the root of a DwC-A, which a tree file can't replace
   */
  public static boolean isReservedFileName(String fileName) {
    return fileName != null && RESERVED_NAMES.contains(fileName.toLowerCase(Locale.ROOT));
  }

  /**
   * Leading or trailing spaces make a name that differs from its trimmed citations: tips would never be linked in the
   * viewer, which compares trimmed phylogeneticTreeFileName values. Other whitespace that the shared file name pattern
   * lets in (tabs, line breaks) has no place in a file name.
   */
  private static boolean hasOnlyInnerSpaces(String fileName) {
    return !fileName.startsWith(" ")
      && !fileName.endsWith(" ")
      && fileName.chars().noneMatch(c -> Character.isWhitespace(c) && c != ' ');
  }

  /**
   * @throws InvalidPhylogeneticTreeException if the file can't be parsed or holds an unusable tree
   * @throws IOException if the file can't be read
   */
  public void validate(File file) throws InvalidPhylogeneticTreeException, IOException {
    if (isBlank(file)) {
      throw new InvalidPhylogeneticTreeException("the file is empty");
    }

    Phylogeny[] trees;
    try {
      PhylogenyParser parser = ParserUtils.createParserDependingFileContents(file, false);
      configure(parser, file);
      trees = parser.parse();
    } catch (InvalidPhylogeneticTreeException e) {
      throw e;
    } catch (RuntimeException e) {
      // forester bugs on some inputs, their messages mean nothing to users
      LOG.debug("forester failed to parse {}", file, e);
      throw new InvalidPhylogeneticTreeException(UNREADABLE);
    } catch (Exception e) {
      // forester reports malformed data as IOExceptions
      throw new InvalidPhylogeneticTreeException(withoutPath(StringUtils.defaultIfBlank(e.getMessage(), UNREADABLE),
        file));
    }

    if (trees == null || trees.length == 0) {
      throw new InvalidPhylogeneticTreeException("no trees found");
    }
    for (int i = 0; i < trees.length; i++) {
      validate(trees[i], i + 1);
    }
  }

  private static void configure(PhylogenyParser parser, File file) throws Exception {
    // keep labels as written, they have to match phylogeneticTreeTipLabel values
    if (parser instanceof NHXParser nhx) {
      nhx.setReplaceUnderscores(false);
      nhx.setTaxonomyExtraction(NHXParser.TAXONOMY_EXTRACTION.NO);
      nhx.setSource(file);
    } else if (parser instanceof NexusPhylogeniesParser nexus) {
      nexus.setReplaceUnderscores(false);
      nexus.setTaxonomyExtraction(NHXParser.TAXONOMY_EXTRACTION.NO);
      nexus.setSource(file);
    } else if (parser instanceof PhyloXmlParser phyloXml) {
      rejectDoctype(file);
      phyloXml.setSource(file);
    } else {
      throw new InvalidPhylogeneticTreeException(UNREADABLE);
    }
  }

  /**
   * forester's phyloXML parser resolves external entities, so XML declaring a DTD must not reach it. phyloXML doesn't
   * use DTDs.
   */
  private static void rejectDoctype(File file) throws Exception {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    SAXParser saxParser = factory.newSAXParser();
    saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", new DefaultHandler2() {
      @Override
      public void startDTD(String name, String publicId, String systemId) throws SAXException {
        throw new DoctypeFound();
      }
    });
    try {
      saxParser.parse(file, new DefaultHandler());
    } catch (DoctypeFound e) {
      throw new InvalidPhylogeneticTreeException("XML files with a DOCTYPE declaration are not supported");
    } catch (SAXParseException e) {
      throw new InvalidPhylogeneticTreeException(
        "not well-formed XML (line " + e.getLineNumber() + "): " + withoutPath(e.getMessage(), file));
    }
  }

  /**
   * @return true if the file has nothing but whitespace, reading only up to the first other character
   */
  private static boolean isBlank(File file) throws IOException {
    try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
      int b;
      while ((b = in.read()) != -1) {
        if (!Character.isWhitespace(b)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * forester puts the path of the parsed file in some messages: for uploads a server temp file, meaningless to users.
   */
  private static String withoutPath(String message, File file) {
    String result = message;
    for (String path : new String[] {file.getAbsolutePath(), file.getPath(), file.toURI().toString()}) {
      result = result.replace("failed to parse [" + path + "]: ", "").replace(path, file.getName());
    }
    return result;
  }

  private static void validate(Phylogeny tree, int position) throws InvalidPhylogeneticTreeException {
    String treeName = StringUtils.isBlank(tree.getName())
      ? "tree " + position
      : "tree " + position + " (" + tree.getName() + ")";
    if (tree.isEmpty() || tree.getNumberOfExternalNodes() < 2) {
      throw new InvalidPhylogeneticTreeException(treeName + " has fewer than two tips");
    }

    Set<String> tipLabels = new HashSet<>();
    for (PhylogenyNodeIterator it = tree.iteratorPreorder(); it.hasNext(); ) {
      PhylogenyNode node = it.next();
      double branchLength = node.getDistanceToParent();
      // forester uses a sentinel for nodes without a branch length
      if (branchLength != PhylogenyDataUtil.BRANCH_LENGTH_DEFAULT && !Double.isFinite(branchLength)) {
        throw new InvalidPhylogeneticTreeException(
          treeName + " has an invalid branch length " + branchLength + " for node " + describe(node));
      }
      if (node.isExternal() && StringUtils.isNotEmpty(node.getName()) && !tipLabels.add(node.getName())) {
        throw new InvalidPhylogeneticTreeException(treeName + " has a duplicate tip label \"" + node.getName() + "\"");
      }
    }
  }

  private static String describe(PhylogenyNode node) {
    return StringUtils.isEmpty(node.getName()) ? "#" + node.getId() : "\"" + node.getName() + "\"";
  }

  /**
   * Stops the DOCTYPE check at the declaration.
   */
  private static final class DoctypeFound extends SAXException {
  }
}
