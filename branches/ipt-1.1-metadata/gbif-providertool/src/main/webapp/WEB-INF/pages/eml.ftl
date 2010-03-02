<#escape x as x?xml>
<eml:eml xmlns:eml="eml://ecoinformatics.org/eml-2.1.0"  
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:res="eml://ecoinformatics.org/resource-2.1.0" 
    xmlns:acc="eml://ecoinformatics.org/access-2.1.0" 
    xmlns:doc="eml://ecoinformatics.org/documentation-2.1.0" 
    xmlns:prot="eml://ecoinformatics.org/protocol-2.1.0" 
    xmlns:ds="eml://ecoinformatics.org/dataset-2.1.0" 
    xmlns:cit="eml://ecoinformatics.org/literature-2.1.0" 
    xmlns:sw="eml://ecoinformatics.org/software-2.1.0"
    xmlns:physical="eml://ecoinformatics.org/physical-2.1.0" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:dc="http://purl.org/dc/terms/" 
    xsi:schemaLocation="eml://ecoinformatics.org/eml-2.1.0 https://code.ecoinformatics.org/code/eml/tags/RELEASE_EML_2_1_0/eml.xsd"
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
    </dataset>
</eml:eml>
</#escape>
