package org.gbif.ipt.task;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;

import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.utils.InputStreamUtils;
import org.gbif.metadata.eml.*;

/**
 * Class to generate a DCAT feed of the data
 * Only resources that are published and public will be in the DCAT feed
 * <p>
 * Project homepage: https://github.com/oSoc15/ipt-dcat
 * Fork: https://github.com/oSoc15/ipt
 *
 * @author Simon Van Cauter, Sylvain Delbauve
 */
@Singleton
public class GenerateDCAT {

    private static final Logger LOG = Logger.getLogger(GenerateDCAT.class);
    /**
     * URL used when no URL is defined for the object
     */
    private static final String DUMMY_URL = "localhost:8080";
    /**
     * Rights of the catalog
     */
    private static final String CATALOG_RIGHTS = "https://creativecommons.org/publicdomain/zero/1.0/";
    /**
     * Title of the theme of the catalog
     */
    private static final String CATALOG_THEME_TITLE = "biodiversity";
    /**
     * Toxonomy URI
     */
    private static final String THEME_TAXONOMY_URI = "http://eurovoc.europa.eu/218403";
    /**
     * Label for the theme of the datatset
     */
    private static final String DATASET_THEME_LABEL = "biodiversity";
    /**
     * Theme of the dataset
     */
    private static final String THEME_URI = "http://eurovoc.europa.eu/5463";
    /**
     * Location of the prefixes
     */
    private static final String PREFIXES_PROPERTIES = "org/gbif/metadata/eml/dcat.properties";
    private static final String LANGUAGE_LINK = "http://id.loc.gov/vocabulary/iso639-1/";

    private String DCAT = "";
    private long time = 0;
    /**
     * Time the DCAT feed is kept the same
     * If the DCAT feed exist longer then the cashing time, the feed is regenerated
     */
    private static final long cachingTime = 60000;

    private AppConfig cfg;
    private RegistrationManager regMgr;
    private ResourceManager rscMgr;

    @Inject
    public GenerateDCAT(AppConfig cfg, RegistrationManager regMgr, ResourceManager rscMgr) {
        this.cfg = cfg;
        this.regMgr = regMgr;
        this.rscMgr = rscMgr;
    }

    /**
     * Return the DCAT feed or update to a new one,
     * if it exceeds the caching time
     *
     * @return DCAT feed
     */
    public String getDCAT() {
        long now = System.currentTimeMillis();
        if (time <= (now - cachingTime) || cfg.devMode()) {
            LOG.info("Updating DCAT feed");
            time = now;
            DCAT = createDCATFeed();
        }
        return DCAT;
    }

    /**
     * Create and regenerate the entire DCAT feed
     * The Prefixes, Catalog, all datasets, all Distributions and the organizations
     *
     * @return DCAT feed
     */
    private String createDCATFeed() {
        StringBuilder feed = new StringBuilder();
        Set<String> organisations = new HashSet<String>();
        Set<String> themes = new HashSet<String>();

        //Prefixes
        feed.append(createPrefixesInformation());
        feed.append("\n");
        //Catalog
        feed.append(createDCATCatalogInformation());
        feed.append("\n");

        //add organisation of Catalog
        Organisation org = regMgr.getHostingOrganisation();
        String publisher = "http://www.gbif.org/publisher/" + org.getKey() + "#Organization";
        String organisation = encapsulateObject(publisher, ObjectTypes.RESOURCE) + " a foaf:Agent ; foaf:name \"" + org.getName() + "\" .";
        organisations.add(organisation);

        //Datasets and Distributions
        for (Resource res : rscMgr.list(PublicationStatus.REGISTERED)) {
            feed.append(createDCATDatasetInformation(res));
            feed.append("\n");
            feed.append(createDCATDistributionInformation(res));
            feed.append("\n");

            //add Organisation of Dataset
            publisher = "http://www.gbif.org/publisher/" + res.getOrganisation().getKey() + "#Organization";
            organisation = encapsulateObject(publisher, ObjectTypes.RESOURCE) + " a foaf:Agent ; foaf:name \"" + res.getOrganisation().getName() + "\" .";
            organisations.add(organisation);
            //add Themes of datasets
            themes.add(encapsulateObject(THEME_URI, ObjectTypes.RESOURCE) + " a skos:Concept ; skos:prefLabel \"" + DATASET_THEME_LABEL + "\"@en ; skos:inScheme <" + THEME_TAXONOMY_URI + "> .");
        }

        //Organisations
        for (String orgs : organisations) {
            feed.append(orgs);
            feed.append("\n");
        }
        feed.append("\n");

        //Themes
        for (String theme : themes) {
            feed.append(theme);
            feed.append("\n");
        }

        if (rscMgr.listPublishedPublicVersions().size() > 0) {
        } else {
            feed.append("\n#No published resources, a valid DCAT feed needs at least one dataset\n");
        }

        return feed.toString();
    }

    /**
     * Create the DCAT information for one resource
     *
     * @return String
     */
    public String createDCATResource(Resource resource) {
        StringBuilder datasetBuilder = new StringBuilder();
        datasetBuilder.append(createDCATDatasetInformation(resource));
        datasetBuilder.append("\n");
        datasetBuilder.append(createDCATDistributionInformation(resource));
        datasetBuilder.append("\n");
        return datasetBuilder.toString();
    }

    /**
     * Write all the prefixes needed in a String representation
     * Prefixes are hardcoded in this method
     *
     * @return String Prefixes
     */
    @VisibleForTesting
    protected String createPrefixesInformation() {
        StringBuilder prefixBuilder = new StringBuilder();
        HashMap<String, String> prefixes = new HashMap<String, String>();
        InputStreamUtils streamUtils = new InputStreamUtils();
        InputStream configStream = streamUtils.classpathStream(PREFIXES_PROPERTIES);
        try {
            Properties props = new Properties();
            if (configStream == null) {
                LOG.error("Could not load prefixes");
                /*
                prefixes.put("dct:", "http://purl.org/dc/terms/");
                prefixes.put("dcat:", "http://www.w3.org/ns/dcat#");
                prefixes.put("xsd:", "http://www.w3.org/2001/XMLSchema#");
                prefixes.put("skos:", "http://www.w3.org/2004/02/skos/core#");
                prefixes.put("rdfs:", "http://www.w3.org/2000/01/rdf-schema#");
                prefixes.put("foaf:", "http://xmlns.com/foaf/0.1/");
                prefixes.put("schema:", "http://schema.org/");
                prefixes.put("adms:", "http://www.w3.org/ns/adms#");
                prefixes.put("locn:", "http://www.w3.org/ns/locn#");
                prefixes.put("vcard:", "http://www.w3.org/2006/vcard/ns#");
                */
            } else {
                props.load(configStream);
                for (String pre : props.stringPropertyNames()) {
                    prefixes.put(pre, props.getProperty(pre));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
     * Create the DCAT feed for the Catalog
     * <p>
     * Implemented
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
    @VisibleForTesting
    protected String createDCATCatalogInformation() {

        StringBuilder catalogBuilder = new StringBuilder();
        List<String> themeTaxonomies = new ArrayList<String>();
        Ipt ipt = regMgr.getIpt();
        Organisation org = regMgr.getHostingOrganisation();

        //Run over resources
        List<String> uris = new ArrayList<String>();
        Date firstCreation = new Date();
        boolean firstPublishedDatePresent = false;
        Date lastModification = new Date(0);
        boolean lastPublishedDatePresent = false;
        for (Resource res : rscMgr.list(PublicationStatus.REGISTERED)) {
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

        //Base
        String url = DUMMY_URL;
        if (cfg != null) {
            url = cfg.getBaseUrl();
        } else {
            LOG.info("Couldn't load catalog URL, using dummy");
        }
        url += "#Catalog";
        catalogBuilder.append(encapsulateObject(url, ObjectTypes.RESOURCE));
        catalogBuilder.append("\n");
        catalogBuilder.append(" a dcat:Catalog");

        //dct:title
        if (ipt != null) {
            addPredicateToBuilder(catalogBuilder, "dct:title");
            addObjectToBuilder(catalogBuilder, org.getName(), ObjectTypes.LITERAL);
        } else {
            LOG.error("IPT is null, can't get title");
        }
        //dct:description
        if (ipt != null) {
            addPredicateToBuilder(catalogBuilder, "dct:description");
            addObjectToBuilder(catalogBuilder, org.getDescription(), ObjectTypes.LITERAL);
        } else {
            LOG.error("IPT is null, can't get description");
        }
        //dct:publisher
        if (ipt != null && ipt.getKey() != null) {
            addPredicateToBuilder(catalogBuilder, "dct:publisher");
            String publisher = "http://www.gbif.org/publisher/" + org.getKey() + "#Organization";
            addObjectToBuilder(catalogBuilder, publisher, ObjectTypes.RESOURCE);
        }
        //dcat:dataset
        if (!uris.isEmpty()) {
            addPredicateToBuilder(catalogBuilder, "dcat:dataset");
            addObjectsToBuilder(catalogBuilder, uris, ObjectTypes.RESOURCE);
        }
        //foaf:homepage
        if (ipt != null && ipt.getHomepageURL() != null) {
            addPredicateToBuilder(catalogBuilder, "foaf:homepage");
            addObjectToBuilder(catalogBuilder, org.getHomepageURL(), ObjectTypes.RESOURCE);
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
        addPredicateToBuilder(catalogBuilder, "dcat:themeTaxonomy");
        String themeTaxonomy = THEME_TAXONOMY_URI;
        themeTaxonomies.add(encapsulateObject(THEME_TAXONOMY_URI, ObjectTypes.RESOURCE) + " a skos:ConceptScheme ; dct:title \"" + CATALOG_THEME_TITLE + "\"@en .");
        addObjectToBuilder(catalogBuilder, themeTaxonomy, ObjectTypes.RESOURCE);
        //dct:rights
        addPredicateToBuilder(catalogBuilder, "dct:license");
        addObjectToBuilder(catalogBuilder, CATALOG_RIGHTS, ObjectTypes.RESOURCE);
        //dct:spatial
        if (cfg.getLongitude() != null && cfg.getLatitude() != null) {
            addPredicateToBuilder(catalogBuilder, "dct:spatial");
            String spatial = " a dct:Location ; locn:geometry \"" + "{ \\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": [ "
                    + cfg.getLongitude() + "," + cfg.getLatitude()
                    + " ] }\" ";
            addObjectToBuilder(catalogBuilder, spatial, ObjectTypes.OBJECT);
        } else {
            LOG.info("No spatial data defined for the IPT");
        }
        //dct:language
        addPredicateToBuilder(catalogBuilder, "dct:language");
        addObjectToBuilder(catalogBuilder, LANGUAGE_LINK + "en", ObjectTypes.RESOURCE);

        catalogBuilder.append(" .\n");
        catalogBuilder.append("\n");
        for (String tax : themeTaxonomies) {
            catalogBuilder.append(tax);
            catalogBuilder.append("\n");
        }
        return catalogBuilder.toString();
    }

    /**
     * Create a DCAT Dataset of one resource
     * <p>
     * Implmented:
     * <ul>
     * <li>dct:title</li>
     * <li>dct:description</li>
     * <li>dcat:keyword</li>
     * <li>dcat:theme</li>
     * <li>adms:contactPoint</li>
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
     * For every publisher it comes along it creates a foaf:agent with the name of the organisation
     * This is then added to the set organisations, this is not added to this return method
     *
     * @param resource resource to create DCAT Dataset from
     * @return String DCAT Dataset for one resource
     */
    @VisibleForTesting
    protected String createDCATDatasetInformation(Resource resource) {

        StringBuilder datasetBuilder = new StringBuilder();
        Eml eml = resource.getEml();

        //Base
        String url = "localhost:8080#Dataset";
        if (cfg != null) {
            url = cfg.getResourceUrl(resource.getShortname()) + "#Dataset";
        } else {
            LOG.info("Couldn't load URL of the resource:" + resource.getShortname());
        }
        datasetBuilder.append(encapsulateObject(url, ObjectTypes.RESOURCE));
        datasetBuilder.append("\n");
        datasetBuilder.append("a dcat:Dataset");

        //dct:title
        addPredicateToBuilder(datasetBuilder, "dct:title");
        addObjectToBuilder(datasetBuilder, eml.getTitle(), ObjectTypes.LITERAL);
        //dct:description
        addPredicateToBuilder(datasetBuilder, "dct:description");
        StringBuilder description = new StringBuilder();
        for (String des : eml.getDescription()) {
            description.append(des);
            description.append("\n");
        }
        description.deleteCharAt(description.length() - 1);
        addObjectToBuilder(datasetBuilder, description.toString(), ObjectTypes.LITERAL);
        //dcat:keyword
        for (KeywordSet key : eml.getKeywords()) {
            addPredicateToBuilder(datasetBuilder, "dcat:keyword");
            addObjectsToBuilder(datasetBuilder, key.getKeywords(), ObjectTypes.LITERAL);
        }
        //dcat:theme
        addPredicateToBuilder(datasetBuilder, "dcat:theme");
        String theme = THEME_URI;
        addObjectToBuilder(datasetBuilder, theme, ObjectTypes.RESOURCE);
        //adms:contactPoint
        for (Agent contact : eml.getContacts()) {
            addPredicateToBuilder(datasetBuilder, "dcat:contactPoint");
            String agent = " a vcard:Individual ; vcard:fn \"" + contact.getFullName() + "\" ";
            if (contact.getEmail() != null) {
                agent += " ; vcard:hasEmail <mailto:" + contact.getEmail() + "> ";
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
        //dct:spatial
        //Uses geoJSON to represent the geospatial coverage
        //This can easily be verified at http://geojsonlint.com/
        for (GeospatialCoverage geospac : eml.getGeospatialCoverages()) {
            BBox bb = geospac.getBoundingCoordinates();
            addPredicateToBuilder(datasetBuilder, "dct:spatial");
            String spatial = " a dct:Location ; locn:geometry \"" + "{ \\\"type\\\": \\\"Polygon\\\", \\\"coordinates\\\": [ [ ["
                    + bb.getMin().getLongitude() + "," + bb.getMin().getLatitude()
                    + "], [" + bb.getMin().getLongitude() + "," + bb.getMax().getLatitude()
                    + "], [" + bb.getMax().getLongitude() + "," + bb.getMax().getLatitude()
                    + "], [" + bb.getMax().getLongitude() + "," + bb.getMin().getLatitude()
                    + "], [" + bb.getMin().getLongitude() + "," + bb.getMin().getLatitude()
                    + "] ] ] }" + "\" ";
            addObjectToBuilder(datasetBuilder, spatial, ObjectTypes.OBJECT);
        }
        //adms:versionInfo
        if (resource.getLastPublishedVersionsVersion() != null) {
            addPredicateToBuilder(datasetBuilder, "adms:versionInfo");
            addObjectToBuilder(datasetBuilder, resource.getLastPublishedVersionsVersion().toString(), ObjectTypes.LITERAL);
        }
        //adms:versionNotes
        if (resource.getLastPublishedVersionsChangeSummary() != null) {
            addPredicateToBuilder(datasetBuilder, "adms:versionNotes");
            addObjectToBuilder(datasetBuilder, resource.getLastPublishedVersionsChangeSummary(), ObjectTypes.LITERAL);
        }
        //dcat:landingPage
        String landingPage = "localhost:8080";
        if (cfg != null) {
            landingPage = cfg.getResourceUrl(resource.getShortname());
        } else {
            LOG.info("Couldn't load URL for landingPage of " + resource.getShortname());
        }
        addPredicateToBuilder(datasetBuilder, "dcat:landingPage");
        addObjectToBuilder(datasetBuilder, landingPage, ObjectTypes.RESOURCE);
        //foaf:homepage
        if (eml.getHomepageUrl() != null) {
            addPredicateToBuilder(datasetBuilder, "foaf:homepage");
            addObjectToBuilder(datasetBuilder, eml.getHomepageUrl(), ObjectTypes.RESOURCE);
        }
        //dct:identifier
        if (resource.getKey() != null) {
            addPredicateToBuilder(datasetBuilder, "dct:identifier");
            addObjectToBuilder(datasetBuilder, "http://www.gbif.org/dataset/" + resource.getKey(), ObjectTypes.LITERAL);
        }
        //dct:publisher
        if (resource.getOrganisation() != null && resource.getOrganisation().getKey() != null) {
            addPredicateToBuilder(datasetBuilder, "dct:publisher");
            String publisher = "http://www.gbif.org/publisher/" + resource.getOrganisation().getKey() + "#Organization";
            addObjectToBuilder(datasetBuilder, publisher, ObjectTypes.RESOURCE);
        }
        //dcat:Distribution
        if (cfg != null) {
            addPredicateToBuilder(datasetBuilder, "dcat:distribution");
            String dist = cfg.getResourceArchiveUrl(resource.getShortname());
            addObjectToBuilder(datasetBuilder, dist, ObjectTypes.RESOURCE);
        }
        //dct:language
        addPredicateToBuilder(datasetBuilder, "dct:language");
        Locale loc = new Locale(eml.getLanguage());
        addObjectToBuilder(datasetBuilder, LANGUAGE_LINK + loc.toLanguageTag(), ObjectTypes.RESOURCE);

        datasetBuilder.append(" .\n");
        return datasetBuilder.toString();
    }

    /**
     * Create a DCAT Distribution of one resoure
     * <p>
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
     * @param resource resource to create he DCAT Distribution from
     * @return String DCAT Distribution for one resource
     */
    @VisibleForTesting
    protected String createDCATDistributionInformation(Resource resource) {
        StringBuilder distributionBuilder = new StringBuilder();
        Eml eml = resource.getEml();

        //Base
        String url = "localhost:8080#Distribution";
        if (cfg != null) {
            url = cfg.getResourceArchiveUrl(resource.getShortname());
        } else {
            LOG.info("Couldn't load URL of the distribution:" + resource.getShortname());
        }
        distributionBuilder.append(encapsulateObject(url, ObjectTypes.RESOURCE));
        distributionBuilder.append("\n");
        distributionBuilder.append("a dcat:Distribution");
        //dct:description
        addPredicateToBuilder(distributionBuilder, "dct:description");
        addObjectToBuilder(distributionBuilder, "Darwin Core Archive", ObjectTypes.LITERAL);
        //dct:license
        if (eml.parseLicenseUrl() != null) {
            addPredicateToBuilder(distributionBuilder, "dct:license");
            addObjectToBuilder(distributionBuilder, eml.parseLicenseUrl(), ObjectTypes.RESOURCE);
        } else {
            LOG.info("Can't parse licenseURL for the distribution of " + resource.getShortname());
        }
        //dct:format
        addPredicateToBuilder(distributionBuilder, "dct:format");
        addObjectToBuilder(distributionBuilder, "dwc-a", ObjectTypes.LITERAL);
        //dcat:mediaType
        addPredicateToBuilder(distributionBuilder, "dcat:mediaType");
        addObjectToBuilder(distributionBuilder, "application/zip", ObjectTypes.LITERAL);
        //dcat:downloadURL
        if (cfg != null) {
            addPredicateToBuilder(distributionBuilder, "dcat:downloadURL");
            addObjectToBuilder(distributionBuilder, cfg.getResourceArchiveUrl(resource.getShortname()), ObjectTypes.RESOURCE);
        } else {
            LOG.info("Couldn't get downloadURL for the distribution of " + resource.getShortname());
        }
        //dcat:accessURL
        String accessURLClass = null;
        if (cfg != null) {
            addPredicateToBuilder(distributionBuilder, "dcat:accessURL");
            accessURLClass = encapsulateObject(cfg.getResourceUrl(resource.getShortname()), ObjectTypes.RESOURCE) + " a rdfs:Resource .";
            addObjectToBuilder(distributionBuilder, cfg.getResourceUrl(resource.getShortname()), ObjectTypes.RESOURCE);
        } else {
            LOG.error("No accessURL available for" + resource.getShortname());
        }

        distributionBuilder.append(" .\n");
        if (accessURLClass != null) {
            distributionBuilder.append(accessURLClass + "\n");
        }
        return distributionBuilder.toString();
    }

    /**
     * Method to add the predicate to the builder in turtle syntax
     * Ends the previous predicate with a ; and a newline
     *
     * @param builder   builder for the String
     * @param predicate Relation between the subject and object
     */
    private static void addPredicateToBuilder(StringBuilder builder, String predicate) {
        builder.append(" ;\n");
        builder.append(predicate);
        builder.append(" ");
    }

    /**
     * Add objects to the builder
     * Encapsulates the object
     * Objects cannot be null and must at least contain one value
     *
     * @param builder StringBuilder
     * @param object  Object to add
     * @param type    type of the object (literal, resource or an object)
     */
    private static void addObjectToBuilder(StringBuilder builder, String object, ObjectTypes type) {
        builder.append(encapsulateObject(object, type));
    }

    /**
     * Add a list of objects to the builder
     * Puts commas between the literal and encapsulates the objects with " or <  depending on the boolean literal
     * Objects cannot be null and must at least contain one value
     *
     * @param builder StringBuilder
     * @param objects List of objects to add
     * @param type    type of the objects (Literals, resources or objects)
     */
    private static void addObjectsToBuilder(StringBuilder builder, List<String> objects, ObjectTypes type) {
        for (String s : objects) {
            if (objects.indexOf(s) != 0) {
                builder.append(" , ");
            }
            builder.append(encapsulateObject(s, type));
        }
    }

    /**
     * Enumeration to describe the kind of the object
     */
    private enum ObjectTypes {
        OBJECT, LITERAL, RESOURCE
    }

    /**
     * Encapsulates an object
     * Literals are encapsulated with "
     * Resources are encapsulated with < and >
     * Objects are encapsulated wth [ and ]
     *
     * @param object string to encapsulate
     * @param type   type of the object
     */
    private static String encapsulateObject(String object, ObjectTypes type) {
        String ret = "";
        switch (type) {
            case LITERAL:
                ret += "\"";
                break;
            case RESOURCE:
                ret += "<";
                break;
            case OBJECT:
                ret += "[";
                break;
        }
        ret += object;
        switch (type) {
            case LITERAL:
                ret += "\"";
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
     * JAVA 8 SUPPORTS THIS ALREADY
     * THIS METHOD WON'T BE NEEDED
     * Parse a Date object to the ISO8601 standard
     *
     * @param dateStamp Date object
     * @return ISO8601 string representation for a date
     */
    private static String parseToIsoDate(Date dateStamp) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX");
        if (dateStamp == null) {
            LOG.error("Date not defined");
        }
        return df.format(dateStamp);
    }

}
