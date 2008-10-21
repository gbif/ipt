<?xml version="1.0" encoding="UTF-8"?>
<#escape x as x?xml>
<eml:eml xmlns:eml="eml://ecoinformatics.org/eml-2.0.1" 
    xmlns:v="eml://ecoinformatics.org/view-2.0.1" 
    xmlns:sp="eml://ecoinformatics.org/storedProcedure-2.0.1" 
    xmlns:sv="eml://ecoinformatics.org/spatialVector-2.0.1" 
    xmlns:md="eml://ecoinformatics.org/methods-2.0.1" 
    xmlns:sr="eml://ecoinformatics.org/spatialRaster-2.0.1" 
    xmlns:proj="eml://ecoinformatics.org/project-2.0.1" 
    xmlns:dat="eml://ecoinformatics.org/dataTable-2.0.1" 
    xmlns:ent="eml://ecoinformatics.org/entity-2.0.1" 
    xmlns:acc="eml://ecoinformatics.org/access-2.0.1" 
    xmlns:d="eml://ecoinformatics.org/dataset-2.0.1" 
    xmlns:res="eml://ecoinformatics.org/resource-2.0.1" 
    xmlns:rp="eml://ecoinformatics.org/party-2.0.1" 
    xmlns:txt="eml://ecoinformatics.org/text-2.0.1" 
    xmlns:doc="eml://ecoinformatics.org/documentation-2.0.1" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    packageId="${resource.guid}-${resource.modified?string("HHmmss")}" system="GBIF-IPT" scope="system">
    <dataset>
        <alternateIdentifier>${resource.guid}</alternateIdentifier>
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
            <onlineUrl>${resource.link!}</onlineUrl>
        </creator>
        <pubDate>${eml.pubDate?date?string.short}</pubDate>
        <language>${eml.language!}</language>
        <abstract>
            <para>${eml.abstract!}</para>
        </abstract>
        <keywordSet>
        	<#list eml.keywords as k>
            <keyword>${k}</keyword>
            </#list>
        </keywordSet>
        <intellectualRights>
            <para>${eml.intellectualRights!}</para>
        </intellectualRights>
        <coverage>
            <geographicCoverage>
                <geographicDescription>${eml.getGeographicCoverage().description!}</geographicDescription>
	            <#if eml.getGeographicCoverage().boundingCoordinates??>
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
                    <beginDate>${eml.getTemporalCoverage().start?date?string.short}</beginDate>
                    <endDate>${eml.getTemporalCoverage().end?date?string.short}</endDate>
                </rangeOfDates>
                <#else>
                <singleDateTime>${eml.getTemporalCoverage().start?date?string.short}</singleDateTime>
                </#if>
            </temporalCoverage>
            </#if>
            <taxonomicCoverage>
                <generalTaxonomicCoverage>${eml.taxonomicCoverageDescription!}</generalTaxonomicCoverage>
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
                <surName>${resource.contactName!}</surName>
            </individualName>
            <electronicMailAddress>${resource.contactEmail!}</electronicMailAddress>
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
            <studyAreaDescription>${eml.researchProject.studyAreaDescription!}</studyAreaDescription>
            </#if>
            <#if eml.researchProject.designDescription??>
            <designDescription>${eml.researchProject.designDescription!}</designDescription>
            </#if>
        </project>
    </dataset>
</eml:eml>
</#escape>
