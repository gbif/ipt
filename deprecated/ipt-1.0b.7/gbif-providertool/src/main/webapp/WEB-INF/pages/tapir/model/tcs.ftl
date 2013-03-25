<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<#escape x as x?xml>
<tn:TaxonName rdf:about="#tn-${t.guid}">
    <#if t.nomenclaturalCode??>
    <tn:nomenclaturalCode rdf:resource="http://rs.tdwg.org/ontology/voc/TaxonName#${t.nomenclaturalCode}"/>
    </#if>                         
    <dc:title>${t.scientificName!}</dc:title>
    <dct:modified>${t.modified?datetime?string(xmlDateFormat)}</dct:modified>
    <#if t.dwcRank??>
    <tn:rank rdf:resource="${t.dwcRank.uri}"/>
    </#if>                         
    <#if t.rank??>
    <tn:rankString>${t.rank}</tn:rankString>
    </#if>                         
    <tn:nameComplete>${t.scientificName!}</tn:nameComplete>
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
    <tc:nameString>${t.scientificName!}</tc:nameString>
    <#if t.dwcRank??>
    <tn:rank rdf:resource="${t.dwcRank.uri}" />
    </#if>                         
    <#if t.rank??>
    <tc:rankString>${t.rank}</tc:rankString>
    </#if>                         
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
</#escape>

