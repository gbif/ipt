<?xml version="1.0"?>
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<#escape x as x?xml>
<feed xmlns="http://www.w3.org/2005/Atom" 
	xmlns:dc="http://purl.org/dc/elements/1.1/" 
	xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#">
    <title>${cfg.ipt.title!IPT}</title>
    <subtitle>Resource metadata of ${cfg.ipt.title!IPT}</subtitle>
    <link rel="self" href="${cfg.getAtomFeedURL()}"/>
    <link rel="alternate" title="IPT Home" type="text/html" href="${cfg.baseUrl!}"/>
	<link rel="first" href="${cfg.getAtomFeedURL()}"/>
	<#if (page>1)>
	<link rel="previous" href="${cfg.getAtomFeedURL(page-1)}"/>
	</#if>
	<#if resources?size==25>
	<link rel="next" href="${cfg.getAtomFeedURL(page+1)}"/>
	</#if>
    <#-- 2003-12-13T18:30:02Z -->      
    <updated>${now?datetime?string(xmlDateFormat)}</updated>
    <author>
	    <name>${cfg.contactName!}</name>
	    <email>${cfg.contactEmail!}</email>
    </author>
    <generator>GBIF IPT <@s.text name="webapp.version"/></generator>
    <id>urn:uuid:${cfg.ipt.uddiID!"not issued yet"}</id>
    <#if (cfg.ipt.location)?? && cfg.ipt.location.latitude?? && cfg.ipt.location.longitude??>
    <#-- single latitude-longitude pair, separated by whitespace: http://georss.org/simple -->
    <geo:point>${cfg.ipt.location.latitude?c} ${cfg.ipt.location.longitude?c}</geo:point>
 	</#if>
	<#list resources as res>
    <entry>
      <title>${res.title}</title>
      <dc:subject>GBIF<#list res.keywords as k> ${k}</#list></dc:subject>
      <#if res.type??>
      <category term="${res.type}"/>
      </#if>
      <id>urn:uuid:${res.guid}</id>
      <link href="${cfg.getResourceUrl(res.guid)}"/>
      <link title="Ecological Markup Language" type="text/xml" rel="alternate" href="${cfg.getEmlUrl(res.guid)}"/>
	  <#if res.link??>
      <link title="Homepage" type="text/html" rel="alternate" href="${res.link}"/>
	  </#if>
      <#-- 2003-12-13T18:30:02Z -->      
      <updated>${res.modified?datetime?string(xmlDateFormat)}</updated>
	  <#if res.contactName??>
      <author>
	    <name>${res.contactName}</name>
	    <email>${res.contactEmail!}</email>
	  </author>
	  </#if>
	  <#if res.geoCoverage?? && res.geoCoverage.isValid()>
	  <#-- space separated list of latitude-longitude pairs: http://georss.org/simple -->
      <geo:polygon>${res.geoCoverage.toStringGeoRSS()}</geo:polygon>
	  </#if>
      <content type="html">&lt;img src="${cfg.getResourceLogoUrl(res.id)}" align="left" style="padding-right:5px;" /&gt; 
      ${res.description} &lt;a href="${cfg.getEmlUrl(res.guid)}"&gt;EML&lt;/a&gt;</content>
    </entry>
 	</#list> 
</feed>
</#escape>