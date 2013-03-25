<?xml version="1.0" encoding="UTF-8"?>
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<thesaurus dc:URI="${vocabulary.uri}" dc:description="${vocabulary.link!}" dc:title="${vocabulary.title}" numConcepts="${concepts?size}" xmlns="http://data.gbif.org/thesaurus" xmlns:dc="http://purl.org/dc/terms/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://data.gbif.org/thesaurus  http://gbif-providertoolkit.googlecode.com/svn/trunk/gbif-providertool/resources/thesaurus.xsd">
<#escape x as x?xml>
<#list concepts as c>
 <concept dc:identifier="${c.identifier}" dc:URI="${c.uri}" <#if c.link??> dc:description="${c.link}"</#if> dc:issued="${c.issued?datetime?string(xmlDateFormat)}">
  <#list c.terms as t>
  <#if t.preferred && t.title??>
  <preferred>
   <term dc:modified="${t.modified?datetime?string(xmlDateFormat)}" dc:source="${t.source!}" dc:title="${t.title}" xml:lang="${t.lang!'en'}"/>
  </preferred>
  </#if>
  </#list>
  <alternative>
  <#list c.terms as t>
  <#if !t.preferred && t.title??>
   <term dc:modified="${t.modified?datetime?string(xmlDateFormat)}" dc:source="${t.source!}" dc:title="${t.title}" xml:lang="${t.lang!'en'}"/>
  </#if>
  </#list>
  </alternative>
 </concept>
</#list>
</#escape>
</thesaurus>