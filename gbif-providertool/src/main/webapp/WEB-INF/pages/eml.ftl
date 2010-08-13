<#escape x as x?xml>
<eml:eml xmlns:eml="eml://ecoinformatics.org/eml-2.0.1" 
    xmlns:md="eml://ecoinformatics.org/methods-2.0.1" 
    xmlns:proj="eml://ecoinformatics.org/project-2.0.1" 
    xmlns:d="eml://ecoinformatics.org/dataset-2.0.1" 
    xmlns:res="eml://ecoinformatics.org/resource-2.0.1" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:dc="http://purl.org/dc/terms/" 
    packageId="${eml.getResource().guid}/eml-${eml.getEmlVersion()}.xml" system="GBIF-IPT" scope="system">

    <dataset>
      <alternateIdentifier>${eml.getResource().guid}</alternateIdentifier>
      <title xml:lang="${eml.language}">${eml.title!}</title>
<!-- The creator is the person who created the resource
     (not necessarily the author of this metadata about the resource). -->
      <creator>
        <individualName>
          <givenName>${eml.getResourceCreator().firstName!}</givenName>
          <surName>${eml.getResourceCreator().lastName!}</surName>
        </individualName>
        <organizationName>${eml.getResourceCreator().organisation!}</organizationName>
      <#if (eml.getResourceCreator().getPosition())??>
        <positionName>${eml.getResourceCreator().position!}</positionName>
      </#if>
        <address>
        <#if (eml.getResourceCreator().getAddress().getAddress())??>
          <deliveryPoint>${eml.getResourceCreator().address.address!}</deliveryPoint>
        </#if>
          <city>${eml.getResourceCreator().address.city!}</city>
          <administrativeArea>${eml.getResourceCreator().address.province!}</administrativeArea>
          <postalCode>${eml.getResourceCreator().address.postalCode!}</postalCode>
          <country>${eml.getResourceCreator().address.country!}</country>
        </address>
      <#if (eml.getResourceCreator().getPhone())??>
        <phone>${eml.getResourceCreator().phone!}</phone>
      </#if>
      <#if (eml.getResourceCreator().getEmail())??>
        <electronicMailAddress>${eml.getResourceCreator().email!}</electronicMailAddress>
      </#if>
      <#if (eml.getResourceCreator().getHomepage())??>
        <onlineUrl>${eml.getResourceCreator().homepage!}</onlineUrl>
      </#if>
      </creator>
<!-- The agent responsible for the creation of the metadata. -->
      <metadataProvider>
        <individualName>
          <givenName>${eml.getMetadataProvider().firstName!}</givenName>
          <surName>${eml.getMetadataProvider().lastName!}</surName>
        </individualName>
      <#if ((eml.getMetadataProvider().getAddress().getAddress())?? 
        || (eml.getMetadataProvider().getAddress().getCity())?? 
        || (eml.getMetadataProvider().getAddress().getProvince())?? 
        || (eml.getMetadataProvider().getAddress().getPostalCode())?? 
        || (eml.getMetadataProvider().getAddress().getCountry())??)>
        <address>
        <#if (eml.getMetadataProvider().getAddress().getAddress())??>
          <deliveryPoint>${eml.getMetadataProvider().address.address!}</deliveryPoint>
        </#if>
          <city>${eml.getMetadataProvider().address.city!}</city>
          <administrativeArea>${eml.getMetadataProvider().address.province!}</administrativeArea>
          <postalCode>${eml.getMetadataProvider().address.postalCode!}</postalCode>
          <country>${eml.getMetadataProvider().address.country!}</country>
        </address>
      </#if>
      <#if (eml.getMetadataProvider().getEmail())??>
        <electronicMailAddress>${eml.getMetadataProvider().email!}</electronicMailAddress>
      </#if>
      </metadataProvider>
<!-- Any other party associated with the resource, along with their role. -->
    <#if (eml.associatedParties ? size > 0)>
    <#list eml.getAssociatedParties() as associatedParty>
      <associatedParty>
        <individualName>
          <givenName>${associatedParty.firstName!}</givenName>
          <surName>${associatedParty.lastName!}</surName>
        </individualName>
      <#if (associatedParty.getPhone())??>
        <phone>${associatedParty.phone!}</phone>
      </#if>
        <role>${associatedParty.role!}</role>
      </associatedParty>
    </#list>
    </#if>
<!-- The date on which the resource was published. -->
      <pubDate>${eml.pubDate?date?string("yyyy-MM-dd")}</pubDate>
<!-- A brief description of the resource. -->
      <abstract>
        <para>
          ${eml.abstract!}
        </para>
      </abstract>
<!-- Zero or more sets of keywords and an associated thesaurus for each. -->        
    <#if (eml.keywords ? size > 0)>
      <#list eml.keywords as keyword>
      <keywordSet>
      <#if (keyword.keywords ? size > 0)>
      <#list keyword.keywords as k>
        <keyword>${k!""}</keyword>
      </#list>
      </#if>
        <keywordThesaurus></keywordThesaurus>
      </keywordSet>
      </#list>
    </#if>
<!-- Any additional information about the resource not covered in any other element. -->
    <#if (eml.getAdditionalInfo())??>
      <additionalInfo>
        <para>
          ${eml.additionalInfo!}
        </para>
      </additionalInfo>
    </#if>
<!-- A statement of the intellectual property rights associated with the resource. -->
    <#if (eml.getIntellectualRights())??>
      <intellectualRights>
        <para>
          ${eml.intellectualRights!}
        </para>
      </intellectualRights>
    </#if>
    <#if (eml.getDistributionUrl())??>
      <distribution scope="document">
        <online>
          <url function="information">${eml.distributionUrl!}</url>
        </online>
      </distribution>
    </#if>
    <#if ((eml.geospatialCoverages ? size > 0)
      || ((eml.geographicCoverage)??)
      ||  (eml.taxonomicCoverages ? size > 0)
      ||  (eml.temporalCoverages ? size > 0))>
      <coverage>        
      <#if (eml.geospatialCoverages ? size > 0)>
      <#list eml.getGeospatialCoverages() as geocoverage>
        <geographicCoverage>
          <geographicDescription>${geocoverage.description!}</geographicDescription>
          <boundingCoordinates>
            <westBoundingCoordinate>${geocoverage.boundingCoordinates.min.longitude!}</westBoundingCoordinate>
            <eastBoundingCoordinate>${geocoverage.boundingCoordinates.max.longitude!}</eastBoundingCoordinate>
            <northBoundingCoordinate>${geocoverage.boundingCoordinates.max.latitude!}</northBoundingCoordinate>
            <southBoundingCoordinate>${geocoverage.boundingCoordinates.min.latitude!}</southBoundingCoordinate>
          </boundingCoordinates>
        </geographicCoverage>
      </#list>
      </#if>
      <#if (eml.geographicCoverage)??>
        <geographicCoverage>
          <geographicDescription>${eml.geographicCoverage.description!}</geographicDescription>
          <boundingCoordinates>
            <westBoundingCoordinate>${eml.geographicCoverage.boundingCoordinates.min.longitude!}</westBoundingCoordinate>
            <eastBoundingCoordinate>${eml.geographicCoverage.boundingCoordinates.max.longitude!}</eastBoundingCoordinate>
            <northBoundingCoordinate>${eml.geographicCoverage.boundingCoordinates.max.latitude!}</northBoundingCoordinate>
            <southBoundingCoordinate>${eml.geographicCoverage.boundingCoordinates.min.latitude!}</southBoundingCoordinate>
          </boundingCoordinates>
        </geographicCoverage>
      </#if>
      <#if (eml.temporalCoverages ? size > 0)>
      <#list eml.temporalCoverages as tempcoverage>
        <#if (tempcoverage.startDate)??>
        <temporalCoverage>
        <#if (tempcoverage.endDate)??>
          <rangeOfDates>
          <#if (tempcoverage.startDate)??>
            <beginDate><calendarDate>${tempcoverage.startDate?string("yyyy-mm-dd")}</calendarDate></beginDate>
          </#if>
            <endDate><calendarDate>${tempcoverage.endDate?string("yyyy-mm-dd")}</calendarDate></endDate>
          </rangeOfDates>
        <#else>
        <#if (tempcoverage.startDate)??>
          <singleDateTime><calendarDate>${tempcoverage.startDate?string("yyyy-mm-dd")}</calendarDate></singleDateTime>
        </#if>
        </#if>
        </temporalCoverage>
        </#if>
      </#list>
      </#if>
      <#if (eml.taxonomicCoverages ? size > 0)>
      <#list eml.getTaxonomicCoverages() as taxoncoverage>
        <taxonomicCoverage>
         <generalTaxonomicCoverage>${taxoncoverage.description!}</generalTaxonomicCoverage>
         <taxonomicClassification>
           <taxonRankName>${taxoncoverage.taxonKeyword.rank!}</taxonRankName>
           <taxonRankValue>${taxoncoverage.taxonKeyword.scientificName!}</taxonRankValue>
           <commonName>${taxoncoverage.taxonKeyword.commonName!}</commonName>
         </taxonomicClassification>
        </taxonomicCoverage>
      </#list>
      </#if>
      </coverage>
    </#if>
    <#if (eml.getPurpose())??>
      <purpose>
        <para>
          ${eml.purpose!}
        </para>
      </purpose>
    </#if>
      <contact>
        <individualName>
          <givenName>${eml.getContact().firstName!}</givenName>
          <surName>${eml.getContact().lastName!}</surName>
        </individualName>
        <organizationName>${eml.getContact().organisation!}</organizationName>
      <#if (eml.getContact().getPosition())??>
        <positionName>${eml.getContact().position!}</positionName>
      </#if>
        <address>
        <#if (eml.getContact().getAddress().getAddress())??>
          <deliveryPoint>${eml.getContact().address.address!}</deliveryPoint>
        </#if>
          <city>${eml.getContact().address.city!}</city>
          <administrativeArea>${eml.getContact().address.province!}</administrativeArea>
          <postalCode>${eml.getContact().address.postalCode!}</postalCode>
          <country>${eml.getContact().address.country!}</country>
        </address>
      <#if (eml.getContact().getPhone())??>
        <phone>${eml.getContact().phone!}</phone>
      </#if>
      <#if (eml.getContact().getEmail())??>
        <electronicMailAddress>${eml.getContact().email!}</electronicMailAddress>
      </#if>
      <#if (eml.getContact().getHomepage())??>
        <onlineUrl>${eml.getContact().homepage!}</onlineUrl>
      </#if>
      </contact>
    <#if (eml.samplingMethods)??>
      <methods>
      <#if (eml.getSamplingMethods() ? size > 0)>
      <#list eml.getSamplingMethods() as samplemethod>
      <#if (samplemethod.getStepDescription())??>
        <methodStep>
          <description>
            <para>
              ${samplemethod.stepDescription!}
            </para></description>
        </methodStep>
      <#else>
      <#if (samplemethod.getQualityControl())??>
        <qualityControl>
          <description>
            <para>
              ${samplemethod.qualityControl!}
            </para></description>
        </qualityControl>
      <#else>
        <sampling>
          <studyExtent>
            <description>${samplemethod.studyExtent!}</description>
          </studyExtent>
          <samplingDescription>
            <para>
              ${samplemethod.sampleDescription!}
            </para>
          </samplingDescription>
        </sampling>
      </#if>
      </#if>
      </#list>
      </#if>
      </methods>
    </#if>
    <#if (eml.project)??>
      <project>
        <title>${eml.project.title!}</title>
        <personnel>
          <individualName>
            <givenName>${eml.project.personnel.firstName!}</givenName>
            <surName>${eml.project.personnel.lastName!}</surName>
          </individualName>
          <role>${eml.project.personnel.role!}</role>
        </personnel>
        <funding>
          <para>
            ${eml.project.funding!}
          </para>
        </funding>
        <studyAreaDescription>
          <#if (eml.project.studyAreaDescription.name)??>
          <#if (eml.project.studyAreaDescription.name.name)??>
          <descriptor name="${eml.project.studyAreaDescription.getName().getName()!}"  citableClassificationSystem="${eml.project.studyAreaDescription.citableClassificationSystem!}">
            <descriptorValue>${eml.project.studyAreaDescription.descriptorValue!}</descriptorValue>
          </descriptor>
          </#if>
          </#if>
        </studyAreaDescription>            
        <designDescription>
          <description>
            <para>
              ${eml.project.designDescription!}
            </para>
          </description>
        </designDescription>
      </project>
      </#if>
    </dataset>
    <#if ((eml.citation)??) || 
         (eml.bibliographicCitations ? size > 0) || 
         (eml.metadataLanguage)?? || 
         (eml.hierarchyLevel)?? ||
         (eml.PhysicalData ? size > 0) ||
         ((eml.jgtiCuratorialUnit)??) ||
         (eml.specimenPreservationMethod)?? ||
         (eml.temporalCoverages ? size > 0) ||
         (eml.parentCollectionId)?? || 
         (eml.collectionId)?? || 
         (eml.collectionName)?? ||
         (eml.logoUrl)?? ||
         (eml.getEmlVersion()>1)>
    <additionalMetadata>
      <metadata>
      <#if (eml.citation)??>
<!-- How to cite the resource. -->
        <citation>${eml.citation!}</citation>
      </#if>
      <#if (eml.getBibliographicCitations() ? size > 0)>
<!-- Citations about the resource. -->
        <bibliography>
        <#list eml.getBibliographicCitations() as bcit>
          <citation>${bcit!}</citation>
        </#list>
        </bibliography>
       </#if>
       <#if (eml.metadataLanguage)??>
        <metadataLanguage>${eml.metadataLanguage!}</metadataLanguage>
       </#if>    
       <#if (eml.hierarchyLevel)??>
        <hierarchyLevel>${eml.hierarchyLevel!}</hierarchyLevel>
       </#if>
       <#if (eml.physicalData ? size > 0)>
       <#list eml.getPhysicalData() as pdata>
        <physical>
          <objectName>${pdata.name}</objectName>
          <characterEncoding>${pdata.charset}</characterEncoding>
          <dataFormat>
            <externallyDefinedFormat>
              <formatName>${pdata.format}</formatName>
              <formatVersion>${pdata.formatVersion}</formatVersion>
            </externallyDefinedFormat>
          </dataFormat>
          <distribution>
            <online>
              <url function="download">${pdata.distributionUrl}</url>
            </online>
          </distribution>
        </physical>
      </#list>
      </#if>
      <#if (eml.jgtiCuratorialUnits ? size > 0)>
      <#list eml.getJgtiCuratorialUnits() as cdata>
        <jgtiCuratorialUnit>
          <jgtiUnitType>${cdata.unitType!}</jgtiUnitType>
          <#if (cdata.rangeEnd)??>
          <jgtiUnitRange>
             <beginRange>${cdata.rangeStart!}</beginRange>
             <endRange>${cdata.rangeEnd!}</endRange>
          </jgtiUnitRange>
          <#else>
            <jgtiUnits uncertaintyMeasure="${cdata.uncertaintyMeasure!}">${cdata.rangeMean!}</jgtiUnits>
          </#if>
        </jgtiCuratorialUnit>
      </#list>
      </#if>
      <#if (eml.getSpecimenPreservationMethod())??>
        <specimenPreservationMethod>${eml.specimenPreservationMethod!}</specimenPreservationMethod>
      </#if>
      <#if (eml.temporalCoverages ? size > 0)>
      <#list eml.getTemporalCoverages() as tcoverage>
      <#if (tcoverage.getLivingTimePeriod())??>
        <livingTimePeriod>${tcoverage.livingTimePeriod!}</livingTimePeriod>
      </#if>
      <#if (tcoverage.getFormationPeriod())??>
        <formationPeriod>${tcoverage.formationPeriod!}</formationPeriod>
      </#if>
      </#list>
      </#if>
       <#if (eml.parentCollectionId)?? || (eml.collectionId)?? || (eml.collectionName)??>
        <collection>
        <#if (eml.parentCollectionId)??>
          <parentCollectionIdentifier>${eml.parentCollectionId!}</parentCollectionIdentifier>
        </#if>
        <#if (eml.collectionId)??>
          <collectionIdentifier>${eml.collectionId!}</collectionIdentifier>
        </#if>
        <#if (eml.collectionName)??>
          <collectionName>${eml.collectionName!}</collectionName>
        </#if>
        </collection>
       </#if>
       <#if (eml.getLogoUrl())??>
	        <resourceLogoUrl>${eml.logoUrl!}</resourceLogoUrl>
       </#if>
       <#if (eml.getEmlVersion()>1)>
        <dc:replaces>${eml.getResource().guid}/eml-${eml.getEmlVersion()-1}.xml</dc:replaces>
       </#if>
      </metadata>
    </additionalMetadata>
    </#if>
</eml:eml>
</#escape>