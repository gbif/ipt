<#if header>
<?xml version="1.0" encoding="UTF-8"?>
<rdf:RDF xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
 xmlns:owl="http://www.w3.org/2002/07/owl#"
 xmlns:dc="http://purl.org/dc/elements/1.1/" 
 xmlns:dct="http://purl.org/dc/terms/"
 xmlns:tn="http://rs.tdwg.org/ontology/voc/TaxonName#"
 xmlns:tc="http://rs.tdwg.org/ontology/voc/TaxonConcept#"
 xmlns:tcom="http://rs.tdwg.org/ontology/voc/Common#">
</#if>
<#list taxa as t>
  <#include "/WEB-INF/pages/tapir/model/tcs.ftl">  
</#list>
<#if footer>
</rdf:RDF>
</#if>
