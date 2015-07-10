package org.gbif.ipt.task;

import org.gbif.ipt.model.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

/**
 * DCAT FILE EXAMPLE
 *
 *@prefix dcat: <http://www.w3.org/ns/dcat#> .
 *@prefix dc: <http://purl.org/dc/terms/> .
 *@prefix foaf: <http://xmlns.com/foaf/0.1/> .
 *<http://datatank4.gent.be/bestuurenbeleid/schepenen>
 *a dcat:Dataset ;
 *dc:title "bestuurenbeleid/schepenen" ;
 *dc:description """Schepenen van Stad Gent.
 *""" ;
 *dc:identifier "bestuurenbeleid/schepenen" ;
 *dc:issued "2015-01-15T13:05:42+0000" ;
 *dc:modified "2015-01-15T13:05:42+0000" ;
 *dcat:distribution <http://datatank4.gent.be/bestuurenbeleid/schepenen.json> .
 *
 */

public class GenerateDCAT {

   /* @Inject
    DataDir Directory;
    private  Resource resource;
    private  Eml eml;
    private  Organisation organisation;*/

    public void create(Resource resource){
        //resource.setShortname("Testing Resource");
        System.out.println("-----------------------------------\n"+"         Testing Resource\n"+"-----------------------------------\n"+ resource.toString());

        HashMap<String,String> prefix = new HashMap<String,String>();

        prefix.put("dct:","http://purl.org/dc/terms/");
        prefix.put("dcat:","http://www.w3.org/ns/dcat#");
        prefix.put("xsd:","http://www.w3.org/2001/XMLSchema#");
        prefix.put("skos:","http://www.w3.org/2004/02/skos/core#");
        prefix.put("rdfs:","http://www.w3.org/2000/01/rdf-schema#");
        prefix.put("foaf:","http://xmlns.com/foaf/0.1/");
        prefix.put("schema:","http://schema.org/");
        prefix.put("adms:","http://www.w3.org/ns/adms#");

        ArrayList<String> txt = readXmlFile();
        ArrayList<String> finalTxt = hasMapToStringArray(prefix);
        finalTxt.addAll(txt);

        writeFile(finalTxt);
    }

    /**
     * Convert the prefix Hashmap to a array list
     * @param prefix
     * @return
     */
    private static ArrayList<String> hasMapToStringArray(HashMap<String,String> prefix){
        Object [] keyArray = prefix.keySet().toArray();
        String[] strArray = new String[keyArray.length];
        for (int i = 0; i < keyArray.length; i++)
        {
            strArray[i] = new String((String) keyArray[i]);
        }
        ArrayList<String> text = new ArrayList<String>();
        for (String key : strArray){
            String link = prefix.get(key);
            String line = "@prefix "+key+link;
            text.add(line);
        }
        return text;
    }

    /**
     * Write a new file with information in it
     *  @param informations
     */
    private static void writeFile(ArrayList<String> informations ) {
        File fnew = new File ("/home/sylvain/Documents/datadir/dcat.txt");//Need to be fix HARDCODING
        try
        {
            PrintWriter pwnew = new PrintWriter (new BufferedWriter (new FileWriter (fnew)));
            for(String info: informations){
                pwnew.println(info);
            }
            pwnew.close();
        }
        catch (IOException exception)
        {
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
                   printNode(doc.getChildNodes(),"",txt);
               }
           } catch (Exception e) {
               System.out.println(e.getMessage());
           }
           return txt;
       }

    /**
     * Recursive method for tracking/printing everyNode
     * @param nodeList
     */
    private static void printNode(NodeList nodeList,String position,ArrayList<String> txt) {

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
                    if(tempNode.getChildNodes().getLength() == 1){
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

    private String createCatalog(){
        //Mandatory
        //dct:title
        //dct:description
        //dct:publisher
        //dcat:dataset
        //foaf:homepage
        //dct:rights
        //dct:issued
        //dct:modified
        //dcat:themeTaxonomy
        //dct:spatial


        return new String();
    }

    private String createDataset(){
        //dct:title
        //dct:description
        //dcat:theme
        //dcat:keyword
        //dct:issued
        //dct:modified
        //dct:isVersionOf
        //dct:spatial
        //adms:versionInfo
        //adms:versionNotes
        //adms:contactPoint
        //dcat:landingPage


        return new String();
    }

    private String createDistribution(){
        String plop ="";
        //dct:description
        //dct:license
        //dcat:mediaType
        //dct:format
        //dcat:downloadURL


        return new String();
    }
}
