<?xml version='1.0' encoding='utf-8'?>
<dwr:DarwinRecord ${nsr.xmlnsDef()}>
<#assign dwc=record>
<#assign core=dwc.resource.coreMapping>
<#list core.extension.properties as p>
<#if "Taxon,DublinCore"?contains(p.group) && (core.hasMappedProperty(p) || "|AcceptedTaxonID|AcceptedTaxon|HigherTaxonID|HigherTaxon|BasionymID|Basionym|"?contains("|"+p.name+"|"))>
  <${nsr.tagnameQualified(p)}>${(rec.core.getPropertyValue(p)!"")?xml}</${nsr.tagnameQualified(p)}>
</#if>
</#list>
<#-- loop through each extension-->
<#include "/WEB-INF/pages/tapir/model/extensionRecords.ftl">
</dwr:DarwinRecord>