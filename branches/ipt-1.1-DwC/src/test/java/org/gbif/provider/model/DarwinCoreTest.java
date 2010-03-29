/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.model;

import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.datasource.ImportSourceFactory;
import org.gbif.provider.model.factory.DarwinCoreFactory;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ResourceTestBase;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Unit tests for DarwinCore.
 * 
 */
public class DarwinCoreTest extends ResourceTestBase {

  private static final long TEST_RESOURCE_ID = 13;
  private static final long TEST_TAXON_ID = 39516;
  private static final long TEST_REGION_ID = 7751;
  private static final String TEST_GUID = "TestGuid";
  private static final String TEST_SOURCE_ID = "-1";
  private static final Date TEST_DATE = new Date();

  private static DarwinCore getTestEntity(DarwinCore e) {
    DarwinCore entity = e == null ? new DarwinCore() : e;
    int i = 1;
    entity.setAcceptedNameUsageID(i++ + "");
    entity.setAccessRights(i++ + "");
    entity.setAssociatedMedia(i++ + "");
    entity.setAssociatedOccurrences(i++ + "");
    entity.setAssociatedReferences(i++ + "");
    entity.setAssociatedSequences(i++ + "");
    entity.setAssociatedTaxa(i++ + "");
    entity.setBasisOfRecord(i++ + "");
    entity.setBed(i++ + "");
    entity.setBehavior(i++ + "");
    entity.setBibliographicCitation(i++ + "");
    entity.setCatalogNumber(i++ + "");
    entity.setCollectionCode(i++ + "");
    entity.setCollectionID(i++ + "");
    entity.setContinent(i++ + "");
    entity.setCoordinatePrecision(i++ + "");
    entity.setCoordinateUncertaintyInMeters(i++ + "");
    entity.setCountry(i++ + "");
    entity.setCountryCode(i++ + "");
    entity.setCounty(i++ + "");
    entity.setDataGeneralizations(i++ + "");
    entity.setDatasetID(i++ + "");
    entity.setDatasetName(i++ + "");
    entity.setDateIdentified(i++ + "");
    entity.setDay(i++ + "");
    entity.setDecimalLatitude(i++ + "");
    entity.setDecimalLongitude(i++ + "");
    entity.setDisposition(i++ + "");
    entity.setDynamicProperties(i++ + "");
    entity.setEarliestAgeOrLowestStage(i++ + "");
    entity.setEarliestEonOrLowestEonothem(i++ + "");
    entity.setEarliestEpochOrLowestSeries(i++ + "");
    entity.setEarliestEraOrLowestErathem(i++ + "");
    entity.setEarliestPeriodOrLowestSystem(i++ + "");
    entity.setEndDayOfYear(i++ + "");
    entity.setEstablishmentMeans(i++ + "");
    entity.setEventDate(i++ + "");
    entity.setEventID(i++ + "");
    entity.setEventRemarks(i++ + "");
    entity.setEventTime(i++ + "");
    entity.setFamily(i++ + "");
    entity.setFieldNotes(i++ + "");
    entity.setFieldNumber(i++ + "");
    entity.setFootprintSRS(i++ + "");
    entity.setFootprintSpatialFit(i++ + "");
    entity.setFootprintWKT(i++ + "");
    entity.setFormation(i++ + "");
    entity.setGenus(i++ + "");
    entity.setGeodeticDatum(i++ + "");
    entity.setGeologicalContextID(i++ + "");
    entity.setGeoreferenceProtocol(i++ + "");
    entity.setGeoreferenceRemarks(i++ + "");
    entity.setGeoreferenceSources(i++ + "");
    entity.setGeoreferenceVerificationStatus(i++ + "");
    entity.setGeoreferencedBy(i++ + "");
    entity.setGroup(i++ + "");
    entity.setHabitat(i++ + "");
    entity.setHigherClassification(i++ + "");
    entity.setHigherGeography(i++ + "");
    entity.setHigherGeographyID(i++ + "");
    entity.setHighestBiostratigraphicZone(i++ + "");
    entity.setIdentificationID(i++ + "");
    entity.setIdentificationQualifier(i++ + "");
    entity.setIdentificationReferences(i++ + "");
    entity.setIdentificationRemarks(i++ + "");
    entity.setIdentifiedBy(i++ + "");
    entity.setIndividualCount(i++ + "");
    entity.setIndividualID(i++ + "");
    entity.setInformationWithheld(i++ + "");
    entity.setInfraspecificEpithet(i++ + "");
    entity.setInstitutionCode(i++ + "");
    entity.setInstitutionID(i++ + "");
    entity.setIsland(i++ + "");
    entity.setIslandGroup(i++ + "");
    entity.setKingdom(i++ + "");
    entity.setLanguage(i++ + "");
    entity.setLatestAgeOrHighestStage(i++ + "");
    entity.setLatestEonOrHighestEonothem(i++ + "");
    entity.setLatestEpochOrHighestSeries(i++ + "");
    entity.setLatestEraOrHighestErathem(i++ + "");
    entity.setLatestPeriodOrHighestSystem(i++ + "");
    entity.setLifeStage(i++ + "");
    entity.setLithostratigraphicTerms(i++ + "");
    entity.setLocality(i++ + "");
    entity.setLocationAccordingTo(i++ + "");
    entity.setLocationID(i++ + "");
    entity.setLocationRemarks(i++ + "");
    entity.setLowestBiostratigraphicZone(i++ + "");
    entity.setMaximumDepthInMeters(i++ + "");
    entity.setMaximumDistanceAboveSurfaceInMeters(i++ + "");
    entity.setMaximumElevationInMeters(i++ + "");
    entity.setMember(i++ + "");
    entity.setMinimumDepthInMeters(i++ + "");
    entity.setMinimumDistanceAboveSurfaceInMeters(i++ + "");
    entity.setMinimumElevationInMeters(i++ + "");
    entity.setDateModified(TEST_DATE);
    entity.setMonth(i++ + "");
    entity.setMunicipality(i++ + "");
    entity.setNameAccordingTo(i++ + "");
    entity.setNameAccordingToID(i++ + "");
    entity.setNamePublishedIn(i++ + "");
    entity.setNamePublishedInID(i++ + "");
    entity.setNomenclaturalCode(i++ + "");
    entity.setNomenclaturalStatus(i++ + "");
    entity.setOccurrenceDetails(i++ + "");
    entity.setOccurrenceID(i++ + "");
    entity.setOccurrenceRemarks(i++ + "");
    entity.setOccurrenceStatus(i++ + "");
    entity.setOrder(i++ + "");
    entity.setOriginalNameUsage(i++ + "");
    entity.setOriginalNameUsageID(i++ + "");
    entity.setOtherCatalogNumbers(i++ + "");
    entity.setOwnerInstitutionCode(i++ + "");
    entity.setParentNameUsage(i++ + "");
    entity.setParentNameUsageID(i++ + "");
    entity.setPhylum(i++ + "");
    entity.setPointRadiusSpatialFit(i++ + "");
    entity.setPreparations(i++ + "");
    entity.setPreviousIdentifications(i++ + "");
    entity.setRecordNumber(i++ + "");
    entity.setRecordedBy(i++ + "");
    entity.setReproductiveCondition(i++ + "");
    entity.setRights(i++ + "");
    entity.setRightsHolder(i++ + "");
    entity.setSamplingEffort(i++ + "");
    entity.setSamplingProtocol(i++ + "");
    entity.setScientificName(i++ + "");
    entity.setScientificNameAuthorship(i++ + "");
    entity.setScientificNameID(i++ + "");
    entity.setSex(i++ + "");
    entity.setSpecificEpithet(i++ + "");
    entity.setStartDayOfYear(i++ + "");
    entity.setStateProvince(i++ + "");
    entity.setSubgenus(i++ + "");
    entity.setTaxonConceptID(i++ + "");
    entity.setTaxonID(i++ + "");
    entity.setTaxonRank(i++ + "");
    entity.setTaxonRemarks(i++ + "");
    entity.setTaxonomicStatus(i++ + "");
    entity.setType(i++ + "");
    entity.setTypeStatus(i++ + "");
    entity.setVerbatimCoordinateSystem(i++ + "");
    entity.setVerbatimCoordinates(i++ + "");
    entity.setVerbatimDepth(i++ + "");
    entity.setVerbatimElevation(i++ + "");
    entity.setVerbatimEventDate(i++ + "");
    entity.setVerbatimLatitude(i++ + "");
    entity.setVerbatimLocality(i++ + "");
    entity.setVerbatimLongitude(i++ + "");
    entity.setVerbatimSRS(i++ + "");
    entity.setVerbatimTaxonRank(i++ + "");
    entity.setVernacularName(i++ + "");
    entity.setWaterbody(i++ + "");
    entity.setYear(i++ + "");
    return entity;
  }

  @Autowired
  private DarwinCoreManager entityManager;

  @Autowired
  private OccResourceManager resourceManager;

  @Autowired
  private TaxonManager taxonManager;

  @Autowired
  private ImportSourceFactory importSourceFactory;

  @Autowired
  private RegionManager regionManager;

  @Autowired
  private DarwinCoreFactory darwinCoreFactory;

  @Test
  public void consistentWithEquals() {
    DarwinCore e0 = getTestEntity(null);
    DarwinCore e1 = getTestEntity(null);
    Assert.assertEquals(e0, e1);
    Assert.assertEquals(0, e0.compareTo(e1));
  }

  @Test
  public void crud() {
    // Creates and stores an entity:
    OccurrenceResource resource = resourceManager.get(TEST_RESOURCE_ID);
    Taxon taxon = taxonManager.get(TEST_TAXON_ID);
    Region region = regionManager.get(TEST_REGION_ID);

    DarwinCore entity = getTestEntity(DarwinCore.with(TEST_SOURCE_ID, resource,
        taxon, region, TEST_GUID));

    entityManager.save(entity);
    entityManager.flush();

    Long id = entity.getId();

    // Verifies existence of the entity:
    Assert.assertTrue(entityManager.exists(entity.getId()));

    // Queries the entity by GUID:
    DarwinCore result = entityManager.get(TEST_GUID);
    Assert.assertNotNull(result);
    Assert.assertEquals(entity, result);

    // Queries for the entity by source id:
    result = entityManager.findBySourceId(entity.getSourceId(),
        resource.getId());
    Assert.assertNotNull(result);
    Assert.assertEquals(entity, result);

    // Queries for the entity by id:
    result = entityManager.get(entity.getId());
    Assert.assertNotNull(result);
    Assert.assertEquals(entity, result);

    // Removes entity from data store:
    entityManager.remove(entity);
    entityManager.flush();

    // Verifies entity no longer exists:
    Assert.assertFalse(entityManager.exists(id));
  }

  /**
   * Loads a source data set, saves each entity, then deletes each entity.
   * 
   * @throws ImportSourceException
   */
  @Test
  public void loadDataset() throws ImportSourceException {
    OccurrenceResource resource = resourceManager.get(TEST_RESOURCE_ID);
    ImportSource source;
    source = importSourceFactory.newInstance(resource,
        resource.getCoreMapping());
    Set<Annotation> annotations = Sets.newHashSet();
    DarwinCore entity;
    double i = Double.MAX_VALUE;
    for (ImportRecord ir : source) {
      if (ir != null) {
        entity = darwinCoreFactory.build(resource, ir, annotations);
        entity.setSourceId(i-- + "");
        entity.setCatalogNumber(i-- + "");
        entity.setGuid(i-- + "");
        entityManager.save(entity);
        entityManager.flush();
        Assert.assertTrue(entityManager.exists(entity.getId()));
        entityManager.remove(entity);
        entityManager.flush();
        Assert.assertFalse(entityManager.exists(entity.getId()));
      }
    }
  }

  @Test
  public void testEqualsAndHashcode() {
    OccurrenceResource resource = resourceManager.get(Constants.TEST_OCC_RESOURCE_ID);
    DarwinCore dc0 = new DarwinCore();
    dc0.setResource(resource);
    dc0.setGuid("guid");
    DarwinCore dc1 = new DarwinCore();
    dc1.setResource(resource);
    dc1.setGuid("guid");
    Assert.assertEquals(dc0, dc1);
    Assert.assertEquals(dc0.hashCode(), dc1.hashCode());
    Map<DarwinCore, String> map = ImmutableMap.of(dc0, "dc");
    Assert.assertEquals("dc", map.get(dc0));
  }
}
