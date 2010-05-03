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
        <title>${eml.title}</title>

        <creator>
            <individualName>
                <givenName>${eml.getResourceCreator().firstName!}</givenName>
                <surName>${eml.getResourceCreator().lastName!}</surName>
            </individualName>
            <organizationName>${eml.getResourceCreator().organisation!}</organizationName>
            <positionName>${eml.getResourceCreator().position!}</positionName>
            <address>
                <city>${eml.getResourceCreator().address.city!}</city>
                <administrativeArea>${eml.getResourceCreator().address.province!}</administrativeArea>
                <postalCode>${eml.getResourceCreator().address.postalCode!}</postalCode>
                <country>${eml.getResourceCreator().address.country!}</country>
            </address>
            <phone>${eml.getResourceCreator().phone!}</phone>
            <electronicMailAddress>${eml.getResourceCreator().email!}</electronicMailAddress>
            <onlineUrl>${eml.getResource().link!}</onlineUrl>
        </creator>

        <metadataProvider>
            <individualName>
                <givenName>${eml.getMetadataProvider().firstName!}</givenName>
                <surName>${eml.getMetadataProvider().lastName!}</surName>
            </individualName>
            <address>
                <deliveryPoint>${eml.getMetadataProvider().address.address!}</deliveryPoint>
                <city>${eml.getMetadataProvider().address.city!}</city>
                <administrativeArea>${eml.getMetadataProvider().address.province!}</administrativeArea>
                <postalCode>${eml.getMetadataProvider().address.postalCode!}</postalCode>
                <country>${eml.getMetadataProvider().address.country!}</country>
            </address>
            <electronicMailAddress>${eml.getMetadataProvider().email!}</electronicMailAddress>
        </metadataProvider>
       
        <#if (eml.associatedParties ? size > 0)>
        <@s.iterator value="eml.associatedParties" status="stat">     
        <associatedParty>
            <individualName>
                <givenName><@s.property value="firstName"/></givenName>
                <surName><@s.property value="lastName"/></surName>
            </individualName>
            <organizationName><@s.property value="organisation"/></organizationName>
            <positionName><@s.property value="position"/></positionName>
            <address>
                <city><@s.property value="address.city"/></city>
                <administrativeArea><@s.property value="address.province"/></administrativeArea>
                <postalCode><@s.property value="address.postalCode"/></postalCode>
                <country><@s.property value="address.country"/></country>
            </address>
            <phone><@s.property value="phone"/></phone>
            <electronicMailAddress><@s.property value="phone"/></electronicMailAddress>
            <onlineUrl><@s.property value="email"/></onlineUrl>
            <role><@s.property value="role"/></role>
        </associatedParty>
        </@s.iterator>
        </#if>
        
        <pubDate>${eml.pubDate?date?string("yyyy-MM-dd")}</pubDate>
        <language>${eml.language!}</language>
        <abstract>
            <para>${eml.abstract!}</para>
        </abstract>
        
        <keywordSet>
            <keyword>IPT</keyword>
            <keyword>GBIF</keyword>
            <#list eml.keywords as k>
            <keyword>${k!""}</keyword>
            </#list>
        </keywordSet>
        
        <additionalInfo>
            <para>${eml.additionalInfo!}</para>
        </additionalInfo>
        
        <intellectualRights>
            <para>${eml.intellectualRights!}</para>
        </intellectualRights>
        
        <distribution scope="document">
            <online>
                <url function="information">${eml.distributionUrl!}</url>
            </online>
        </distribution>
        
        <coverage>
            <#if (eml.geospatialCoverages ? size > 0)>
            <#list eml.getGeospatialCoverages() as geocoverage>
            <geographicCoverage>
                <geographicDescription>${geocoverage.description!}</geographicDescription>
                <#if geocoverage.boundingCoordinates.min?exists>
                <boundingCoordinates>
                    <westBoundingCoordinate>${geocoverage.boundingCoordinates.min.longitude!}</westBoundingCoordinate>
                    <eastBoundingCoordinate>${geocoverage.boundingCoordinates.max.longitude!}</eastBoundingCoordinate>
                    <northBoundingCoordinate>${geocoverage.boundingCoordinates.max.latitude!}</northBoundingCoordinate>
                    <southBoundingCoordinate>${geocoverage.boundingCoordinates.min.latitude!}</southBoundingCoordinate>
                </boundingCoordinates>
                </#if>
            </geographicCoverage>
            </#list>
            </#if>

            <#if (eml.temporalCoverages ? size > 0)>
            <#list eml.getTemporalCoverages() as tempcoverage>
            <temporalCoverage>
                <#if (tempcoverage.end)?? || (tempcoverage.start)??>
                  <#if (tempcoverage.end)?? && (tempcoverage.start)??>
                    <#if (tempcoverage.end) == (tempcoverage.start)>
                      <singleDateTime><calendarDate>tempcoverage.start?date?string("yyyy-MM-dd")}</calendarDate></singleDateTime>
                    <#else>
                      <rangeOfDates>
                        <beginDate><calendarDate>tempcoverage.start?date?string("yyyy-MM-dd")</calendarDate></beginDate>
                        <endDate><calendarDate>tempcoverage.end?date?string("yyyy-MM-dd")}</calendarDate></endDate>
                       </rangeOfDates>
                    </#if>
                  </#if>
                  <#if (tempcoverage.end)??>
                    <rangeOfDates>
                      <beginDate><calendarDate></calendarDate></beginDate>
                      <endDate><calendarDate>tempcoverage.end?date?string("yyyy-MM-dd")}</calendarDate></endDate>
                    </rangeOfDates>
                  <#else>
                    <rangeOfDates>
                      <beginDate><calendarDate></calendarDate></beginDate>
                      <endDate><calendarDate>tempcoverage.end?date?string("yyyy-MM-dd")}</calendarDate></endDate>
                    </rangeOfDates>
                  </#if>
                </#if>
            </temporalCoverage>
            </#list>
            </#if>

            <#if (eml.taxonomicCoverages ? size > 0)>
            <#list eml.getTaxonomicCoverages() as taxoncoverage>
            <taxonomicCoverage>
                <generalTaxonomicCoverage>${taxoncoverage.description!}</generalTaxonomicCoverage>
                <#list taxoncoverage.keywords as k>
                <taxonomicClassification>
                    <taxonRankName>${k.rank!}</taxonRankName>
                    <taxonRankValue>${k.scientificName!}</taxonRankValue>
                    <commonName>${k.commonName!}</commonName>
                </taxonomicClassification>
                </#list>
            </taxonomicCoverage>
            </#list>
            </#if>
        </coverage>

        <purpose>
            <para>${eml.purpose!}</para>
        </purpose>
        
        <contact>
            <individualName>
                <givenName>${eml.getResourceCreator().firstName!}</givenName>
                <surName>${eml.getResourceCreator().lastName!}</surName>
            </individualName>
            <organizationName>${eml.getResourceCreator().organisation!}</organizationName>
            <positionName>${eml.getResourceCreator().position!}</positionName>
            <address>
                <city>${eml.getResourceCreator().address.city!}</city>
                <administrativeArea>${eml.getResourceCreator().address.province!}</administrativeArea>
                <postalCode>${eml.getResourceCreator().address.postalCode!}</postalCode>
                <country>${eml.getResourceCreator().address.country!}</country>
            </address>
            <phone>${eml.getResourceCreator().phone!}</phone>
            <electronicMailAddress>${eml.getResourceCreator().email!}</electronicMailAddress>
            <onlineUrl>${eml.getResource().link!}</onlineUrl>
        </contact>

        <#if (eml.samplingMethods)??>
        <methods>
            <#if (eml.getSamplingMethods() ? size > 0)>
            <#list eml.getSamplingMethods() as samplemethod>
            <methodStep>
              <description><para>${samplemethod.sampleDescription!}</para></description>
            </methodStep>
            <#if (samplemethod.studyExtent)??>
            <sampling>
              <studyExtent><description>${samplemethod.studyExtent!}</description></studyExtent>
            </sampling>
            </#if>
            <#if (samplemethod.qualityControl)??>
            <qualityControl>
              <description><para>${samplemethod.qualityControl!}</para></description>
            </qualityControl>
            </#if>
            </#list>
            </#if>
        </methods>
        </#if>

        <#if (eml.project)??>
        <project>
            <title>${eml.project.title!}</title>
            <#if (eml.project.personnel)??>
            <personnel>
              <individualName>
                <givenName>${eml.project.personnel.firstName!}</givenName>
                <surName>${eml.project.personnel.lastName!}</surName>
              </individualName>
              <organizationName>${eml.project.personnel.organisation!}</organizationName>
              <positionName>${eml.project.personnel.position!}</positionName>
              <address>
                <city>${eml.project.personnel.address.city!}</city>
                <administrativeArea>${eml.project.personnel.address.province!}</administrativeArea>
                <postalCode>${eml.project.personnel.address.postalCode!}</postalCode>
                <country>${eml.project.personnel.address.country!}</country>
              </address>
              <phone>${eml.project.personnel.phone!}</phone>
              <electronicMailAddress>${eml.project.personnel.email!}</electronicMailAddress>
              <onlineUrl>${eml.project.personnel.homepage!}</onlineUrl>
            </personnel>
            </#if>
            <#if eml.project.funding??>
            <abstract>
                <para>${eml.project.funding!}</para>
            </abstract>
            </#if>
            <#if eml.project.studyAreaDescription??>
            <studyAreaDescription>
              <descriptor name=${eml.project.studyAreaDescription.name.name!}  citableClassificationSystem=${eml.project.studyAreaDescription.citableClassificationSystem!}>
                 <descriptorValue>${eml.project.studyAreaDescription.descriptorValue!}</descriptorValue>
              </descriptor>
            </studyAreaDescription>            
            </#if>
            <#if eml.project.designDescription??>
            <designDescription><description><para>${eml.project.designDescription!}</para></description></designDescription>
            </#if>
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
        <citation>${eml.citation!}</citation>
       </#if>

       <#if (eml.bibliographicCitations ? size > 0)>
        <bibliography>
        <#list eml.getBibliographicCitations() as bcitset>
        <#list bcitset.bibliographicCitations as bcit>
          <citation>${bcit!}</citation>
        </#list>
        </#list>
        </bibliography>
       </#if>

       <#if (eml.metadataLanguage)??>
        <metadataLanguage>${eml.metadataLanguage!}</metadataLanguage>
       </#if>    

       <#if (eml.hierarchyLevel)??>
        <hierarchyLevel>${eml.hierarchyLevel!}</hierarchyLevel>
       </#if>

       <#if (eml.physicalData)??>
       <#if (eml.physicalData ? size > 0)>
       <#list eml.getPhysicalData() as pdata>
        <physical>
          <objectName>pdata.name</objectName>
          <characterEncoding>pdata.name</characterEncoding>
          <dataFormat>
            <externallyDefinedFormat>
              <formatName>pdata.format</formatName>
              <formatVersion>pdata.formatVersion</formatVersion>
            </externallyDefinedFormat>
          </dataFormat>
          <distribution>
            <online>
              <url function="download">pdata.distributionUrl</url>
            </online>
          </distribution>
        </physical>
       </#list>
       </#if>
       </#if>
       <#if (eml.jgtiCuratorialUnit)??>
        <jgtiCuratorialUnit>
          <jgtoUnitType>${eml.jgtiCuratorialUnit.unitType!}</jgtiUnitType>
          <#if (eml.jgtiCuratorialUnit.rangeEnd)??>
          <jgtoUnitRange>
             <beginRange>${eml.jgtiCuratorialUnit.rangeStart!}</beginRange>
             <endRange>${eml.jgtiCuratorialUnit.rangeEnd!}</endRange>
          </jgtoUnitRange>
          <#else>
            <jgtiUnits uncertaintyMeasure="${eml.jgtiCuratorialUnit.uncertaintyMeasure!}">${eml.jgtiCuratorialUnit.beginRange!}</jgtiUnits>
          </#if>
          <unit>${eml.jgtiCuratorialUnit.unit!}</unit>
        </jgtiCuratorialUnit>
       </#if>

       <#if (eml.specimenPreservationMethod)??>
        <specimenPreservationMethod>${eml.specimenPreservationMethod!}</specimenPreservationMethod>
       </#if>
    
       <#if (eml.temporalCoverages ? size > 0)>
       <#list eml.getTemporalCoverages() as tcoverage>
       <#if (tcoverage.livingTimePeriod)??>
        <livingTimePeriod>${tcoverage.livingTimePeriod!}</livingTimePeriod>
       </#if>
       <#if (tcoverage.formationPeriod)??>
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
       <#if (eml.logoUrl)??>
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