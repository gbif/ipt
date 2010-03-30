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
        <intellectualRights>
            <para>${eml.intellectualRights!}</para>
        </intellectualRights>
        <coverage>
            <geographicCoverage>
                <geographicDescription>${(eml.getGeographicCoverage().description)!}</geographicDescription>
	            <#if (eml.getGeographicCoverage().boundingCoordinates.min)??>
                <boundingCoordinates>
                    <westBoundingCoordinate>${eml.getGeographicCoverage().boundingCoordinates.min.longitude!}</westBoundingCoordinate>
                    <eastBoundingCoordinate>${eml.getGeographicCoverage().boundingCoordinates.max.longitude!}</eastBoundingCoordinate>
                    <northBoundingCoordinate>${eml.getGeographicCoverage().boundingCoordinates.max.latitude!}</northBoundingCoordinate>
                    <southBoundingCoordinate>${eml.getGeographicCoverage().boundingCoordinates.min.latitude!}</southBoundingCoordinate>
                </boundingCoordinates>
                </#if>
            </geographicCoverage>
            <#if eml.getTemporalCoverage().start??>
            <temporalCoverage>
	            <#if eml.getTemporalCoverage().end??>
                <rangeOfDates>
                    <beginDate><calendarDate>${eml.getTemporalCoverage().start?date?string("yyyy-MM-dd")}</calendarDate></beginDate>
                    <endDate><calendarDate>${eml.getTemporalCoverage().end?date?string("yyyy-MM-dd")}</calendarDate></endDate>
                </rangeOfDates>
                <#else>
                <singleDateTime><calendarDate>${eml.getTemporalCoverage().start?date?string("yyyy-MM-dd")}</calendarDate></singleDateTime>
                </#if>
            </temporalCoverage>
            </#if>
            <taxonomicCoverage>
                <generalTaxonomicCoverage>${eml.taxonomicCoverageDescription!}</generalTaxonomicCoverage>
                <#if eml.lowestCommonTaxon()?exists>
                <taxonomicClassification>
                    <taxonRankName>${eml.lowestCommonTaxon().rank!}</taxonRankName>
                    <taxonRankValue>${eml.lowestCommonTaxon().scientificName!}</taxonRankValue>
                    <commonName>${eml.lowestCommonTaxon().commonName!}</commonName>
                </taxonomicClassification>
                </#if>
	        	<#list eml.getTaxonomicClassification() as k>
                <taxonomicClassification>
                    <taxonRankName>${k.rank!}</taxonRankName>
                    <taxonRankValue>${k.scientificName!}</taxonRankValue>
                    <commonName>${k.commonName!}</commonName>
                </taxonomicClassification>
	            </#list>
            </taxonomicCoverage>
        </coverage>
        <contact>
            <individualName>
                <surName>${eml.getResource().contactName!}</surName>
            </individualName>
            <electronicMailAddress>${eml.getResource().contactEmail!}</electronicMailAddress>
        </contact>
        <methods>
            <#if eml.methods??>
            <methodStep>
                <description>
                    <para>${eml.methods!}</para>
                </description>
            </methodStep>
            </#if>
            <#if eml.samplingDescription??>
            <sampling>
				<studyExtent><description><para></para></description></studyExtent>
                <samplingDescription><para>${eml.samplingDescription!}</para></samplingDescription>
            </sampling>
            </#if>
            <#if eml.qualityControl??>
            <qualityControl>
                <description><para>${eml.qualityControl!}</para></description>
            </qualityControl>            
            </#if>
        </methods>
        <project>
            <title>${eml.researchProject.title!}</title>
            <#if eml.researchProject.personnelOriginator.organisation??>
            <personnel>
                <organizationName>${eml.researchProject.personnelOriginator.organisation!}</organizationName>
                <role></role>
            </personnel>
            </#if>
            <#if eml.researchProject.abstract??>
            <abstract>
                <para>${eml.researchProject.abstract!}</para>
            </abstract>
            </#if>
            <#if eml.researchProject.funding??>
            <funding>
                <para>${eml.researchProject.funding!}</para>
            </funding>
            </#if>
            <#if eml.researchProject.studyAreaDescription??>
            <studyAreaDescription>
                <coverage>
                    <geographicCoverage>
                        <geographicDescription>${eml.researchProject.studyAreaDescription!}</geographicDescription>
                        <boundingCoordinates>
                            <westBoundingCoordinate></westBoundingCoordinate>
                            <eastBoundingCoordinate></eastBoundingCoordinate>
                            <northBoundingCoordinate></northBoundingCoordinate>
                            <southBoundingCoordinate></southBoundingCoordinate>
                        </boundingCoordinates>
                    </geographicCoverage>
                </coverage>
            </studyAreaDescription>            
            </#if>
            <#if eml.researchProject.designDescription??>
            <designDescription><description><para>${eml.researchProject.designDescription!}</para></description></designDescription>
            </#if>
        </project>
    </dataset>
    <additionalMetadata>
	    <#if (eml.getEmlVersion()>1)>
	    <dc:replaces>${eml.getResource().guid}/eml-${eml.getEmlVersion()-1}.xml</dc:replaces>
	    </#if>
    </additionalMetadata>
</eml:eml>
</#escape>
