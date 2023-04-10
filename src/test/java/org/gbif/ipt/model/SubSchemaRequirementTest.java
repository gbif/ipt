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

import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubSchemaRequirementTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final File requirementsJson = FileUtils.getClasspathFile("schemas/requirements.json");

  @Test
  public void testAtLeastOneRequiredSchemaPresent() throws Exception {
    SubSchemaRequirement subSchemaRequirement = objectMapper.readValue(requirementsJson, SubSchemaRequirement.class);
    Set<String> schemas = new HashSet<>(Arrays.asList("bla", "schema", "random"));
    assertFalse(subSchemaRequirement.validate(schemas).isValid());
  }

  @ParameterizedTest
  @MethodSource("validRequiredSchemasCombinations")
  public void testRequiredSchemas(Set<String> schemas) throws Exception {
    SubSchemaRequirement subSchemaRequirement = objectMapper.readValue(requirementsJson, SubSchemaRequirement.class);
    assertTrue(subSchemaRequirement.validate(schemas).isValid());
  }

  @ParameterizedTest
  @MethodSource("invalidCombinationsWithNameUsage")
  public void testNameUsageExcludesNameTaxonSynonym(Set<String> schemas) throws Exception {
    SubSchemaRequirement subSchemaRequirement = objectMapper.readValue(requirementsJson, SubSchemaRequirement.class);
    assertFalse(subSchemaRequirement.validate(schemas).isValid());
  }

  @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
  static Stream<Arguments> validRequiredSchemasCombinations() {
    return Stream.of(
      Arguments.of(new HashSet<>(Arrays.asList("name"))),
      Arguments.of(new HashSet<>(Arrays.asList("taxon"))),
      Arguments.of(new HashSet<>(Arrays.asList("reference"))),
      Arguments.of(new HashSet<>(Arrays.asList("name-usage"))),
      Arguments.of(new HashSet<>(Arrays.asList("name", "taxon"))),
      Arguments.of(new HashSet<>(Arrays.asList("taxon", "reference"))),
      Arguments.of(new HashSet<>(Arrays.asList("name", "reference"))),
      Arguments.of(new HashSet<>(Arrays.asList("name", "taxon", "reference")))
    );
  }

  static Stream<Arguments> invalidCombinationsWithNameUsage() {
    return Stream.of(
      Arguments.of(new HashSet<>(Arrays.asList("name-usage", "name"))),
      Arguments.of(new HashSet<>(Arrays.asList("name-usage", "taxon"))),
      Arguments.of(new HashSet<>(Arrays.asList("name-usage", "synonym"))),
      Arguments.of(new HashSet<>(Arrays.asList("name-usage", "name", "taxon"))),
      Arguments.of(new HashSet<>(Arrays.asList("name-usage", "name", "taxon", "synonym")))
    );
  }
}
