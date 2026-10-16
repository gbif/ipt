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

import org.gbif.ipt.service.InvalidPhylogeneticTreeException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhylogeneticTreeValidatorTest {

  private static final String NEXUS = """
    #NEXUS
    begin taxa;
      dimensions ntax=3;
      taxlabels Fabales_Fabaceae_A B C;
    end;
    begin trees;
      translate 1 Fabales_Fabaceae_A, 2 B, 3 C;
      tree t1 = [&R] (1:0.1,(2:1.884795486e-05,3:0.3):0.4);
      tree t2 = ((1,2),3);
    end;
    """;

  private static final String PHYLOXML = """
    <?xml version="1.0" encoding="UTF-8"?>
    <phyloxml xmlns="http://www.phyloxml.org">
      <phylogeny rooted="true">
        <clade>
          <clade><name>A</name><branch_length>0.1</branch_length></clade>
          <clade><name>B</name><branch_length>0.2</branch_length></clade>
        </clade>
      </phylogeny>
    </phyloxml>
    """;

  @TempDir
  Path tmp;

  private final PhylogeneticTreeValidator validator = new PhylogeneticTreeValidator();

  @ParameterizedTest
  @ValueSource(strings = {
    "(A,B);",
    "(Fabales_Fabaceae_A:0.1,(B:1.884795486e-05,C:0.3):0.4);",
    "(A:0.1,B:0.2)[&&NHX:S=x];\n(A,(B,C));",
    // negative branch lengths come out of neighbour joining
    "(A:-0.1,B:0.2);"
  })
  public void testValidNewick(String tree) throws Exception {
    assertDoesNotThrow(() -> validator.validate(write("tree.nwk", tree)));
  }

  @Test
  public void testReservedFileNames() {
    assertTrue(PhylogeneticTreeValidator.isReservedFileName("eml.xml"));
    assertTrue(PhylogeneticTreeValidator.isReservedFileName("META.xml"));
    assertFalse(PhylogeneticTreeValidator.isReservedFileName("tree.xml"));
    assertFalse(PhylogeneticTreeValidator.isReservedFileName(null));
  }

  @Test
  public void testValidNexus() throws Exception {
    assertDoesNotThrow(() -> validator.validate(write("tree.nex", NEXUS)));
  }

  @Test
  public void testValidPhyloXml() throws Exception {
    assertDoesNotThrow(() -> validator.validate(write("tree.xml", PHYLOXML)));
  }

  @Test
  public void testInvalidBranchLength() throws Exception {
    // a renaming script replaced "-" with "_" everywhere, including in exponents
    assertInvalid("(A:0.1,B:1.884795486e_05);", "failed to parse number from \"1.884795486e_05\"");
    assertInvalid(NEXUS.replace("e-05", "e_05"), "failed to parse number from \"1.884795486e_05\"");
    assertInvalid("(A:Infinity,B:0.2);", "invalid branch length");
    assertInvalid("(A:NaN,B:0.2);", "invalid branch length");
  }

  @Test
  public void testInvalidStructure() throws Exception {
    assertInvalid("", "the file is empty");
    assertInvalid("  \n\t\n", "the file is empty");
    assertInvalid("((A,B);", "number of open parens does not equal number of close parens");
    assertInvalid("hello world", "fewer than two tips");
    assertInvalid("(A,(B,A));", "duplicate tip label \"A\"");
    assertInvalid(PHYLOXML.replace("<branch_length>0.1", "<branch_length>x"), "branch_length");
    assertInvalid(PHYLOXML.replace("</phyloxml>", ""), "not well-formed XML");
  }

  @Test
  public void testMessagesWithoutServerPath() throws Exception {
    // uploads are validated in a server temp file, its path means nothing to users
    File file = write("upload_123.tmp", PHYLOXML.replace("<branch_length>0.1", "<branch_length>x"));
    InvalidPhylogeneticTreeException e = assertThrows(InvalidPhylogeneticTreeException.class,
      () -> validator.validate(file));
    assertFalse(e.getMessage().contains(tmp.toString()), e.getMessage());
    assertTrue(e.getMessage().startsWith("attempt to parse [x] into double"), e.getMessage());
  }

  @Test
  public void testPhyloXmlWithDoctypeRejected() throws Exception {
    File secret = write("secret.txt", "SECRET");
    String xxe = PHYLOXML.replace("<phyloxml ",
        "<!DOCTYPE phyloxml [<!ENTITY x SYSTEM \"" + secret.toURI() + "\">]>\n<phyloxml ")
      .replace("<name>A</name>", "<name>&x;</name>");

    InvalidPhylogeneticTreeException e = assertThrows(InvalidPhylogeneticTreeException.class,
      () -> validator.validate(write("tree.xml", xxe)));
    assertEquals("XML files with a DOCTYPE declaration are not supported", e.getMessage());
  }

  private void assertInvalid(String content, String expectedMessage) throws IOException {
    File file = write("tree.tmp", content);
    InvalidPhylogeneticTreeException e = assertThrows(InvalidPhylogeneticTreeException.class,
      () -> validator.validate(file));
    assertTrue(e.getMessage().contains(expectedMessage), e.getMessage());
  }

  private File write(String name, String content) throws IOException {
    Path path = tmp.resolve(name);
    Files.writeString(path, content);
    return path.toFile();
  }
}
