package org.gbif.ipt.service.registry.impl;

import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.metrics.cube.OccurrenceCube;
import org.gbif.api.model.metrics.cube.ReadBuilder;
import org.gbif.api.model.registry.Dataset;
import org.gbif.api.model.registry.Installation;
import org.gbif.api.model.registry.Organization;
import org.gbif.api.service.metrics.CubeService;
import org.gbif.api.service.registry.DatasetService;
import org.gbif.api.service.registry.InstallationService;
import org.gbif.api.service.registry.OrganizationService;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.DatasetType;
import org.gbif.api.vocabulary.InstallationType;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import static org.gbif.ipt.config.RegistryTestModule.webserviceClient;
import static org.gbif.ipt.config.RegistryTestModule.webserviceClientReadOnly;

import static org.junit.Assert.assertEquals;

public class RegistryWsClientTest {

  // logging
  private static final Logger LOG = Logger.getLogger(RegistryWsClientTest.class);
  private static final int PAGING_LIMIT = 100;

  @Test
  public void testGetDataset() {
    DatasetService ds = webserviceClientReadOnly().getInstance(DatasetService.class);
    Dataset dataset = ds.get(UUID.fromString("8575f23e-f762-11e1-a439-00145eb45e9a"));
    assertEquals("PonTaurus collection", dataset.getTitle());
  }

  @Ignore
     public void testUpdateDataset() {
    DatasetService ds = webserviceClient().getInstance(DatasetService.class);
    Dataset dataset = ds.get(UUID.fromString("8575f23e-f762-11e1-a439-00145eb45e9a"));
    dataset.setRights("CC0");
    ds.update(dataset);
  }

  /**
   * Gather statistics needed to update IPT statistics on http://www.gbif.org/ipt/stats.
   * Iterates through all installations, looking for IPT installations. For each IPT installation, it counts the
   * number of occurrence, checklist, and metadata-only datasets hosted by that installation.
   * Remember to configure registry.properties to connect to the desired service URLs.
   */
  @Test
  public void gatherStatistics() {
    InstallationService installationService = webserviceClientReadOnly().getInstance(InstallationService.class);
    CubeService occurrenceCubeService = webserviceClientReadOnly().getInstance(CubeService.class);
    OrganizationService organizationService = webserviceClientReadOnly().getInstance(OrganizationService.class);

    int installationCount = 0;
    int iptInstallationCount = 0;
    int iptDatasetCount = 0;
    int iptChecklistDatasetCount = 0;
    int iptOccurrenceDatasetCount = 0;
    int iptMetadataDatasetCount = 0;
    long totalOccurrenceRecords = 0;
    Set<Country> countriesRepresented = Sets.newHashSet();
    Set<UUID> checklistDatasetPublisherKeys = Sets.newHashSet();
    Set<UUID> occurrenceDatasetPublisherKeys = Sets.newHashSet();
    Set<UUID> metadataDatasetPublisherKeys = Sets.newHashSet();
    PagingRequest installationPage = new PagingRequest(0, PAGING_LIMIT);
    PagingResponse<Installation> installationsResults;
    do {
      installationsResults = installationService.list(installationPage);

      // count # of IPT installations
      for (Installation installation : installationsResults.getResults()) {
        installationCount++;
        if (installation.getType().equals(InstallationType.IPT_INSTALLATION)) {
          iptInstallationCount++;
          // count number of countries where IPTs are installed
          Organization organization = organizationService.get(installation.getOrganizationKey());
          countriesRepresented.add(organization.getCountry());

          // count # of datasets hosted by IPT installations
          PagingRequest datasetPage = new PagingRequest(0, PAGING_LIMIT);
          PagingResponse<Dataset> datasetsResults;
          do {
            datasetsResults = installationService.getHostedDatasets(installation.getKey(), datasetPage);

            for (Dataset dataset : datasetsResults.getResults()) {
              iptDatasetCount++;

              // count how many datasets are Checklist datasets, and how many different publishers share them?
              if (dataset.getType().equals(DatasetType.CHECKLIST)) {
                iptChecklistDatasetCount++;
                checklistDatasetPublisherKeys.add(dataset.getOwningOrganizationKey());
              }
              // how many datasets are Occurrence datasets, and how many different publishers share them?
              else if (dataset.getType().equals(DatasetType.OCCURRENCE)) {
                iptOccurrenceDatasetCount++;
                occurrenceDatasetPublisherKeys.add(dataset.getOwningOrganizationKey());
                // how many occurrence records?
                long numOccurrences =
                  occurrenceCubeService.get(new ReadBuilder().at(OccurrenceCube.DATASET_KEY, dataset.getKey()));
                totalOccurrenceRecords = totalOccurrenceRecords + numOccurrences;
              }
              // how many datasets are Metadata-only datasets, and how many different publishers share them?
              else {
                iptMetadataDatasetCount++;
                metadataDatasetPublisherKeys.add(dataset.getOwningOrganizationKey());
              }
            }

            datasetPage.nextPage();
          } while (!datasetsResults.isEndOfRecords());
        }
      }

      installationPage.nextPage();
    } while (!installationsResults.isEndOfRecords());

    LOG.info(iptInstallationCount + " out of " + installationCount + " installations are IPTs");
    LOG.info(
      iptInstallationCount + " IPTs hosted in " + countriesRepresented.size() + " countries serve " + iptDatasetCount
      + " datasets");
    LOG.info(iptChecklistDatasetCount + " checklist datasets published by " + checklistDatasetPublisherKeys.size()
             + " publishers");
    LOG.info(iptOccurrenceDatasetCount + " occurrence datasets published by " + occurrenceDatasetPublisherKeys.size()
             + " publishers totalling " + totalOccurrenceRecords + " records");
    LOG.info(iptMetadataDatasetCount + " metadata-only datasets published by " + metadataDatasetPublisherKeys.size()
             + " publishers");
  }

}
