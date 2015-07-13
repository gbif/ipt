package org.gbif.ipt.task;

import com.google.inject.Inject;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.KeywordSet;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

/**
 * DCAT FILE EXAMPLE
 *
 * @prefix dcat: <http://www.w3.org/ns/dcat#> .
 * @prefix dc: <http://purl.org/dc/terms/> .
 * @prefix foaf: <http://xmlns.com/foaf/0.1/> .
 * <http://datatank4.gent.be/bestuurenbeleid/schepenen>
 * a dcat:Dataset ;
 * dc:title "bestuurenbeleid/schepenen" ;
 * dc:description """Schepenen van Stad Gent.
 * """ ;
 * dc:identifier "bestuurenbeleid/schepenen" ;
 * dc:issued "2015-01-15T13:05:42+0000" ;
 * dc:modified "2015-01-15T13:05:42+0000" ;
 * dcat:distribution <http://datatank4.gent.be/bestuurenbeleid/schepenen.json> .
 */

public class GenerateDCAT {

    @Inject
    DataDir directory;

    public GenerateDCAT() {

    }

    public void create(Resource resource) {
        //resource.setShortname("Testing Resource");
        System.out.println("-----------------------------------\n" + "         Testing Resource\n" + "-----------------------------------\n" + resource.toString());

        HashMap<String, String> prefix = new HashMap<String, String>();

        prefix.put("dct:", "http://purl.org/dc/terms/");
        prefix.put("dcat:", "http://www.w3.org/ns/dcat#");
        prefix.put("xsd:", "http://www.w3.org/2001/XMLSchema#");
        prefix.put("skos:", "http://www.w3.org/2004/02/skos/core#");
        prefix.put("rdfs:", "http://www.w3.org/2000/01/rdf-schema#");
        prefix.put("foaf:", "http://xmlns.com/foaf/0.1/");
        prefix.put("schema:", "http://schema.org/");
        prefix.put("adms:", "http://www.w3.org/ns/adms#");

        ArrayList<String> txt = readXmlFile();
        ArrayList<String> finalTxt = hasMapToStringArray(prefix);
        finalTxt.addAll(txt);

        writeFile(finalTxt);
    }

    /**
     * Convert the prefix Hashmap to a array list
     *
     * @param prefix
     * @return
     */
    private static ArrayList<String> hasMapToStringArray(HashMap<String, String> prefix) {
        Object[] keyArray = prefix.keySet().toArray();
        String[] strArray = new String[keyArray.length];
        for (int i = 0; i < keyArray.length; i++) {
            strArray[i] = new String((String) keyArray[i]);
        }
        ArrayList<String> text = new ArrayList<String>();
        for (String key : strArray) {
            String link = prefix.get(key);
            String line = "@prefix " + key + link;
            text.add(line);
        }
        return text;
    }

    /**
     * Write a new file with information in it
     *
     * @param informations
     */
    private static void writeFile(ArrayList<String> informations) {
        File fnew = new File("/home/sylvain/Documents/datadir/dcat.txt");//Need to be fix HARDCODING
        try {
            PrintWriter pwnew = new PrintWriter(new BufferedWriter(new FileWriter(fnew)));
            for (String info : informations) {
                pwnew.println(info);
            }
            pwnew.close();
        } catch (IOException exception) {
            System.out.println("Writing error " + exception.getMessage());
        }
    }

    private static ArrayList<String> readXmlFile() {
        ArrayList<String> txt = new ArrayList<String>();
        try {

            File file = new File("/home/sylvain/Documents/datadir/coccinellidae/eml.xml");//Need to be fix HARDOCING
            //  File file = new File("/home/sylvain/Documents/datadir/resources/ResTest1/eml.xml");//Need to be fix HARDOCING
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document doc = dBuilder.parse(file);
            if (doc.hasChildNodes()) {
                printNode(doc.getChildNodes(), "", txt);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return txt;
    }

    /**
     * Recursive method for tracking/printing everyNode
     *
     * @param nodeList
     */
    private static void printNode(NodeList nodeList, String position, ArrayList<String> txt) {

        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                position += "-";
                System.out.println(position + "[OPEN]" + tempNode.getNodeName());
                txt.add(position + "[OPEN]" + tempNode.getNodeName());
                if (tempNode.hasAttributes()) {
                    NamedNodeMap nodeMap = tempNode.getAttributes();

                    for (int i = 0; i < nodeMap.getLength(); i++) {
                        Node node = nodeMap.item(i);
                        /*System.out.println("attr name : " + node.getNodeName());
                        System.out.println("attr value : " + node.getNodeValue());*/
                    }
                }
                //Recursive loop
                if (tempNode.hasChildNodes()) {
                    if (tempNode.getChildNodes().getLength() == 1) {
                        System.out.println(tempNode.getTextContent());
                        txt.add(tempNode.getTextContent());
                    }
                    printNode(tempNode.getChildNodes(), position, txt);
                }
                System.out.println(position + "[CLOSE]" + tempNode.getNodeName());
                txt.add(position + "[CLOSE]" + tempNode.getNodeName());
            }
        }
    }

    private String createCatalog() {
        //Mandatory
        //dct:title
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


        return new String();
    }

    /**
     * Create the DCAT feed for one dataset
     *
     * @param resource the resource to create the DCAT feed for
     * @return string representation of DCAT feed
     */
    public String createDCATDataset(Resource resource) {

        StringBuilder datasetBuilder = new StringBuilder();
        Eml eml = resource.getEml();

        //Base
        datasetBuilder.append(":");
        datasetBuilder.append(eml.getHomepageUrl() + "/#dataset" + "\n");
        datasetBuilder.append("a dcat:Dataset");


        //----------------------------------
        //Mandatory
        //dct:title
        addPredicateToBuilder(datasetBuilder, "dct:title");
        addObjectToBuilder(datasetBuilder, eml.getTitle(), true);
        //dct:description
        addPredicateToBuilder(datasetBuilder, "dct:description");
        String description = "";
        for (String des : eml.getDescription()) {
            description += des;
            if (eml.getDescription().indexOf(des) != eml.getDescription().size() - 1) {
                description += "\n";
            }
        }
        addObjectToBuilder(datasetBuilder, description, true);

        //---------------------------------
        //Recommended
        //dcat:keyword
        for (KeywordSet key : eml.getKeywords()) {
            addPredicateToBuilder(datasetBuilder, "dcat:keyword");
            addObjectsToBuilder(datasetBuilder, key.getKeywords(), true);
        }

        //dcat:theme
        //TODO
        String theme = "";
        //adms:contactPoint
        //TODO
        String contactPoint = "";

        //----------------------------------
        //Optional
        //dct:issued
        addPredicateToBuilder(datasetBuilder, "dct:issued");
        addObjectToBuilder(datasetBuilder, parseToIsoDate(eml.getDateStamp()), true);
        //dct:modified
        addPredicateToBuilder(datasetBuilder, "dct:modified");
        addObjectToBuilder(datasetBuilder, parseToIsoDate(eml.getPubDate()), true);
        //dct:isVersionOf
        //TODO
        //dct:spatial
        //addStringtoBuilder(datasetBuilder, "dct:spatial", resource.getEml().getGeospatialCoverages().toString());
        //adms:versionInfo
        //adms:versionNotes
        //addStringtoBuilder(datasetBuilder, "adms:versionNotes", resource.getEml().getEmlVersion().toString());
        //dcat:landingPage

        return datasetBuilder.toString();
    }

    /**
     * JAVA 8 SUPPORTS THIS ALREADY
     * THIS METHOD WON'T BE NEEDED
     * Parse a Date object to the ISO8601 standard
     *
     * @param dateStamp Date object
     * @return ISO8601 string representation for a date
     */
    public static String parseToIsoDate(Date dateStamp) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX");
        return df.format(new Date());
    }

    private String createDistribution() {
        String dist = "";

        //Recommended
        //dct:description

        //Optional
        //dct:license
        //dcat:mediaType
        //dct:format
        //dcat:downloadURL


        return new String();
    }

    /**
     * Method to add the predicate to the builder in turtle syntax
     * Ends the last predicate with a ; and a newline
     *
     * @param stringBuilder builder for the String
     * @param predicate     Realtion between the subject and object
     */
    private static void addPredicateToBuilder(StringBuilder stringBuilder, String predicate) {
        stringBuilder.append(";\n");
        stringBuilder.append(predicate);
        stringBuilder.append(" ");
    }

    /**
     * Add objects to the builder.
     * Puts commas between the literal and encapsulates the objects with " or <  depending on the boolean literal
     * Objects cannot be null and must at least contain one value
     *
     * @param builder StringBuilder
     * @param object  Object to add
     * @param literal Whether the object is a literal or a resoure
     *                A literal is encaplsulated with ", while a resource with '<'
     */
    private static void addObjectToBuilder(StringBuilder builder, String object, boolean literal) {
        builder.append(encapsulateObject(object, literal));
    }

    /**
     * Add a list of objects to the builder.
     * Puts commas between the literal and encapsulates the objects with " or <  depending on the boolean literal
     * Objects cannot be null and must at least contain one value
     *
     * @param builder StringBuilder
     * @param objects List of objects to add
     * @param literal Whether the objects are literals or resoures
     *                A literal is encaplsulated with ", while a resource with '<'
     */
    private static void addObjectsToBuilder(StringBuilder builder, List<String> objects, boolean literal) {
        for (String s : objects) {
            if (objects.indexOf(s) != 0) {
                builder.append(",");
            }
            builder.append(encapsulateObject(s, literal));
        }
    }

    /**
     * Encapsulates an object with " or < depending on the boolean literal
     * Literals are encapsulated with "
     * Resources are encapsulated with < and >
     *
     * @param object  string to encapsulate
     * @param literal if the object is a literal or a resource
     */
    private static String encapsulateObject(String object, boolean literal) {
        String ret = "";
        if (literal) {
            ret += "\"";
        } else {
            ret += "<";
        }
        ret += object;
        if (literal) {
            ret += "\"";
        } else {
            ret += ">";
        }
        return ret;
    }

}
