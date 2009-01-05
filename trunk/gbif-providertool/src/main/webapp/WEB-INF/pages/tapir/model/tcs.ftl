<#assign page=JspTaglibs["http://www.opensymphony.com/sitemesh/page"]>
<?xml version="1.0" encoding="UTF-8"?>
<rdf:RDF xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:dc="http://purl.org/dc/elements/1.1/" 
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:tn="http://rs.tdwg.org/ontology/voc/TaxonName#"
    xmlns:tc="http://rs.tdwg.org/ontology/voc/TaxonConcept#"
    xmlns:tcom="http://rs.tdwg.org/ontology/voc/Common#"    
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:owl="http://www.w3.org/2002/07/owl#">
    
<#escape x as x?xml>
    <#list taxa as t>
        <tn:TaxonName rdf:about="#tn-${t.guid}">
            <tn:nomenclaturalCode rdf:resource="http://rs.tdwg.org/ontology/voc/TaxonName#${t.nomenclaturalCode}"/>
            <dc:title>${t.name}</dc:title>
            <#if t.dwcRank??>
            <tn:rank rdf:resource="${t.dwcRank.uri}" />
            </#if>                         
            <tn:rankString>${t.rank}</tn:rankString>
            <tn:nameComplete>${t.name}</tn:nameComplete>
            <#if t.nomenclaturalReference??>
            <tcom:publishedIn>${t.nomenclaturalReference}</tcom:publishedIn>    
            </#if>                         
            <#if t.basionym??>
            <tn:hasBasionym>
                <tn:TaxonName rdf:resource="#tc-${t.basionym.guid}"/>
            </tn:hasBasionym>
            </#if>
            <#if t.nomStatus??>
            <tn:hasAnnotation>
            	<tn:note>${t.nomStatus}</tn:note>
            	<tn:subjectTaxonName>
            		<tn:TaxonName rdf:resource="#tn-${t.guid}">
            	</tn:subjectTaxonName>
            </tn:hasAnnotation>                         
            </#if>
        </tn:TaxonName>        
        <tc:TaxonConcept rdf:about="#tc-${t.guid}">
            <tc:accordingToString>${t.resource.title}</tc:accordingToString>
            <tc:hasName rdf:resource="#tn-${t.guid}"/>
            <tc:nameString>${t.name}</tc:nameString>
            <#if t.dwcRank??>
            <tn:rank rdf:resource="${t.dwcRank.uri}" />
            </#if>                         
            <tc:rankString>${t.rank}</tc:rankString>
            <#if t.parent??>
                <tc:hasRelationship>
                    <tc:Relationship>
                        <tc:fromTaxon rdf:resource="#tc-${t.guid}"/>
                        <tc:toTaxon rdf:resource="#tc-${t.parent.guid}"/>
                        <tc:relationshipCategory rdf:resource="http://rs.tdwg.org/ontology/voc/TaxonConcept#IsChildTaxonOf"/>
                    </tc:Relationship>
                </tc:hasRelationship>
            </#if>                         
            <#if t.acceptedTaxon??>
                <tc:hasRelationship>
                    <tc:Relationship>
                        <tc:fromTaxon rdf:resource="#tc-${t.guid}"/>
                        <tc:toTaxon rdf:resource="#tc-${t.acceptedTaxon.guid}"/>
                        <tc:relationshipCategory rdf:resource="http://rs.tdwg.org/ontology/voc/TaxonConcept#IsSynonymFor"/>
                    </tc:Relationship>
                </tc:hasRelationship>
            </#if>                         
        </tc:TaxonConcept>
    </#list>    
</#escape>
</rdf:RDF>

