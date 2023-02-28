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
package org.gbif.ipt.task;

import org.gbif.api.vocabulary.Language;
import org.gbif.common.parsers.LanguageParser;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.utils.InputStreamUtils;
import org.gbif.ipt.utils.ResourceUtils;
import org.gbif.metadata.eml.ipt.model.Agent;
import org.gbif.metadata.eml.ipt.model.BBox;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.metadata.eml.ipt.model.GeospatialCoverage;
import org.gbif.metadata.eml.ipt.model.KeywordSet;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Class to generate a DCAT feed, including all resources that are published, public and have a license.
 *
 * @see <a href="https://github.com/oSoc15/ipt-dcat">DCAT project homepage</a>
 * @see <a href="https://github.com/oSoc15/ipt">DCAT IPT Fork</a>
 */
@Singleton
public class GenerateDCAT {

  // logging
  private static final Logger LOG = LogManager.getLogger(GenerateDCAT.class);

  private static final String DCAT_SETTINGS = "org/gbif/metadata/eml/dcatsettings.properties";
  private static final String PREFIXES_PROPERTIES = "org/gbif/metadata/eml/dcat.properties";
  private static final LanguageParser LANGUAGE_PARSER = LanguageParser.getInstance();
  private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX");
  private static final InputStreamUtils streamUtils = new InputStreamUtils();

  // DCAT settings keys
  private static final String CATALOG_THEME_TITLE_KEY = "catalogThemeTitle";
  private static final String THEME_TAXONOMY_URI_KEY = "themeTaxonomyUri";
  private static final String DATASET_THEME_LABEL_KEY = "datasetThemeLabel";
  private static final String THEME_URI_KEY = "themeUri";
  private static final String LANGUAGE_LINK_KEY = "languageLink";
  private static final String PUBLISHER_BASELINK_KEY = "publisherBaselink";
  private static final String DATASET_BASELINK_KEY = "datasetBaselink";
  private static final String CATALOG_RIGHTS_KEY = "catalogRights";
  private static final String CACHING_TIME_KEY = "cachingTime";

  private final Map<String, String> settings;
  private final Map<String, String> prefixes;

  // String used to cache the feed
  private String feed = "";
  // time in milliseconds since feed was created
  private long time = 0;

  private final AppConfig cfg;
  private final RegistrationManager registrationManager;
  private final ResourceManager resourceManager;

  @Inject
  public GenerateDCAT(AppConfig cfg, RegistrationManager registrationManager, ResourceManager resourceManager) {
    this.cfg = cfg;
    this.registrationManager = registrationManager;
    this.resourceManager = resourceManager;
    settings = Collections.unmodifiableMap(loadDCATSettings());
    prefixes = Collections.unmodifiableMap(loadDCATPrefixes());
  }

  /**
   * Return the DCAT feed, recreating it if the caching time has been exceeded.
   *
   * @return DCAT feed
   */
  public String getFeed() {
    // determine caching time, from settings
    long cacheTime;
    try {
     cacheTime = Long.parseLong(settings.get(CACHING_TIME_KEY));
    } catch (NumberFormatException e) {
      throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_PROPERTIES_FILE,
        "Invalid caching time in properties file: " + DCAT_SETTINGS);
    }

    long now = System.currentTimeMillis();
    if ((now > time + cacheTime) || cfg.devMode()) {
      feed = createDCATFeed();
      time = now;
      LOG.info("Updated DCAT feed");
    }
    return feed;
  }

  /**
   * Create and regenerate the entire DCAT feed. This includes the Prefixes, Catalog, dataset(s), Distribution(s)
   * and the organization(s).
   *
   * @return DCAT feed
   */
  private String createDCATFeed() {
    StringBuilder feed = new StringBuilder();

    //Prefixes
    if (!prefixes.isEmpty()) {
      feed.append(createPrefixesInformation());
      feed.append("\n");
    }

    //Catalog
    feed.append(createDCATCatalogInformation());
    feed.append("\n");

    Set<String> organisations = new HashSet<>();

    //add organisation of Catalog
    String publisherBaselink = settings.get(PUBLISHER_BASELINK_KEY);
    if (registrationManager.getHostingOrganisation() != null && publisherBaselink != null) {
      Organisation org = registrationManager.getHostingOrganisation();
      String publisher = publisherBaselink + org.getKey() + "#Organization";
      String organisation =
        encapsulateObject(publisher, ObjectTypes.RESOURCE) + " a foaf:Agent ; foaf:name \"" + org.getName() + "\"";
      if (org.getHomepageURL() != null) {
        String homepagesStrWithoutBrackets = StringUtils.substringBetween(org.getHomepageURL(), "[", "]");

        if (StringUtils.isNotBlank(homepagesStrWithoutBrackets)) {
          String[] homepages = homepagesStrWithoutBrackets.split(",");

          String homepagesStr = Arrays.stream(homepages)
              .map(String::trim)
              .map(h -> encapsulateObject(h, ObjectTypes.RESOURCE))
              .collect(Collectors.joining(" , "));

          organisation += " ; foaf:homepage " + homepagesStr;
        }
      }
      organisation += " .";
      organisations.add(organisation);
    }

    //Datasets and Distributions
    Set<String> themes = new HashSet<>();
    String themeUri = settings.get(THEME_URI_KEY);
    String datasetThemeLabel = settings.get(DATASET_THEME_LABEL_KEY);
    String themeTaxonomyUri = settings.get(THEME_TAXONOMY_URI_KEY);
    boolean foundDatasets = false;

    // iterate through all published public versions
    for (Resource resource : resourceManager.listPublishedPublicVersions()) {
      if (themeUri != null && datasetThemeLabel != null && themeTaxonomyUri != null && publisherBaselink != null) {

        // reconstruct the last published public version
        BigDecimal v = resource.getLastPublishedVersionsVersion();
        String shortname = resource.getShortname();
        File versionEmlFile = cfg.getDataDir().resourceEmlFile(shortname, v);
        Resource publishedPublicVersion = ResourceUtils.reconstructVersion(v, resource.getShortname(), resource.getCoreType(), resource.getSchemaIdentifier(),
          resource.getAssignedDoi(), resource.getOrganisation(), resource.findVersionHistory(v), versionEmlFile,
          resource.getKey());

        // make sure it has a license
        if (publishedPublicVersion.getEml() != null && publishedPublicVersion.getEml().parseLicenseUrl() != null) {

          feed.append(createDCATDatasetInformation(publishedPublicVersion));
          feed.append("\n");
          feed.append(createDCATDistributionInformation(publishedPublicVersion));
          feed.append("\n");
          foundDatasets = true;

          //add Organisation of Dataset (optional)
          if (publishedPublicVersion.getOrganisation() != null) {
            String publisher = publisherBaselink + publishedPublicVersion.getOrganisation().getKey() + "#Organization";
            String organisation =
              encapsulateObject(publisher, ObjectTypes.RESOURCE) + " a foaf:Agent ; foaf:name \"" + publishedPublicVersion
                .getOrganisation().getName() + "\"";
            if (publishedPublicVersion.getOrganisation().getHomepageURL() != null) {
              String homepagesStrWithoutBrackets = StringUtils.substringBetween(
                  publishedPublicVersion.getOrganisation().getHomepageURL(), "[", "]");

              if (StringUtils.isNotBlank(homepagesStrWithoutBrackets)) {
                String[] homepages = homepagesStrWithoutBrackets.split(",");

                String homepagesStr = Arrays.stream(homepages)
                    .map(String::trim)
                    .map(h -> encapsulateObject(h, ObjectTypes.RESOURCE))
                    .collect(Collectors.joining(" , "));

                organisation += " ; foaf:homepage " + homepagesStr;
              }
            }

            organisation += " .";
            organisations.add(organisation);
          }

          //add Themes of datasets
          themes.add(
            encapsulateObject(themeUri, ObjectTypes.RESOURCE) + " a skos:Concept ; skos:prefLabel \"" + datasetThemeLabel
            + "\"@en ; skos:inScheme <" + themeTaxonomyUri + "> .");

        }
      }
    }

    if (foundDatasets) {
      LOG.debug("Resources appended to DCAT feed.");
    } else {
      feed.append("\n#No published public resources added to DCAT feed, a valid DCAT feed requires at least one!\n");
    }

    //Organisations
    for (String organisation : organisations) {
      feed.append(organisation);
      feed.append("\n");
    }
    feed.append("\n");

    //Themes
    for (String theme : themes) {
      feed.append(theme);
      feed.append("\n");
    }
    return feed.toString();
  }

  /**
   * This method loads the DCAT settings from dcatsettings.properties.
   */
  private Map<String, String> loadDCATSettings() {
    Map<String, String> loadedSettings = new HashMap<>();

    try (InputStream configStream = streamUtils.classpathStream(DCAT_SETTINGS)) {
      if (configStream == null) {
        LOG.error("Failed to load DCAT settings: " + DCAT_SETTINGS);
      } else {
        Properties properties = new Properties();
        properties.load(configStream);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
          String key = StringUtils.trim((String) entry.getKey());
          String value = StringUtils.trim((String) entry.getValue());
          if (key != null && value != null) {
            loadedSettings.put(key, value);
          } else {
            throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_PROPERTIES_FILE,
              "Invalid properties file: " + DCAT_SETTINGS);
          }
        }
        LOG.debug("Loaded static DCAT settings: " + loadedSettings);
      }
    } catch (Exception e) {
      LOG.error("Failed to load DCAT settings from: " + DCAT_SETTINGS, e);
    }
    return loadedSettings;
  }

  /**
   * This method loads the DCAT prefixes from dcat.properties.
   */
  private Map<String, String> loadDCATPrefixes() {
    HashMap<String, String> prefixes = new HashMap<>();
    InputStreamUtils streamUtils = new InputStreamUtils();
    try (InputStream configStream = streamUtils.classpathStream(PREFIXES_PROPERTIES);) {
      if (configStream == null) {
        LOG.error("Could not load DCAT prefixes from file: " + PREFIXES_PROPERTIES);
      } else {
        Properties properties = new Properties();
        properties.load(configStream);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
          String key = StringUtils.trim((String) entry.getKey());
          String value = StringUtils.trim((String) entry.getValue());
          if (key != null && value != null) {
            prefixes.put(key, value);
          } else {
            throw new InvalidConfigException(InvalidConfigException.TYPE.INVALID_PROPERTIES_FILE,
              "Invalid properties file: " + PREFIXES_PROPERTIES);
          }
        }
        LOG.debug("Loaded DCAT prefixes: " + prefixes);
      }
    } catch (Exception e) {
      LOG.error("Exception while loading DCAT prefixes", e);
    }
    return prefixes;
  }

  /**
   * Write all the prefixes needed in a String representation.
   *
   * @return String representation of Prefixes
   */
  protected String createPrefixesInformation() {
    StringBuilder prefixBuilder = new StringBuilder();
    for (String pre : prefixes.keySet()) {
      prefixBuilder.append("@prefix ");
      prefixBuilder.append(pre);
      prefixBuilder.append(": <");
      prefixBuilder.append(prefixes.get(pre));
      prefixBuilder.append("> .\n");
    }
    return prefixBuilder.toString();
  }

  /**
   * Create the DCAT feed for the Catalog.
   * </br>
   * Implemented:
   * <ul>
   * <li>dct:title</li>
   * <li>dct:description</li>
   * <li>dct:publisher</li>
   * <li>dcat:dataset</li>
   * <li>foaf:homepage</li>
   * <li>dct:issued</li>
   * <li>dct:modified</li>
   * <li>dcat:themeTaxonomy</li>
   * <li>dct:license</li>
   * <li>dct:spatial</li>
   * <li>dct:language</li>
   * </ul>
   * </p>
   *
   * @return String DCAT Catalog
   */
  protected String createDCATCatalogInformation() {
    StringBuilder catalogBuilder = new StringBuilder();
    List<String> themeTaxonomies = new ArrayList<>();
    Ipt ipt = registrationManager.getIpt();

    //Run over resources
    List<String> uris = new ArrayList<>();
    Date firstCreation = new Date();
    boolean firstPublishedDatePresent = false;
    Date lastModification = new Date(0);
    boolean lastPublishedDatePresent = false;
    for (Resource res : resourceManager.listPublishedPublicVersions()) {
      if (res.getEml().parseLicenseUrl() != null) {
        String uri = cfg.getResourceUrl(res.getShortname()) + "#Dataset";
        uris.add(uri);
        if (res.getCreated() != null && res.getCreated().before(firstCreation)) {
          firstCreation = res.getCreated();
          firstPublishedDatePresent = true;
        }
        if (res.getLastPublished() != null && res.getLastPublished().after(lastModification)) {
          lastModification = res.getLastPublished();
          lastPublishedDatePresent = true;
        }
      }
    }

    //Base
    String url = cfg.getBaseUrl();
    url += "#Catalog";
    catalogBuilder.append(encapsulateObject(url, ObjectTypes.RESOURCE));
    catalogBuilder.append("\n");
    catalogBuilder.append(" a dcat:Catalog");

    String publisherBaselink = settings.get(PUBLISHER_BASELINK_KEY);
    if (ipt != null && publisherBaselink != null) {
      //dct:title
      addPredicateToBuilder(catalogBuilder, "dct:title");
      addObjectToBuilder(catalogBuilder, ipt.getName(), ObjectTypes.LITERAL);

      //dct:description
      if (ipt.getDescription() != null) {
        addPredicateToBuilder(catalogBuilder, "dct:description");
        addObjectToBuilder(catalogBuilder, ipt.getDescription(), ObjectTypes.LITERAL);
      } else {
        LOG.debug("IPT description is null");
      }

      //dct:publisher
      addPredicateToBuilder(catalogBuilder, "dct:publisher");
      String publisher = publisherBaselink + ipt.getOrganisationKey() + "#Organization";
      addObjectToBuilder(catalogBuilder, publisher, ObjectTypes.RESOURCE);

      //dcat:dataset
      if (!uris.isEmpty()) {
        addPredicateToBuilder(catalogBuilder, "dcat:dataset");
        addObjectsToBuilder(catalogBuilder, uris, ObjectTypes.RESOURCE);
      }

      //foaf:homepage
      if (cfg.getBaseUrl() != null) {
        addPredicateToBuilder(catalogBuilder, "foaf:homepage");
        addObjectToBuilder(catalogBuilder, cfg.getBaseUrl(), ObjectTypes.RESOURCE);
      }

      //dct:issued
      if (firstPublishedDatePresent) {
        addPredicateToBuilder(catalogBuilder, "dct:issued");
        addObjectToBuilder(catalogBuilder, parseToIsoDate(firstCreation), ObjectTypes.LITERAL);
      }

      //dct:modified
      if (lastPublishedDatePresent) {
        addPredicateToBuilder(catalogBuilder, "dct:modified");
        addObjectToBuilder(catalogBuilder, parseToIsoDate(lastModification), ObjectTypes.LITERAL);
      }

      //dcat:themeTaxonomy
      String themeTaxonomyUri = settings.get(THEME_TAXONOMY_URI_KEY);
      String catalogThemeTitle = settings.get(CATALOG_THEME_TITLE_KEY);
      if (themeTaxonomyUri != null && catalogThemeTitle != null) {
        addPredicateToBuilder(catalogBuilder, "dcat:themeTaxonomy");
        themeTaxonomies.add(
          encapsulateObject(themeTaxonomyUri, ObjectTypes.RESOURCE) + " a skos:ConceptScheme ; dct:title \""
          + catalogThemeTitle + "\"@en .");
        addObjectToBuilder(catalogBuilder, themeTaxonomyUri, ObjectTypes.RESOURCE);
      }

      //dct:rights
      String catalogRights = settings.get(CATALOG_RIGHTS_KEY);
      if (catalogRights != null) {
        addPredicateToBuilder(catalogBuilder, "dct:license");
        addObjectToBuilder(catalogBuilder, catalogRights, ObjectTypes.RESOURCE);
      }

      //dct:spatial
      if (cfg.getLongitude() != null && cfg.getLatitude() != null) {
        addPredicateToBuilder(catalogBuilder, "dct:spatial");
        String spatial =
          " a dct:Location ; locn:geometry \"" + "{ \\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": [ " + cfg
            .getLongitude() + "," + cfg.getLatitude() + " ] }\" ";
        addObjectToBuilder(catalogBuilder, spatial, ObjectTypes.OBJECT);
      } else {
        LOG.debug("No spatial data defined for the IPT");
      }

      //dct:language
      String languageLink = settings.get(LANGUAGE_LINK_KEY);
      if (languageLink != null) {
        languageLink = languageLink + "en";
        addPredicateToBuilder(catalogBuilder, "dct:language");
        addObjectToBuilder(catalogBuilder, languageLink, ObjectTypes.RESOURCE);
      }
    } else {
      LOG.error("IPT has not been registered yet");
    }

    catalogBuilder.append(" .\n");
    catalogBuilder.append("\n");
    for (String tax : themeTaxonomies) {
      catalogBuilder.append(tax);
      catalogBuilder.append("\n");
    }
    return catalogBuilder.toString();
  }

  /**
   * Create a DCAT Dataset from one resource.
   * </br>
   * For every publisher it creates a foaf:agent with the name of the organisation.
   * </br>
   * Implmented:
   * <ul>
   * <li>dct:title</li>
   * <li>dct:description</li>
   * <li>dcat:keyword</li>
   * <li>dcat:theme</li>
   * <li>dcat:contactPoint</li>
   * <li>dct:issued</li>
   * <li>dct:modified</li>
   * <li>dct:spatial</li>
   * <li>adms:versionInfo</li>
   * <li>adms:versionNotes</li>
   * <li>dcat:landingPage</li>
   * <li>foaf:homepage</li>
   * <li>dct:identifier</li>
   * <li>dct:publisher</li>
   * <li>dcat:distribution</li>
   * <li>dct:language</li>
   * </ul>
   * </p>
   *
   * @param resource resource to create DCAT Dataset from
   *
   * @return String DCAT Dataset for one resource
   */
  protected String createDCATDatasetInformation(Resource resource) {
    StringBuilder datasetBuilder = new StringBuilder();
    Eml eml = resource.getEml();

    //Base
    String url = cfg.getResourceUrl(resource.getShortname()) + "#Dataset";
    datasetBuilder.append(encapsulateObject(url, ObjectTypes.RESOURCE));
    datasetBuilder.append("\n");
    datasetBuilder.append("a dcat:Dataset");

    //dct:title
    if (eml.getTitle() != null) {
      addPredicateToBuilder(datasetBuilder, "dct:title");
      addObjectToBuilder(datasetBuilder, eml.getTitle(), ObjectTypes.LITERAL);
    }

    //dct:description
    if (!eml.getDescription().isEmpty()) {
      addPredicateToBuilder(datasetBuilder, "dct:description");
      StringBuilder description = new StringBuilder();
      Iterator<String> iter = eml.getDescription().iterator();
      while (iter.hasNext()) {
        String des = StringUtils.trimToNull(iter.next());
        if (des != null) {
          description.append(des);
        }
        // turtle format requires line breaks to be escaped
        if (iter.hasNext()) {
          description.append("\\n");
        }
      }
      addObjectToBuilder(datasetBuilder, description.toString(), ObjectTypes.LITERAL);
    }

    //dcat:keyword
    // note: duplicate keywords cannot exist, see issue #1210
    if (!eml.getKeywords().isEmpty()) {
      List<String> keywords = new ArrayList<>();
      for (KeywordSet keywordSet : eml.getKeywords()) {
        for (String keyword : keywordSet.getKeywords()) {
          if (StringUtils.isNotBlank(keyword) && !keywords.contains(keyword)) {
            keywords.add(keyword);
          }
        }
      }
      addPredicateToBuilder(datasetBuilder, "dcat:keyword");
      addObjectsToBuilder(datasetBuilder, keywords, ObjectTypes.LITERAL);
    }

    //dcat:theme
    String theme = settings.get(THEME_URI_KEY);
    if (theme != null) {
      addPredicateToBuilder(datasetBuilder, "dcat:theme");
      addObjectToBuilder(datasetBuilder, theme, ObjectTypes.RESOURCE);
    }

    //adms:contactPoint
    for (Agent contact : eml.getContacts()) {
      addPredicateToBuilder(datasetBuilder, "dcat:contactPoint");
      String agent = " a vcard:Individual ; vcard:fn \"" + contact.getFullName() + "\"";
      if (contact.getEmail() != null) {
        agent += "; vcard:hasEmail <mailto:" + contact.getEmail() + "> ";
      }
      addObjectToBuilder(datasetBuilder, agent, ObjectTypes.OBJECT);
    }

    //dct:issued
    if (resource.getCreated() != null) {
      addPredicateToBuilder(datasetBuilder, "dct:issued");
      addObjectToBuilder(datasetBuilder, parseToIsoDate(resource.getCreated()), ObjectTypes.LITERAL);
    }

    //dct:modified
    if (resource.getLastPublished() != null) {
      addPredicateToBuilder(datasetBuilder, "dct:modified");
      addObjectToBuilder(datasetBuilder, parseToIsoDate(resource.getLastPublished()), ObjectTypes.LITERAL);
    }

    // dct:spatial
    // Uses geoJSON to represent the geospatial coverage, this can easily be verified at http://geojsonlint.com/
    // Note, a Polygon's first and last points must be equivalent
    for (GeospatialCoverage coverage : eml.getGeospatialCoverages()) {
      BBox bb = coverage.getBoundingCoordinates();
      addPredicateToBuilder(datasetBuilder, "dct:spatial");
      String spatial =
        " a dct:Location ; locn:geometry \"" + "{ \\\"type\\\": \\\"Polygon\\\", \\\"coordinates\\\": [ [ [" + bb
          .getMin().getLongitude() + "," + bb.getMin().getLatitude() + "], [" + bb.getMin().getLongitude() + "," + bb
          .getMax().getLatitude() + "], [" + bb.getMax().getLongitude() + "," + bb.getMax().getLatitude() + "], [" + bb
          .getMax().getLongitude() + "," + bb.getMin().getLatitude() + "], [" + bb.getMin().getLongitude() + "," + bb
          .getMin().getLatitude() + "] ] ] }" + "\" ";
      addObjectToBuilder(datasetBuilder, spatial, ObjectTypes.OBJECT);
    }

    //adms:versionInfo
    if (resource.getLastPublishedVersionsVersion() != null) {
      addPredicateToBuilder(datasetBuilder, "adms:versionInfo");
      addObjectToBuilder(datasetBuilder, resource.getLastPublishedVersionsVersion().toPlainString(),
        ObjectTypes.LITERAL);
    }

    //adms:versionNotes
    if (resource.getLastPublishedVersionsChangeSummary() != null) {
      addPredicateToBuilder(datasetBuilder, "adms:versionNotes");
      addObjectToBuilder(datasetBuilder, resource.getLastPublishedVersionsChangeSummary(), ObjectTypes.LITERAL);
    }

    //dcat:landingPage
    String landingPage = cfg.getResourceUrl(resource.getShortname());
    addPredicateToBuilder(datasetBuilder, "dcat:landingPage");
    addObjectToBuilder(datasetBuilder, landingPage, ObjectTypes.RESOURCE);

    //foaf:homepage
    if (eml.getHomepageUrl() != null) {
      addPredicateToBuilder(datasetBuilder, "foaf:homepage");
      addObjectToBuilder(datasetBuilder, eml.getHomepageUrl(), ObjectTypes.RESOURCE);
    }

    //dct:identifier
    String datasetBaselink = settings.get(DATASET_BASELINK_KEY);
    if (resource.getKey() != null && datasetBaselink != null) {
      addPredicateToBuilder(datasetBuilder, "dct:identifier");
      // TODO check if should be type Resource not Literal
      addObjectToBuilder(datasetBuilder, datasetBaselink + resource.getKey(), ObjectTypes.LITERAL);
    }

    //dct:publisher
    String publisherBaselink = settings.get(PUBLISHER_BASELINK_KEY);
    if (resource.getOrganisation() != null && publisherBaselink != null) {
      addPredicateToBuilder(datasetBuilder, "dct:publisher");
      String publisherLink = publisherBaselink + resource.getOrganisation().getKey() + "#Organization";
      addObjectToBuilder(datasetBuilder, publisherLink, ObjectTypes.RESOURCE);
    }

    //dcat:Distribution
    addPredicateToBuilder(datasetBuilder, "dcat:distribution");
    String dist = cfg.getResourceArchiveUrl(resource.getShortname());
    addObjectToBuilder(datasetBuilder, dist, ObjectTypes.RESOURCE);

    //dct:language (ISO 639 1 - 2 letter code)
    String languageLink = settings.get(LANGUAGE_LINK_KEY);
    if (languageLink != null) {
      addPredicateToBuilder(datasetBuilder, "dct:language");
      ParseResult<Language> result = LANGUAGE_PARSER.parse(eml.getMetadataLanguage());
      String ln = result.isSuccessful() ? languageLink + result.getPayload().getIso2LetterCode().toLowerCase()
        : languageLink + "en";
      addObjectToBuilder(datasetBuilder, ln, ObjectTypes.RESOURCE);
    }

    datasetBuilder.append(" .\n");
    return datasetBuilder.toString();
  }

  /**
   * Create a DCAT Distribution for one resource.
   * </br>
   * Implemented:
   * <ul>
   * <li>dct:description</li>
   * <li>dct:license</li>
   * <li>dct:format</li>
   * <li>dcat:mediaType</li>
   * <li>dcat:downloadURL</li>
   * <li>dcat:accessURL</li>
   * </ul>
   *
   * @param resource resource to create the DCAT Distribution from
   *
   * @return String DCAT Distribution for one resource
   */
  protected String createDCATDistributionInformation(Resource resource) {
    Objects.requireNonNull(resource.getEml());
    Objects.requireNonNull(resource.getEml().parseLicenseUrl());

    StringBuilder distributionBuilder = new StringBuilder();

    //Base
    String url = cfg.getResourceArchiveUrl(resource.getShortname());
    distributionBuilder.append(encapsulateObject(url, ObjectTypes.RESOURCE));
    distributionBuilder.append("\n");
    distributionBuilder.append("a dcat:Distribution");

    //dct:title
    if (resource.getEml().getTitle() != null) {
      addPredicateToBuilder(distributionBuilder, "dct:title");
      addObjectToBuilder(distributionBuilder, "Darwin Core Archive of " + resource.getEml().getTitle(), ObjectTypes.LITERAL);
    }

    //dct:description
    addPredicateToBuilder(distributionBuilder, "dct:description");
    addObjectToBuilder(distributionBuilder, "Darwin Core Archive", ObjectTypes.LITERAL);

    //dct:license
    addPredicateToBuilder(distributionBuilder, "dct:license");
    addObjectToBuilder(distributionBuilder, resource.getEml().parseLicenseUrl(), ObjectTypes.RESOURCE);

    //dct:format
    addPredicateToBuilder(distributionBuilder, "dct:format");
    addObjectToBuilder(distributionBuilder, "dwc-a", ObjectTypes.LITERAL);

    //dcat:mediaType
    addPredicateToBuilder(distributionBuilder, "dcat:mediaType");
    addObjectToBuilder(distributionBuilder, "application/zip", ObjectTypes.LITERAL);

    //dcat:downloadURL
    addPredicateToBuilder(distributionBuilder, "dcat:downloadURL");
    addObjectToBuilder(distributionBuilder, cfg.getResourceArchiveUrl(resource.getShortname()), ObjectTypes.RESOURCE);

    //dcat:accessURL
    addPredicateToBuilder(distributionBuilder, "dcat:accessURL");
    String accessURLClass =
      encapsulateObject(cfg.getResourceUrl(resource.getShortname()), ObjectTypes.RESOURCE) + " a rdfs:Resource .";
    addObjectToBuilder(distributionBuilder, cfg.getResourceUrl(resource.getShortname()), ObjectTypes.RESOURCE);

    distributionBuilder.append(" .\n");
    distributionBuilder.append(accessURLClass);
    distributionBuilder.append("\n");

    return distributionBuilder.toString();
  }

  /**
   * Method adds the predicate to the builder in turtle syntax: ends the previous predicate with a ; and a newline.
   *
   * @param builder   StringBuilder
   * @param predicate between the subject and object
   */
  private void addPredicateToBuilder(@NotNull StringBuilder builder, @NotNull String predicate) {
    builder.append(" ;\n");
    builder.append(predicate);
    builder.append(" ");
  }

  /**
   * Method encapsulates object, then adds object to the builder. Note the object cannot be null.
   *
   * @param builder StringBuilder
   * @param object  to add
   * @param type    type of the object
   */
  private void addObjectToBuilder(@NotNull StringBuilder builder, @NotNull String object, @NotNull ObjectTypes type) {
    builder.append(encapsulateObject(object, type));
  }

  /**
   * Method adds a list of objects to the builder: puts commas between the literal and encapsulates the objects with "
   * or <  depending on the boolean literal. Note objects cannot be null and must at least contain one value.
   *
   * @param builder StringBuilder
   * @param objects List of objects to add
   * @param type    type of the objects (Literals, resources or objects)
   */
  private void addObjectsToBuilder(@NotNull StringBuilder builder, @NotNull List<String> objects,
    @NotNull ObjectTypes type) {
    if (objects.size() == 0) {
      throw new IllegalArgumentException();
    }

    for (String s : objects) {
      if (objects.indexOf(s) != 0) {
        builder.append(" , ");
      }
      builder.append(encapsulateObject(s, type));
    }
  }

  /**
   * Enumeration to describe the kind of the object.
   */
  private enum ObjectTypes {
    OBJECT, LITERAL, RESOURCE
  }

  /**
   * Encapsulates an object:
   * </br>
   * Literals are encapsulated with double quotation marks (") or three double quotation marks (""") for literals which
   * contain CR or LF.
   * </br>
   * Resources are encapsulated with less than (<) and greater than (>) symbols.
   * </br>
   * Objects are encapsulated with left bracket ([) and right bracket (]).
   *
   * @param object string to encapsulate
   * @param type   type of the object
   */
  private String encapsulateObject(@NotNull String object, @NotNull ObjectTypes type) {
    String ret = "";
    switch (type) {
      case LITERAL:
        if (object.contains("\r") || object.contains("\n")) {
          ret += "\"\"\"";
        } else {
          ret += "\"";
        }
        break;
      case RESOURCE:
        ret += "<";
        break;
      case OBJECT:
        ret += "[";
        break;
    }
    ret += (ObjectTypes.LITERAL == type) ? escapeString(object) : object;
    switch (type) {
      case LITERAL:
        if (object.contains("\r") || object.contains("\n")) {
          ret += "\"\"\"";
        } else {
          ret += "\"";
        }
        break;
      case RESOURCE:
        ret += ">";
        break;
      case OBJECT:
        ret += "]";
        break;
    }
    return ret;
  }

  /**
   * Parse a Date to the ISO 8601 standard.
   *
   * @param dateStamp Date
   *
   * @return ISO8601 string representation for a date
   */
  private String parseToIsoDate(@NotNull Date dateStamp) {
    return dateFormat.format(dateStamp);
  }

  /**
   * This method ensures a string uses \-escape sequences where necessary (e.g. double quotation marks).
   * Escape sequence specified in <a href="http://www.w3.org/TeamSubmission/turtle/#sec-strings">Turtle specification</a>.
   *
   * @param s string
   *
   * @return escaped string
   */
  private String escapeString(String s) {
    return (s == null) ? null : s.replaceAll("\"","\\\\\"");
  }
}
