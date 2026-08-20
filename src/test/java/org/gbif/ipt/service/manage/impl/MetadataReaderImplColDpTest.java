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

import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.service.manage.JsonService;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.YamlService;
import org.gbif.ipt.utils.MetadataUtils;
import org.gbif.utils.file.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetadataReaderImplColDpTest {

  private final JsonService jsonService = new JsonServiceImpl();
  private final YamlService yamlService = new YamlServiceImpl();
  private final MetadataReader reader = new MetadataReaderImpl(jsonService, yamlService);

  @Test
  void testColDpSerDeRoundtrip(@TempDir Path tempDir) throws Exception {
    File datapackageJson = FileUtils.getClasspathFile("frictionless/coldp/metadata.yaml");
    DataPackageMetadata metadata = reader.readValue(datapackageJson, MetadataUtils.metadataClassForType("coldp"));

    assertNotNull(metadata);
    assertInstanceOf(ColMetadata.class, metadata);

    ColMetadata cm = (ColMetadata) metadata;
    verifyColDpMetadata(cm);

    Path roundTripFile = tempDir.resolve("datapackage-roundtrip.json");
    reader.writeValue(roundTripFile.toFile(), cm);

    ColMetadata roundtrip = reader.readValue(roundTripFile.toFile(), ColMetadata.class);
    verifyColDpMetadata(roundtrip);
  }

  private static void verifyColDpMetadata(ColMetadata cm) {
    assertEquals("10.15468/2zjeva", cm.getDoi().toString());

    assertEquals("col:1010", cm.getIdentifier().get(0));
    assertEquals("gbif:b96ed603-b710-4b3a-b99b-9bf0de6ef85b", cm.getIdentifier().get(1));
    assertEquals("plazi:3378FFAB6B55FFE0B065C77BFF226429", cm.getIdentifier().get(2));

    assertEquals("ColDP Example. The full dataset title", cm.getTitle());
    assertEquals("ColDP Example", cm.getAlias());
    assertEquals(
        """
            An abstract about the content of this dataset.
            This can be as many paragraphs as you like in literal style YAML.   \s
            """,
        cm.getDescription()
    );

    assertEquals(
        List.of("example", "coldp", "invasives", "legal", "fish"),
        cm.getKeyword()
    );

    assertEquals("2018-06-01", cm.getIssued().toString());
    assertEquals("v.48 (06/2018)", cm.getVersion());
    assertEquals("2405-8858", cm.getIssn());

    // Contact
    assertNotNull(cm.getContact());
    assertEquals("Rainer", cm.getContact().getGiven());
    assertEquals("Froese", cm.getContact().getFamily());
    assertEquals("rainer@mailinator.com", cm.getContact().getEmail());

    // Creators
    assertNotNull(cm.getCreator());
    assertEquals(3, cm.getCreator().size());

    assertEquals("Nicolas", cm.getCreator().get(0).getGiven());
    assertEquals("Bailly", cm.getCreator().get(0).getFamily());
    assertEquals("0000-0003-4994-0653", cm.getCreator().get(0).getOrcid());

    assertEquals("Rainer", cm.getCreator().get(1).getGiven());
    assertEquals("Froese", cm.getCreator().get(1).getFamily());
    assertEquals("0000-0001-9745-636X", cm.getCreator().get(1).getOrcid());

    assertEquals("Daniel", cm.getCreator().get(2).getGiven());
    assertEquals("Pauly", cm.getCreator().get(2).getFamily());
    assertEquals("0000-0003-3756-4793", cm.getCreator().get(2).getOrcid());

    // Editors
    assertNotNull(cm.getEditor());
    assertEquals(2, cm.getEditor().size());

    assertEquals("Rainer", cm.getEditor().get(0).getGiven());
    assertEquals("Froese", cm.getEditor().get(0).getFamily());
    assertEquals("rainer@mailinator.com", cm.getEditor().get(0).getEmail());
    assertEquals("0000-0001-9745-636X", cm.getEditor().get(0).getOrcid());

    assertEquals("Daniel", cm.getEditor().get(1).getGiven());
    assertEquals("Pauly", cm.getEditor().get(1).getFamily());
    assertEquals("0000-0003-3756-4793", cm.getEditor().get(1).getOrcid());

    // Publisher
    assertNotNull(cm.getPublisher());
    assertEquals("03rmrcq20", cm.getPublisher().getRorid());
    assertEquals("University of British Columbia", cm.getPublisher().getOrganisation());
    assertEquals("Global Fisheries Cluster", cm.getPublisher().getDepartment());
    assertEquals("Vancouver", cm.getPublisher().getCity());
    assertEquals("B.C.", cm.getPublisher().getState());
    assertEquals("CA", cm.getPublisher().getCountry());

    // Contributors
    assertNotNull(cm.getContributor());
    assertEquals(8, cm.getContributor().size());

    assertEquals("Atheer", cm.getContributor().get(0).getGiven());
    assertEquals("Ali", cm.getContributor().get(0).getFamily());
    assertEquals("0000-0002-2541-968X", cm.getContributor().get(0).getOrcid());
    assertEquals("atheeralibu@gmail.com", cm.getContributor().get(0).getEmail());
    assertEquals(
        "Provided references on fishes of Iraq",
        cm.getContributor().get(0).getNote()
    );

    assertEquals("Richard Lawrence", cm.getContributor().get(1).getGiven());
    assertEquals("Pyle", cm.getContributor().get(1).getFamily());
    assertEquals("0000-0003-0768-1286", cm.getContributor().get(1).getOrcid());
    assertEquals("Bernice Pauahi Bishop Museum", cm.getContributor().get(1).getOrganisation());
    assertEquals("Natural Sciences", cm.getContributor().get(1).getDepartment());
    assertEquals("Honolulu", cm.getContributor().get(1).getCity());
    assertEquals("Hawaii", cm.getContributor().get(1).getState());
    assertEquals("US", cm.getContributor().get(1).getCountry());
    assertEquals(
        "Review of Pacific species",
        cm.getContributor().get(1).getNote()
    );

    assertEquals("Markus", cm.getContributor().get(2).getGiven());
    assertEquals("Döring", cm.getContributor().get(2).getFamily());
    assertEquals("0000-0001-7757-1889", cm.getContributor().get(2).getOrcid());
    assertEquals("IT support", cm.getContributor().get(2).getNote());

    assertEquals("The WorldFish Center", cm.getContributor().get(3).getOrganisation());
    assertEquals("Penang", cm.getContributor().get(3).getCity());
    assertEquals("MY", cm.getContributor().get(3).getCountry());

    assertEquals("02h2x0161", cm.getContributor().get(4).getRorid());
    assertEquals("Helmholtz Centre for Ocean Research Kiel",
        cm.getContributor().get(4).getOrganisation());
    assertEquals("GEOMAR", cm.getContributor().get(4).getDepartment());
    assertEquals("Kiel", cm.getContributor().get(4).getCity());
    assertEquals("DE", cm.getContributor().get(4).getCountry());
    assertEquals("Hosting services", cm.getContributor().get(4).getNote());

    assertEquals(
        "Food and Agriculture Organization of the United Nations",
        cm.getContributor().get(5).getOrganisation()
    );
    assertEquals("00pe0tf51", cm.getContributor().get(5).getRorid());
    assertEquals("Rome", cm.getContributor().get(5).getCity());
    assertEquals("IT", cm.getContributor().get(5).getCountry());

    assertEquals(
        "Muséum National d'Histoire Naturelle",
        cm.getContributor().get(6).getOrganisation()
    );
    assertEquals("03wkt5x30", cm.getContributor().get(6).getRorid());
    assertEquals("Paris", cm.getContributor().get(6).getCity());
    assertEquals("FR", cm.getContributor().get(6).getCountry());

    assertEquals(
        "Aristotle University of Thessaloniki",
        cm.getContributor().get(7).getOrganisation()
    );
    assertEquals("02j61yw88", cm.getContributor().get(7).getRorid());
    assertEquals("Thessaloniki", cm.getContributor().get(7).getCity());
    assertEquals("GR", cm.getContributor().get(7).getCountry());

    // Scope and quality
    assertEquals("global", cm.getGeographicScope());
    assertEquals("Fishes", cm.getTaxonomicScope());
    assertEquals("Extant taxa described until 1987", cm.getTemporalScope());
    assertEquals(5, cm.getConfidence());
    assertEquals(95, cm.getCompleteness());

    // Licensing and links
    assertEquals("CC0-1.0", cm.getLicense());
    assertEquals("https://www.fishbase.org", cm.getUrl().toString());
    assertEquals(
        "https://github.com/CatalogueOfLife/data/issues",
        cm.getFeedbackUrl().toString()
    );
    assertEquals(
        "https://www.fishbase.de/images/gifs/fblogo_new.gif",
        cm.getLogo().toString()
    );

    // URL formatters
    assertNotNull(cm.getUrlFormatter());
    assertEquals(
        "https://fishbase.mnhn.fr/summary/{ID}",
        cm.getUrlFormatter().getName()
    );
    assertEquals(
        "https://fishbase.mnhn.fr/summary/{ID}",
        cm.getUrlFormatter().getTaxon()
    );
    assertEquals(
        "https://fishbase.mnhn.fr/Nomenclature/SynonymSummary.php?GSID={TAXONID}&ID={ID}",
        cm.getUrlFormatter().getSynonym()
    );
    assertEquals(
        "https://fishbase.mnhn.fr/references/FBRefSummary.php?ID={ID}",
        cm.getUrlFormatter().getReference()
    );
    assertEquals(
        "https://fishbase.mnhn.fr/collaborators/CollaboratorSummary.php?ID={ID}",
        cm.getUrlFormatter().getAuthor()
    );
    assertNull(cm.getUrlFormatter().getTypeMaterial());

    // Conversion
    assertNotNull(cm.getConversion());
    assertEquals(
        "The MySQL database is being exported on a monthly basis to ColDP CSV files with the help of a python script.",
        cm.getConversion().getDescription()
    );
    assertEquals(
        "https://github.com/CatalogueOfLife/coldp-generator",
        cm.getConversion().getUrl().toString()
    );

    // Sources
    assertNotNull(cm.getSource());
    assertEquals(2, cm.getSource().size());

    var source = cm.getSource().get(0);
    assertEquals("ecf", source.getId());
    assertEquals("book", source.getType().toString());
    assertEquals("Eschmeyer's Catalog of Fishes", source.getTitle());
    assertEquals("ECoF", source.getAlias());
    assertEquals("2021-04", source.getIssued().toString());
    assertEquals("13 April 2021", source.getVersion());
    assertEquals(
        "https://researcharchive.calacademy.org/research/ichthyology/catalog/fishcatmain.asp",
        source.getUrl().toString()
    );

    assertNotNull(source.getEditor());
    assertEquals(3, source.getEditor().size());

    // Simple string names
    assertEquals("Ronald Fricke", source.getEditor().get(0).getLiteral());
    assertEquals("Van der Laan, R.", source.getEditor().get(1).getLiteral());

    // Structured name
    assertEquals("William N.", source.getEditor().get(2).getGiven());
    assertEquals("Eschmeyer", source.getEditor().get(2).getFamily());

    assertEquals("Remarks, comments and usage notes about this dataset", cm.getNotes());
  }

}