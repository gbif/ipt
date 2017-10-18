package org.gbif.ipt.service.registry.impl;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.gbif.api.model.checklistbank.DatasetMetrics;
import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.metrics.cube.OccurrenceCube;
import org.gbif.api.model.metrics.cube.ReadBuilder;
import org.gbif.api.model.registry.Dataset;
import org.gbif.api.model.registry.Installation;
import org.gbif.api.model.registry.Organization;
import org.gbif.api.service.checklistbank.DatasetMetricsService;
import org.gbif.api.service.metrics.CubeService;
import org.gbif.api.service.registry.DatasetService;
import org.gbif.api.service.registry.InstallationService;
import org.gbif.api.service.registry.OrganizationService;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.DatasetType;
import org.gbif.api.vocabulary.InstallationType;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;

import static org.gbif.ipt.config.RegistryTestModule.webserviceClient;
import static org.gbif.ipt.config.RegistryTestModule.webserviceClientReadOnly;
import static org.junit.Assert.assertEquals;

@Ignore("These require live UAT webservice and should therefore only run when manually triggered")
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

  @Test
  public void testUpdateDataset() {
    DatasetService ds = webserviceClient().getInstance(DatasetService.class);
    Dataset dataset = ds.get(UUID.fromString("8575f23e-f762-11e1-a439-00145eb45e9a"));
    dataset.setRights("CC0");
    ds.update(dataset);
  }

  /**
   * Gather statistics needed to update IPT statistics on http://www.gbif.org/ipt/stats.
   * Iterates through all installations, looking for IPT installations. For each IPT installation, it counts the
   * number of occurrence, sampling-event, checklist, and metadata-only datasets hosted by that installation.
   * For each occurrence dataset and sampling-event dataset it counts the number of records.
   * For each checklist, it counts the number of usages and the number of occurrence records.
   * </br>
   * Remember to configure registry.properties to connect to the desired service URLs.
   */
  @Ignore("Uses a deprecated API, see https://github.com/gbif/ipt/issues/1366")
  public void gatherStatistics() {
    InstallationService installationService = webserviceClientReadOnly().getInstance(InstallationService.class);
    CubeService occurrenceCubeService = webserviceClientReadOnly().getInstance(CubeService.class);
    OrganizationService organizationService = webserviceClientReadOnly().getInstance(OrganizationService.class);
    DatasetMetricsService datasetMetricsService = webserviceClientReadOnly().getInstance(DatasetMetricsService.class);

    int installationCount = 0;
    int iptInstallationCount = 0;
    int iptDatasetCount = 0;
    int iptChecklistDatasetCount = 0;
    int iptOccurrenceDatasetCount = 0;
    int iptSamplingEventDatasetCount = 0;
    int iptMetadataDatasetCount = 0;
    long totalOccurrenceRecords = 0;
    long totalOccurrenceRecordsFromSamplingEventDatasets = 0;
    long totalNameUsages = 0;
    long totalOccurrenceRecordsFromChecklists = 0;
    Set<Country> countriesRepresented = Sets.newHashSet();
    Set<UUID> checklistDatasetPublisherKeys = Sets.newHashSet();
    Set<UUID> occurrenceDatasetPublisherKeys = Sets.newHashSet();
    Set<UUID> samplingEventDatasetPublisherKeys = Sets.newHashSet();
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
                checklistDatasetPublisherKeys.add(dataset.getPublishingOrganizationKey());
                // how many name usages?
                DatasetMetrics metrics = datasetMetricsService.get(dataset.getKey());
                if (metrics != null) {
                  long numNameUsages = metrics.getUsagesCount();
                  //LOG.info("Checklist [" + dataset.getKey() + "] has " + numNameUsages + " usages");
                  totalNameUsages = totalNameUsages + numNameUsages;
                }
                // how many occurrence records
                long numOccurrencesForChecklist =
                  occurrenceCubeService.get(new ReadBuilder().at(OccurrenceCube.DATASET_KEY, dataset.getKey()));
                if (numOccurrencesForChecklist > 0 && !dataset.getInstallationKey()
                  .equals(UUID.fromString("9afa1395-6e93-4848-a42d-bce896f5195e"))) {
                  //LOG.info("Checklist [" + dataset.getKey() + "] has " + numOccurrencesForChecklist + " occurrence records");
                  totalOccurrenceRecordsFromChecklists =
                    totalOccurrenceRecordsFromChecklists + numOccurrencesForChecklist;
                }
              }
              // how many datasets are Occurrence datasets, and how many different publishers share them?
              else if (dataset.getType().equals(DatasetType.OCCURRENCE)) {
                iptOccurrenceDatasetCount++;
                occurrenceDatasetPublisherKeys.add(dataset.getPublishingOrganizationKey());
                // how many occurrence records?
                long numOccurrences =
                  occurrenceCubeService.get(new ReadBuilder().at(OccurrenceCube.DATASET_KEY, dataset.getKey()));
                totalOccurrenceRecords = totalOccurrenceRecords + numOccurrences;
              }
              // how many datasets are Sampling-event datasets, and how many different publishers share them?
              else if (dataset.getType().equals(DatasetType.SAMPLING_EVENT)) {
                iptSamplingEventDatasetCount++;
                samplingEventDatasetPublisherKeys.add(dataset.getPublishingOrganizationKey());
                // how many occurrence records?
                long numOccurrences =
                  occurrenceCubeService.get(new ReadBuilder().at(OccurrenceCube.DATASET_KEY, dataset.getKey()));
                totalOccurrenceRecordsFromSamplingEventDatasets =
                  totalOccurrenceRecordsFromSamplingEventDatasets + numOccurrences;
              }
              // how many datasets are Metadata-only datasets, and how many different publishers share them?
              else {
                iptMetadataDatasetCount++;
                metadataDatasetPublisherKeys.add(dataset.getPublishingOrganizationKey());
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
             + " publishers totalling " + totalNameUsages + " usages and " + totalOccurrenceRecordsFromChecklists + " occurrence records");
    LOG.info(iptOccurrenceDatasetCount + " occurrence datasets published by " + occurrenceDatasetPublisherKeys.size()
             + " publishers totalling " + totalOccurrenceRecords + " occurrence records");
    LOG.info(
      iptSamplingEventDatasetCount + " sampling event datasets published by " + samplingEventDatasetPublisherKeys.size()
      + " publishers totalling " + totalOccurrenceRecordsFromSamplingEventDatasets + " occurrence records");
    LOG.info(iptMetadataDatasetCount + " metadata-only datasets published by " + metadataDatasetPublisherKeys.size()
             + " publishers");
  }

}
