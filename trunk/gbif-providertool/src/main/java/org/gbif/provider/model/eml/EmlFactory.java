/*
 * Copyright 2010 GBIF.
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
package org.gbif.provider.model.eml;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.gbif.provider.model.Address;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.Resource;
import org.xml.sax.SAXException;

/**
 * This class is considered a utility for testing but should be migrated to the source
 * when stable, as this is an EML Model Factory based on the Apache Commons Digester and
 * will be used when importing DwC-A
 * 
 * @author Tim Robertson
 */
public class EmlFactory {

	/**
	 * Uses rule based parsing to read the EML XML and build the EML model
	 * 
	 * Note the following:
	 * - Metadata provider rules are omitted on the assumption that the provider is the same as the creator
	 * - Contact rules are omitted on the assumption that contacts are covered by the creator and associated parties
	 * - Publisher rules are omitted on the assumption the publisher is covered by the creator and associated parties
	 * 
	 * @param xml To read.  Note this will be closed before returning
	 * @return The EML populated
	 * @throws IOException If the Stream cannot be read from
	 * @throws SAXException If the XML is not well formed
	 */
	public static Eml build(InputStream xml) throws IOException, SAXException {
		Digester digester = new Digester();
		digester.setNamespaceAware(true);  

		// push the EML object onto the stack 
		Eml eml = new Eml();
		digester.push(eml);
		Resource r = new Resource();
		eml.setResource(r); // eml.resource is transient so we need one set up	      

		// add the rules 
		digester.addBeanPropertySetter("eml/dataset/alternateIdentifier", "guid");
		digester.addBeanPropertySetter("eml/dataset/title", "title");
		digester.addBeanPropertySetter("eml/dataset/language", "language");
		digester.addBeanPropertySetter("eml/dataset/abstract/para", "abstract");
		digester.addBeanPropertySetter("eml/dataset/additionalInfo/para", "additionalInfo");
		digester.addBeanPropertySetter("eml/dataset/intellectualRights/para", "intellectualRights");
		digester.addBeanPropertySetter("eml/dataset/purpose/para", "purpose");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/specimenPreservationMethod", "specimenPreservationMethod");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/metadataLanguage", "metadataLanguage");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/hierarchyLevel", "hierarchyLevel");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/collection/parentCollectionIdentifier", "parentCollectionId");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/collection/collectionIdentifier", "collectionId");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/collection/collectionName", "collectionName");

    digester.addCallMethod("eml/dataset/pubDate", "setPubDate", 1);
		digester.addCallParam("eml/dataset/pubDate", 0);
		
		addAgentRules(digester, "eml/dataset/creator", "setResourceCreator");
		addAgentRules(digester, "eml/dataset/metadataProvider", "setMetadataProvider");
		addAgentRules(digester, "eml/dataset/associatedParty", "addAssociatedParty");
		addKeywordRules(digester);
		addGeographicCoverageRules(digester);
		addTemporalCoverageRules(digester);
    addLivingTimePeriodRules(digester);
    addFormationPeriodRules(digester);
    addTaxonomicCoverageRules(digester);
		addMethodRules(digester);
		addProjectRules(digester);
		addPhysicalDataRules(digester);
		addJGTICuratorialIUnit(digester);

	  // rule to call "addCitation" on last stack object, with 1 param
		digester.addCallMethod("eml/additionalMetadata/metadata/citation", "addCitation", 1);
	  // set the parameter to pass in to the method as the citation content
	  digester.addCallParam("eml/additionalMetadata/metadata/citation", 0);

    // now parse and return the EML 
		try {
			digester.parse(xml);
		} finally {
			xml.close();
		}

		return eml;
	}
	
  /**
   * Add rules to extract the jgtiCuratorialUnit
   * @param digester to add the rules to
   */
  private static void addJGTICuratorialIUnit(Digester digester) {
    digester.addObjectCreate("eml/additionalMetadata/metadata/jgtiCuratorialUnit", JGTICuratorialUnit.class);
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/jgtiCuratorialUnit/jgtiUnitRange/beginRange", "rangeStart");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/jgtiCuratorialUnit/jgtiUnitRange/endRange", "rangeEnd");
    digester.addSetNext("eml/additionalMetadata/metadata/jgtiCuratorialUnit", "setJgtiCuratorialUnit"); // add the addJGTICuratorialIUnit to the list in EML    
  }
  
  /**
   * Add rules to extract the physicalData
   * @param digester to add the rules to
   */
  private static void addPhysicalDataRules(Digester digester) {
    digester.addObjectCreate("eml/additionalMetadata/metadata/physical", PhysicalData.class);
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/physical/objectName", "name");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/physical/characterEncoding", "charset");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/physical/dataFormat/externallyDefinedFormat/formatName", "format");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/physical/dataFormat/externallyDefinedFormat/formatVersion", "formatVersion");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/physical/distribution/online/url", "distributionUrl");
    digester.addSetNext("eml/additionalMetadata/metadata/physical", "addPhysicalData"); // add the PhysicalData to the list in EML    
  }
	

	/**
	 * Add rules for pulling the project details
	 * @param digester to add the rules to
	 */
	private static void addProjectRules(Digester digester) {
		digester.addObjectCreate("eml/dataset/project", Project.class);
		digester.addBeanPropertySetter("eml/dataset/project/title", "title");
		addAgentRules(digester, "eml/dataset/project/personnel", "setPersonnel");
		digester.addBeanPropertySetter("eml/dataset/project/abstract/para", "projectAbstract");
		digester.addBeanPropertySetter("eml/dataset/project/funding/para", "funding");
		// skipping the descriptor attributes 
		digester.addBeanPropertySetter("eml/dataset/project/studyAreaDescription/descriptor/descriptorValue", "studyAreaDescription");
		digester.addBeanPropertySetter("eml/dataset/project/designDescription/description", "designDescription");
		digester.addSetNext("eml/dataset/project", "setProject"); // add the Project to the list in EML
	}

	/**
	 * Adds rules to extract the taxonomic coverage 
	 * @param digester to add the rules to
	 */
	private static void addTaxonomicCoverageRules(Digester digester) {
		digester.addObjectCreate("eml/dataset/coverage/taxonomicCoverage", TaxonomicCoverage.class);
		digester.addBeanPropertySetter("eml/dataset/coverage/taxonomicCoverage/generalTaxonomicCoverage", "description");
		digester.addObjectCreate("eml/dataset/coverage/taxonomicCoverage/taxonomicClassification", TaxonKeyword.class);
		digester.addBeanPropertySetter("eml/dataset/coverage/taxonomicCoverage/taxonomicClassification/taxonRankName", "rank");
		digester.addBeanPropertySetter("eml/dataset/coverage/taxonomicCoverage/taxonomicClassification/taxonRankValue", "scientificName");
		digester.addBeanPropertySetter("eml/dataset/coverage/taxonomicCoverage/taxonomicClassification/commonName", "commonName");
		digester.addSetNext("eml/dataset/coverage/taxonomicCoverage/taxonomicClassification", "addTaxonKeyword"); // add the TaxonKeyword to the list in the coverage
		digester.addSetNext("eml/dataset/coverage/taxonomicCoverage", "addTaxonomicCoverage"); // add the TaxonomicCoverage to the list in EML
	}

	/**
	 * Adds rules to extract the temporal coverage
	 * @param digester to add the rules to
	 */
	private static void addTemporalCoverageRules(Digester digester) {
		digester.addObjectCreate("eml/dataset/coverage/temporalCoverage", TemporalCoverage.class);
		digester.addCallMethod("eml/dataset/coverage/temporalCoverage/singleDateTime/calendarDate", "setStart", 2);
		digester.addCallParam("eml/dataset/coverage/temporalCoverage/singleDateTime/calendarDate", 0);
		digester.addObjectParam("eml/dataset/coverage/temporalCoverage/singleDateTime/calendarDate", 1, "yyyy-MM-dd");
    digester.addCallMethod("eml/dataset/coverage/temporalCoverage/singleDateTime/calendarDate", "setEnd", 2);
    digester.addCallParam("eml/dataset/coverage/temporalCoverage/singleDateTime/calendarDate", 0);
    digester.addObjectParam("eml/dataset/coverage/temporalCoverage/singleDateTime/calendarDate", 1, "yyyy-MM-dd");
		digester.addCallMethod("eml/dataset/coverage/temporalCoverage/rangeOfDates/beginDate/calendarDate", "setStart", 2);
		digester.addCallParam("eml/dataset/coverage/temporalCoverage/rangeOfDates/beginDate/calendarDate", 0);
		digester.addObjectParam("eml/dataset/coverage/temporalCoverage/rangeOfDates/beginDate/calendarDate", 1, "yyyy-MM-dd");
		digester.addCallMethod("eml/dataset/coverage/temporalCoverage/rangeOfDates/endDate/calendarDate", "setEnd", 2);
		digester.addCallParam("eml/dataset/coverage/temporalCoverage/rangeOfDates/endDate/calendarDate", 0);
		digester.addObjectParam("eml/dataset/coverage/temporalCoverage/rangeOfDates/endDate/calendarDate", 1, "yyyy-MM-dd");
		digester.addSetNext("eml/dataset/coverage/temporalCoverage", "addTemporalCoverage"); // add the TemporalCoverage to the list in EML
}

  /**
   * Adds rules to extract the livingTimePeriod temporal coverage
   * @param digester to add the rules to
   */
  private static void addLivingTimePeriodRules(Digester digester) {
    digester.addObjectCreate("eml/additionalMetadata/metadata/livingTimePeriod", TemporalCoverage.class);
    digester.addCallMethod("eml/additionalMetadata/metadata/livingTimePeriod", "setLivingTimePeriod", 1);
    digester.addCallParam("eml/additionalMetadata/metadata/livingTimePeriod", 0);
    digester.addSetNext("eml/additionalMetadata/metadata/livingTimePeriod", "addTemporalCoverage"); // add the TemporalCoverage to the list in EML
  }

  /**
   * Adds rules to extract the livingTimePeriod temporal coverage
   * @param digester to add the rules to
   */
  private static void addFormationPeriodRules(Digester digester) {
    digester.addObjectCreate("eml/additionalMetadata/metadata/formationPeriod", TemporalCoverage.class);
    digester.addCallMethod("eml/additionalMetadata/metadata/formationPeriod", "setFormationPeriod", 1);
    digester.addCallParam("eml/additionalMetadata/metadata/formationPeriod", 0);
    digester.addSetNext("eml/additionalMetadata/metadata/formationPeriod", "addTemporalCoverage"); // add the TemporalCoverage to the list in EML
  }

  /**
	 * Adds rules to get the geographic coverage
	 * @param digester to add the rules to
	 */
	private static void addGeographicCoverageRules(Digester digester) {
		digester.addObjectCreate("eml/dataset/coverage/geographicCoverage", GeospatialCoverage.class);
		digester.addBeanPropertySetter("eml/dataset/coverage/geographicCoverage/geographicDescription", "description");
		digester.addObjectCreate("eml/dataset/coverage/geographicCoverage/boundingCoordinates", BBox.class);
		digester.addBeanPropertySetter("eml/dataset/coverage/geographicCoverage/boundingCoordinates/westBoundingCoordinate", "minX");
		digester.addBeanPropertySetter("eml/dataset/coverage/geographicCoverage/boundingCoordinates/eastBoundingCoordinate", "maxX");
		digester.addBeanPropertySetter("eml/dataset/coverage/geographicCoverage/boundingCoordinates/northBoundingCoordinate", "maxY");
		digester.addBeanPropertySetter("eml/dataset/coverage/geographicCoverage/boundingCoordinates/southBoundingCoordinate", "minY");
		digester.addSetNext("eml/dataset/coverage/geographicCoverage/boundingCoordinates", "setBoundingCoordinates"); // add the BBox to the GeospatialCoverage
		digester.addSetNext("eml/dataset/coverage/geographicCoverage", "addGeospatialCoverage"); // add the GeospatialCoverage to the list in EML
	}

	/**
	 * Add rules to extract the keywords
	 * @param digester to add the rules to
	 */
	private static void addKeywordRules(Digester digester) {
		digester.addObjectCreate("eml/dataset/keywordSet", KeywordSet.class);
		digester.addCallMethod("eml/dataset/keywordSet/keyword", "add", 1);
		digester.addCallParam("eml/dataset/keywordSet/keyword", 0);
		digester.addBeanPropertySetter("eml/dataset/keywordSet/keywordThesaurus", "keywordThesaurus");
		digester.addSetNext("eml/dataset/keywordSet", "addKeywordSet"); // add the KeywordSet to the list in EML
	}

	/**
	 * Adds rules to extract the methods
	 * In the EML, it will always be a repetition of [methodStep, ?sampling, ?qualityControl]
	 * (sampling and quality control are optional)
	 * to handle this, the utility object MethodsParseUtil is used to collect the methods
	 * @param digester to add the rules to
	 */
	private static void addMethodRules(Digester digester) {
		digester.addObjectCreate("eml/dataset/methods", MethodParseUtil.class);
		digester.addCallMethod("eml/dataset/methods/methodStep/description/para", "collectStep", 1);
		digester.addCallParam("eml/dataset/methods/methodStep/description/para", 0);
		digester.addCallMethod("eml/dataset/methods/sampling/studyExtent/description", "collectExtentDesc", 1);
		digester.addCallParam("eml/dataset/methods/sampling/studyExtent/description", 0);
		digester.addCallMethod("eml/dataset/methods/sampling/samplingDescription/para", "collectSamplingDesc", 1);
		digester.addCallParam("eml/dataset/methods/sampling/samplingDescription/para", 0);
		digester.addCallMethod("eml/dataset/methods/qualityControl/description/para", "collectQualityDesc", 1);
		digester.addCallParam("eml/dataset/methods/qualityControl/description/para", 0);
		digester.addSetTop("eml/dataset/methods", "updateEml"); // note that this is pushing the EML onto the util and popping the util
	}

	/**
	 * This is a reusable set of rules to build Agents and their Addresses, and add the Agent to the
	 * predecessor object on the Stack
	 * Note that we are ignoring the userId as there have been no requests for the IPT to support this
	 * @param digester to add the rules to
	 * @param prefix The XPath prefix to prepend for extracting the Agent information
	 * @param parentMethod Of the previous stack object to call and add the Agent to 
	 */
	private static void addAgentRules(Digester digester, String prefix, String parentMethod) {
		digester.addObjectCreate(prefix, Agent.class);
		digester.addBeanPropertySetter(prefix + "/individualName/givenName", "firstName");
		digester.addBeanPropertySetter(prefix + "/individualName/surName", "lastName");
		digester.addBeanPropertySetter(prefix + "/organizationName", "organisation");
		digester.addBeanPropertySetter(prefix + "/positionName", "position");
    digester.addBeanPropertySetter(prefix + "/phone", "phone");
		digester.addBeanPropertySetter(prefix + "/electronicMailAddress", "email");
		digester.addBeanPropertySetter(prefix + "/onlineUrl", "homepage");

		digester.addCallMethod(prefix + "/role", "setRole", 1);
    digester.addCallParam(prefix + "/role", 0);

		digester.addObjectCreate(prefix + "/address", Address.class);
		digester.addBeanPropertySetter(prefix + "/address/city", "city");
		digester.addBeanPropertySetter(prefix + "/address/administrativeArea", "province");
		digester.addBeanPropertySetter(prefix + "/address/postalCode", "postalCode");
		digester.addBeanPropertySetter(prefix + "/address/country", "country");
		digester.addBeanPropertySetter(prefix + "/address/deliveryPoint", "address");
		digester.addSetNext(prefix + "/address", "setAddress"); // called on </address> to set on parent Agent
		digester.addSetNext(prefix + "", parentMethod); // method called on parent object which is the previous stack object
	}  
}
