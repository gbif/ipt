package org.gbif.ipt.service.registry.impl;

import org.gbif.api.model.checklistbank.DatasetMetrics;
import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.metrics.cube.OccurrenceCube;
import org.gbif.api.model.metrics.cube.ReadBuilder;
import org.gbif.api.model.registry.Dataset;
import org.gbif.api.model.registry.Endpoint;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.gbif.ipt.config.RegistryTestModule.webserviceClient;
import static org.gbif.ipt.config.RegistryTestModule.webserviceClientReadOnly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Ignore("These require live UAT webservice and should therefore only run when manually triggered")
public class RegistryWsClientTest {

  // logging
  private static final Logger LOG = Logger.getLogger(RegistryWsClientTest.class);
  private static final int PAGING_LIMIT = 100;
  // timeout in milliseconds for both the connection timeout and the response read timeout
  private static final int TIMEOUT_MILLIS = 2000;
  private static final SAXParserFactory saxParserFactory = provideNsAwareSaxParserFactory();
  private static final String IPT_RSS_NAMESPACE = "http://ipt.gbif.org/";
  private static final Pattern ESCAPE_CHARS = Pattern.compile("[\t\n\r]");

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
    List<String[]> stats = Lists.newArrayList();
    PagingRequest installationPage = new PagingRequest(0, PAGING_LIMIT);
    PagingResponse<Installation> installationsResults;
    do {
      installationsResults = installationService.list(installationPage);

      // count # of IPT installations
      for (Installation installation : installationsResults.getResults()) {
        installationCount++;
        if (installation.getType().equals(InstallationType.IPT_INSTALLATION)) {
          iptInstallationCount++;

          // check what version this IPT is running by reading its RSS feed
          // Be aware!: attempting to connect to read each IPTs' RSS feed slows down this method significantly!
          String[] row = new String[4];
          stats.add(row);
          List<Endpoint> endpointList = installation.getEndpoints();
          if (!endpointList.isEmpty() && endpointList.size() == 1) {
            Endpoint endpoint = endpointList.get(0);
            row[0] = (endpoint != null && endpoint.getUrl() != null) ? endpoint.getUrl().toString() : "";
            row[1] = (installation.getKey() != null) ? installation.getKey().toString() : "";
            try {
              RSS rss = pingURL(endpoint.getUrl().toURL());
              row[2] = (rss != null) ? rss.getIdentifier() : "";
              row[3] = (rss != null) ? rss.getVersion() : "";
            } catch (MalformedURLException e) {
              LOG.error("RSS endpoint has malformed URL! " + endpoint.getUrl().toString());
            }
          }

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

    // write stats to file
    writeStatsToFile(stats);

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

  /**
   * Requests a RSS feed with GET request and parses it into RSS object.
   *
   * @param url     The RSS HTTP URL to be pinged
   *
   * @return Populated RSS object or null if URL was offline or version couldn't be determined for any other reason
   */
  public RSS pingURL(URL url) {
    RSS rss = null;
    try {
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(TIMEOUT_MILLIS);
      connection.setReadTimeout(TIMEOUT_MILLIS);
      connection.setRequestMethod("GET");
      try {
        rss = parse(connection.getInputStream());
      } catch (Exception e) {
        LOG.error("Failed to parse RSS feed: " + rss, e);
      }
      connection.disconnect();
    } catch (IOException exception) {
      return null;
    }
    return rss;
  }

  @Test
  public void testPingUrl() throws MalformedURLException {
    RSS rss = pingURL(new URL("http://ipt.ala.org.au/rss.do"));
    assertNotNull(rss);
    assertEquals("1322b7b1-6b85-499f-964e-5e8599c73e6e", rss.getIdentifier());
    assertEquals("GBIF IPT 2.3.4-r68469e8", rss.getVersion());
  }

  @Test
  public void testRSSParsing() throws IOException, SAXException, ParserConfigurationException {
    InputStream rssIs = RegistryWsClientTest.class.getResourceAsStream("/responses/rss.xml");
    RSS rss = parse(rssIs);
    assertNotNull(rss);
    assertEquals("1322b7b1-6b85-499f-964e-5e8599c73e6e", rss.getIdentifier());
    assertEquals("GBIF IPT 2.3.4-r68469e8", rss.getVersion());
  }

  /**
   * Parses a RSS response as input stream.
   *
   * @param is For the XML of the RSS feed
   *
   * @return Populated RSS object or null if the RSS response could not be extracted for any reason
   */
  private RSS parse(InputStream is) throws ParserConfigurationException, SAXException, IOException {
    // in order to deal with arbitrary namespace prefixes we need to parse namespace aware!
    Digester digester = new Digester(saxParserFactory.newSAXParser());
    digester.setRuleNamespaceURI(IPT_RSS_NAMESPACE);
    digester.setNamespaceAware(true);

    RSS rss = new RSS();
    digester.push(rss);
    digester.addBeanPropertySetter("*/identifier", "identifier");
    digester.addBeanPropertySetter("*/generator", "version");
    digester.parse(is);
    return rss;
  }

  @Singleton
  private static SAXParserFactory provideNsAwareSaxParserFactory() {
    SAXParserFactory saxf = null;
    try {
      saxf = SAXParserFactory.newInstance();
      saxf.setValidating(false);
      saxf.setNamespaceAware(true);
    } catch (Exception e) {
      LOG.error("Failed to create SAX Parser Factory: " + e.getMessage(), e);
    }
    return saxf;
  }

  /**
   * Class representing RSS feed with select properties of interest.
   */
  public class RSS {
    private String identifier;
    private String version;

    public RSS() {
    }

    public String getIdentifier() {
      return identifier;
    }

    public void setIdentifier(String identifier) {
      this.identifier = identifier;
    }

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }
  }

  /**
   * Method writes each string array as row to output file.
   */
  private void writeStatsToFile(List<String[]> ls) {
    LOG.info("Writing IPT stats..");
    Writer writer;
    File out = null;
    try {
      File outputDirectory = org.gbif.utils.file.FileUtils.createTempDir();
      out = new File(outputDirectory, "stats.tab");
      LOG.info("IPT stats written to: " + out.getAbsolutePath());
      writer = org.gbif.utils.file.FileUtils.startNewUtf8File(out);
      // write header to output file
      String[] header = new String[]{"RSS URL", "Registry UUID", "RSS UUID", "RSS Version"};
      writer.write(tabRow(header));
      // write each string array to output file
      for (String[] r : ls) {
        writer.write(tabRow(r));
      }
      writer.close();
    } catch (IOException e) {
      LOG.error("Exception while writing to output file: " + out.getAbsolutePath());
    }
  }

  /**
   * Generate a row/string of values tab delimited. Line breaking characters encountered in
   * a value are replaced with an empty character.
   *
   * @param columns array of values/columns
   *
   * @return row/string of values tab delimited
   */
  @NotNull
  public static String tabRow(String[] columns) {
    // escape \t \n \r chars, and wrap in double quotes
    for (int i = 0; i < columns.length; i++) {
      if (columns[i] != null) {
        columns[i] = "\"" + StringUtils.trimToNull(ESCAPE_CHARS.matcher(columns[i]).replaceAll(" ")) + "\"";
      }
    }
    return StringUtils.join(columns, '\t') + "\n";
  }

}
