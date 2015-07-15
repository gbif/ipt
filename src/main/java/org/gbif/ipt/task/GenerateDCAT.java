package org.gbif.ipt.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Resource;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.metadata.eml.*;

/**
 * Class to generate a DCAT feed of the data
 */
@Singleton
public class GenerateDCAT {

    private AppConfig cfg;
    private RegistrationManager regMgr;
    private ResourceManager rscMgr;
    private static Map<String, String> prefixes;
    private Set<String> organisations;

    @Inject
    public GenerateDCAT(AppConfig cfg, RegistrationManager regMgr, ResourceManager rscMgr) {
        this.cfg = cfg;
        this.regMgr = regMgr;
        this.rscMgr = rscMgr;
    }

    public  String readingDCAT(){
        String DCATCatalog ="";
        String CurrentLine;
        BufferedReader br = null;
        for(Resource res : rscMgr.listPublishedPublicVersions()){
           try
           {
               String path = rscMgr.isDCATExisting(res.getShortname());
               if(path != null){
                br = new BufferedReader(new FileReader(path));
                while ((CurrentLine = br.readLine()) != null) {
                    DCATCatalog += CurrentLine + "\n";
                }
               }
               else {//TODO Create a DCAT feed for this dataset ?
                };
        }
        catch (FileNotFoundException e){
            System.out.println("404 : DCAT-DATASET file not found : "+ e);
        }catch(IOException e){
                System.out.println("error" + e);
            } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
       }
       System.out.println("CATALOG \n" + DCATCatalog);
        return DCATCatalog;
    }

    /**
     * Create the DCAT feed
     * The prefixes, Catalog, all Datasets, all Distributions and the organizations
     *
     * @return DCAT feed
     */
    public String getDCATFeed() {
        StringBuilder feed = new StringBuilder();
        return feed.toString();
    }

    /**
     * Create and regenerate the entire DCAT feed
     * The Prefixes, Catalog, all datasets, all Distributions and the organizations
     *
     * @return DCAT feed
     */
    public String createDCATFeed() {
        StringBuilder feed = new StringBuilder();
        organisations = new HashSet<String>();
        feed.append(createPrefixes());
        feed.append("\n");
        feed.append(createDCATCatalog());
        feed.append("\n");

        for (Resource res : rscMgr.list()) {
            feed.append(createDCATDataset(res));
            feed.append("\n");
            feed.append(createDCATDistribution(res));
            feed.append("\n");
        }

        for (String org : organisations) {
            feed.append(org);
            feed.append("\n");
        }
        return feed.toString();
    }

    /**
     * Create the DCAT information for one
     *
     * @return
     */
    public String createDCATDataset() {
        StringBuilder datasetBuilder = new StringBuilder();
        return datasetBuilder.toString();
    }

    /**
     * Create a new file and write the information in it
     *
     * @param path        the path of the file
     * @param information information to be put in the file
     */
    private void writeFile(String path, String information) {
        try {
            PrintWriter pwnew = new PrintWriter(new BufferedWriter(new FileWriter(new File(path))));
            pwnew.println(information);
            pwnew.close();
        } catch (IOException exception) {
            //System.out.println("Writing error " + exception.getMessage());
        }
    }

    /**
     * Write all the prefixes needed in a String representation
     *
     * @return String Prefixes
     */
    private String createPrefixes() {
        prefixes = new HashMap<String, String>();
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

        StringBuilder prefixBuilder = new StringBuilder();
        for (String pre : prefixes.keySet()) {
            prefixBuilder.append("@prefix ");
            prefixBuilder.append(pre);
            prefixBuilder.append(" <");
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
     * <li>dct:rights</li>
     * <li>dct:spatial</li>
     * </ul>
     * </p>
     *
     * @return String DCAT Catalog
     */
    private String createDCATCatalog() {

        StringBuilder catalogBuilder = new StringBuilder();
        Ipt ipt = regMgr.getIpt();

        //Run over resources
        List<String> uris = new ArrayList<String>();
        Date firstCreation = new Date();
        Date lastModification = new Date();
        for (Resource res : rscMgr.list()) {
            String uri = cfg.getResourceUrl(res.getShortname()) + "#dataset";
            uris.add(uri);
            if (res.getCreated().before(firstCreation)) {
                firstCreation = res.getCreated();
            }
            if (res.getLastPublished().after(lastModification)) {
                lastModification = res.getLastPublished();
            }
        }

        //Base
        String url = "localhost:8080#catalog";
        if (cfg != null) {
            url = cfg.getBaseUrl() + "#catalog";
        } else {
            Logger.getGlobal().info("Couldn't load catalog URL");
        }
        catalogBuilder.append(encapsulateObject(url, ObjectTypes.RESOURCE));
        catalogBuilder.append("\n");
        catalogBuilder.append(" a dcat:Catalog");

        //Mandatory
        //dct:title
        //System.out.println(registrationManager.getIpt().getName());
        //dct:description
        //dct:publisher
        //dcat:dataset

        //Recommended
        //foaf:homepage
        //dct:issued
        //dct:modified
        //dcat:themeTaxonomy

        //Optional
        //dct:rights
        //dct:spatial


        catalogBuilder.append(" .\n");
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
     * </ul>
     * </p>
     * For every publisher it comes along it creates a foaf:agent with the name of the organisation
     * This is then added to the set organisations, this is not added to this return method
     *
     * @param resource resource to create DCAT Dataset from
     * @return String DCAT Dataset for one resource
     */
    private String createDCATDataset(Resource resource) {

        StringBuilder datasetBuilder = new StringBuilder();
        Eml eml = resource.getEml();

        //Base
        String url = "localhost:8080#dataset";
        if (cfg != null) {
            url = cfg.getResourceUrl(resource.getShortname()) + "#dataset";
        } else {
            Logger.getGlobal().info("Couldn't load URL of the resource:" + resource.getShortname());
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
        addObjectToBuilder(datasetBuilder, "http://eurovoc.europa.eu/5463", ObjectTypes.RESOURCE);
        //adms:contactPoint
        for (Agent contact : eml.getContacts()) {
            addPredicateToBuilder(datasetBuilder, "adms:contactPoint");
            String agent = " a vcard:Kind ; vcard:fn \"" + contact.getFullName() + "\" ";
            if (contact.getEmail() != null) {
                agent += " ; vcard:hasEmail <mailto:" + contact.getEmail() + "> ";
            }
            addObjectToBuilder(datasetBuilder, agent, ObjectTypes.OBJECT);
        }
        //dct:issued
        addPredicateToBuilder(datasetBuilder, "dct:issued");
        addObjectToBuilder(datasetBuilder, parseToIsoDate(eml.getDateStamp()), ObjectTypes.LITERAL);
        //dct:modified
        addPredicateToBuilder(datasetBuilder, "dct:modified");
        addObjectToBuilder(datasetBuilder, parseToIsoDate(eml.getPubDate()), ObjectTypes.LITERAL);
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
            Logger.getGlobal().info("Couldn't load URL for landingPage of " + resource.getShortname());
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
            String organisation = encapsulateObject(publisher, ObjectTypes.RESOURCE) + " a foaf:Agent ; foaf:name \"" + resource.getOrganisation().getName() + "\" .";
            organisations.add(organisation);
            addObjectToBuilder(datasetBuilder, publisher, ObjectTypes.RESOURCE);
        }

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
     * </ul>
     *
     * @param resource resource to create he DCAT Distribution from
     * @return String DCAT Distribution for one resource
     */
    private String createDCATDistribution(Resource resource) {
        StringBuilder distributionBuilder = new StringBuilder();
        Eml eml = resource.getEml();

        //Base
        String url = "localhost:8080#distribution";
        if (cfg != null) {
            url = cfg.getResourceArchiveUrl(resource.getShortname());
        } else {
            Logger.getGlobal().info("Couldn't load URL of the distribution:" + resource.getShortname());
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
            Logger.getGlobal().info("Can't parse licenseURL for the distribution of " + resource.getShortname());
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
            Logger.getGlobal().info("Couldn't get downloadURL for the distribution of " + resource.getShortname());
        }

        distributionBuilder.append(" .\n");
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
        return df.format(new Date());
    }

}
